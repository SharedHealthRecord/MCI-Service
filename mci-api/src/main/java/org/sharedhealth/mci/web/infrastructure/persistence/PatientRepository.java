package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.utils.UidGenerator;
import org.sharedhealth.mci.web.exception.HealthIDExistException;
import org.sharedhealth.mci.web.exception.PatientNotFoundException;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.handler.PatientFilter;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.model.Patient;
import org.sharedhealth.mci.web.model.PendingApprovalMapping;
import org.sharedhealth.mci.web.model.PendingApprovalRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.utils.UUIDs.unixTimestamp;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.*;

@Component
public class PatientRepository extends BaseRepository {

    protected static final Logger logger = LoggerFactory.getLogger(PatientRepository.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private UidGenerator uidGenerator;
    private PatientMapper mapper;

    @Autowired
    public PatientRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations,
                             PatientMapper mapper,
                             UidGenerator uidGenerator) {
        super(cassandraOperations);
        this.mapper = mapper;
        this.uidGenerator = uidGenerator;
    }

    public MCIResponse create(PatientData patientData) {
        if (!StringUtils.isBlank(patientData.getHealthId())) {
            DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(patientData, "patient");
            bindingResult.addError(new FieldError("patient", "hid", "3001"));
            throw new HealthIDExistException(bindingResult);
        }
        Patient patient = mapper.map(patientData, new PatientData());
        patient.setHealthId(uidGenerator.getId());
        patient.setCreatedAt(new Date());
        patient.setUpdatedAt(new Date());

        if(StringUtils.isBlank(patient.getStatus())) {
            patient.setStatus("alive");
        }

        cassandraOps.execute(buildSaveBatch(patient, cassandraOps.getConverter()));
        return new MCIResponse(patient.getHealthId(), HttpStatus.CREATED);
    }

    public MCIResponse update(PatientData patientDataForUpdate, String healthId) {
        PatientData existingPatientData = this.findByHealthId(healthId);

        Properties properties = getApprovalProperties();
        PatientData patientDataToSave = new PatientData();
        patientDataToSave.setHealthId(healthId);
        patientDataForUpdate.setHealthId(healthId);

        PatientFilter patientFilter = new PatientFilter(properties, existingPatientData, patientDataForUpdate, patientDataToSave);
        PendingApprovalRequest pendingApprovalRequest = patientFilter.filter();

        String fullName = "";

        if (patientDataForUpdate.getGivenName() != null) {
            fullName = patientDataForUpdate.getGivenName();
        }

        if (patientDataForUpdate.getSurName() != null) {
            fullName = fullName + " " + patientDataForUpdate.getSurName();
        }

        Patient patientToSave = mapper.map(patientDataToSave, existingPatientData);
        patientToSave.setHealthId(healthId);
        patientToSave.setFullName(fullName);
        patientToSave.setUpdatedAt(new Date());

        Catchment catchment = new Catchment(existingPatientData.getAddress().getDivisionId(),
                existingPatientData.getAddress().getDistrictId(),
                existingPatientData.getAddress().getUpazilaId());

        cassandraOps.execute(buildUpdateBatch(patientToSave, pendingApprovalRequest, catchment));
        return new MCIResponse(patientToSave.getHealthId(), HttpStatus.ACCEPTED);
    }

    private Properties getApprovalProperties() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("approvalFeilds.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read approval property file.", e);
        }
        return properties;
    }

    private TreeSet<PendingApproval> buildPendingApproval(UUID uuid, PendingApprovalRequest request) {
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        Map<String, String> requestFields = request.getFields();

        for (String fieldName : requestFields.keySet()) {
            PendingApproval pendingApproval = new PendingApproval();
            pendingApproval.setName(fieldName);
            pendingApproval.setCurrentValue(null);

            PendingApprovalFieldDetails fieldDetails = new PendingApprovalFieldDetails();
            fieldDetails.setValue(requestFields.get(fieldName));
            fieldDetails.setFacilityId(request.getFacilityId());
            fieldDetails.setCreatedAt(unixTimestamp(uuid));

            TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();
            fieldDetailsMap.put(uuid, fieldDetails);
            pendingApproval.setFieldDetails(fieldDetailsMap);
            pendingApprovals.add(pendingApproval);
        }
        return pendingApprovals;
    }

    private Batch buildUpdateBatch(Patient patient, PendingApprovalRequest pendingApprovalRequest, Catchment catchment) {
        Batch batch = QueryBuilder.batch();
        if (pendingApprovalRequest != null) {
            TreeSet<PendingApproval> existingPendingApprovals = patient.getPendingApprovals();
            String healthId = patient.getHealthId();

            if (existingPendingApprovals != null && existingPendingApprovals.size() > 0) {
                UUID lastUpdated = findLatestUuid(existingPendingApprovals);
                PendingApprovalMapping mapping = buildPendingApprovalMapping(catchment, lastUpdated, healthId);
                batch.add(buildDeletePendingApprovalMappingStmt(mapping, cassandraOps.getConverter()));
            }

            UUID uuid = UUIDs.timeBased();
            PendingApprovalMapping mapping = buildPendingApprovalMapping(catchment, uuid, healthId);
            batch.add(buildCreatePendingApprovalMappingStmt(mapping, cassandraOps.getConverter()));

            patient.addPendingApprovals(buildPendingApproval(uuid, pendingApprovalRequest));
        }
        batch.add(buildUpdateStmt(patient, cassandraOps.getConverter()));
        return batch;
    }

    UUID findLatestUuid(TreeSet<PendingApproval> pendingApprovals) {
        UUID latest = null;
        for (PendingApproval pendingApproval : pendingApprovals) {
            UUID uuid = pendingApproval.getFieldDetails().keySet().iterator().next();
            if (latest == null) {
                latest = uuid;
                continue;
            }
            if (unixTimestamp(uuid) > unixTimestamp(latest)) {
                latest = uuid;
            }
        }
        return latest;
    }

    private PendingApprovalMapping buildPendingApprovalMapping(Catchment catchment, UUID uuid, String healthId) {
        PendingApprovalMapping pendingApprovalMapping;
        pendingApprovalMapping = new PendingApprovalMapping();
        pendingApprovalMapping.setDivisionId(catchment.getDivisionId());
        pendingApprovalMapping.setDistrictId(catchment.getDistrictId());
        pendingApprovalMapping.setUpazilaId(catchment.getUpazilaId());
        pendingApprovalMapping.setLastUpdated(uuid);
        pendingApprovalMapping.setHealthId(healthId);
        return pendingApprovalMapping;
    }

    public PatientData findByHealthId(final String healthId) {
        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        if (patient == null) {
            throw new PatientNotFoundException("No patient found with health id: " + healthId);
        }
        return mapper.map(patient);
    }

    public List<PatientData> findByHealthId(List<String> healthIds) {
        String[] values = healthIds.toArray(new String[healthIds.size()]);
        List<Patient> patients = cassandraOps.select(buildFindByHidStmt(values), Patient.class);
        if (isEmpty(patients)) {
            throw new PatientNotFoundException("No patient found with health ids: " + healthIds);
        }
        return mapper.map(patients);
    }

    public List<PatientData> findAllByQuery(SearchQuery searchQuery) {
        return filterPatients(findProbables(searchQuery), searchQuery);
    }

    public List<PatientSummaryData> findAllSummaryByQuery(SearchQuery searchQuery) {
        return mapper.mapSummary(filterPatients(findProbables(searchQuery), searchQuery));
    }

    private List<PatientData> findProbables(SearchQuery searchQuery) {
        List<PatientData> dataList = new ArrayList<>();
        String query = null;

        if (isNotBlank(searchQuery.getNid())) {
            query = buildFindByNidStmt(searchQuery.getNid());

        } else if (isNotBlank(searchQuery.getBin_brn())) {
            query = buildFindByBrnStmt(searchQuery.getBin_brn());

        } else if (isNotBlank(searchQuery.getUid())) {
            query = buildFindByUidStmt(searchQuery.getUid());

        } else if (isNotBlank(searchQuery.getPhone_no())) {
            query = buildFindByPhoneNumberStmt(searchQuery.getPhone_no());

        } else if (isNotBlank(searchQuery.getPresent_address()) && isNotBlank(searchQuery.getGiven_name())) {
            query = buildFindByNameStmt(searchQuery.getDivisionId(), searchQuery.getDistrictId(),
                    searchQuery.getUpazilaId(), searchQuery.getGiven_name(), searchQuery.getSur_name());
        }

        if (isNotBlank(query)) {
            List<String> healthIds = cassandraOps.queryForList(query, String.class);
            if (isNotEmpty(healthIds)) {
                dataList.addAll(this.findByHealthId(healthIds));
            }
        }
        return dataList;
    }

    private List<PatientData> filterPatients(List<PatientData> patients, SearchQuery searchQuery) {
        List<PatientData> result = new ArrayList<>();
        for (PatientData patient : patients) {
            if (isMatchingPatient(patient, searchQuery)) {
                result.add(patient);
            }
        }
        return result;
    }

    private boolean isMatchingPatient(PatientData p, SearchQuery searchQuery) {
        if (isNotBlank(searchQuery.getNid()) && !p.getNationalId().equals(searchQuery.getNid())) {
            return false;
        }
        if (isNotBlank(searchQuery.getBin_brn()) && !searchQuery.getBin_brn().equals(p.getBirthRegistrationNumber())) {
            return false;
        }
        if (isNotBlank(searchQuery.getUid()) && !searchQuery.getUid().equals(p.getUid())) {
            return false;
        }
        if (isNotBlank(searchQuery.getArea_code()) && !searchQuery.getArea_code().equals(p.getPhoneNumber().getAreaCode())) {
            return false;
        }
        if (isNotBlank(searchQuery.getPhone_no()) && !searchQuery.getPhone_no().equals(p.getPhoneNumber().getNumber())) {
            return false;
        }
        Address address = p.getAddress();
        if (isNotBlank(searchQuery.getDivisionId()) && !searchQuery.getDivisionId().equals(address.getDivisionId())) {
            return false;
        }
        if (isNotBlank(searchQuery.getDistrictId()) && !searchQuery.getDistrictId().equals(address.getDistrictId())) {
            return false;
        }
        if (isNotBlank(searchQuery.getUpazilaId()) && !searchQuery.getUpazilaId().equals(address.getUpazilaId())) {
            return false;
        }
        if (isNotBlank(searchQuery.getGiven_name()) && !searchQuery.getGiven_name().equalsIgnoreCase(p.getGivenName())) {
            return false;
        }
        if (isNotBlank(searchQuery.getSur_name()) && !searchQuery.getSur_name().equalsIgnoreCase(p.getSurName())) {
            return false;
        }
        return true;
    }

    public List<PatientData> findAllByLocations(List<String> locations, String start, Date since) {
        List<PatientData> dataList = new ArrayList<>();
        int limit = PER_PAGE_LIMIT;

        if (locations != null && locations.size() > 0) {
            String locationPointer = getLocationPointer(locations, start, null);

            for (String catchment : locations) {
                if (dataList.size() == 0 && !isLocationBelongsToCatchment(locationPointer, catchment)) {
                    continue;
                }
                try {
                    dataList.addAll(this.findAllByLocation(catchment, start, limit, since));
                    if (dataList.size() < PER_PAGE_LIMIT) {
                        start = null;
                        limit = PER_PAGE_LIMIT - dataList.size();
                        locationPointer = null;
                    } else {
                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            return this.findAllByLocation(null, start, limit, since);
        }
        return dataList;
    }

    List<PatientData> findAllByLocation(String location, String start, int limit, Date since) {
        Select select = select().from("patient");
        select.where(QueryBuilder.eq(getAddressHierarchyField(location.length()), location));

        if (isNotBlank(start)) {
            select.where(QueryBuilder.gt(QueryBuilder.token("health_id"), QueryBuilder.raw("token('" + start + "')")));
        }
        if (since != null) {
            select.where(QueryBuilder.gt("updated_at", since));
            select.allowFiltering();
        }

        if (limit > 0) {
            select.limit(limit);
        }
        return mapper.map(cassandraOps.select(select, Patient.class));
    }

    private String getLocationPointer(List<String> locations, String start, String d) {
        if (locations.size() > 1 && isNotBlank(start)) {
            PatientData p = findByHealthId(start);
            return p.getAddress().getGeoCode();
        }
        return d;
    }

    private boolean isLocationBelongsToCatchment(String location, String catchment) {
        return StringUtils.isBlank(location) || location.startsWith(catchment);
    }

    private String getAddressHierarchyField(int length) {
        return "location_level" + (length / 2);
    }

    public List<PendingApprovalMapping> findPendingApprovalMapping(Catchment catchment, UUID after, UUID before, int limit) {
        return cassandraOps.select(buildFindPendingApprovalMappingStmt(catchment, after, before, limit), PendingApprovalMapping.class);
    }

    public String updatePendingApprovals(PatientData patient) {
        return null;
    }
}
