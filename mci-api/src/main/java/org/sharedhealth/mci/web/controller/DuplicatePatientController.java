package org.sharedhealth.mci.web.controller;

import org.apache.commons.collections4.Predicate;
import org.sharedhealth.mci.web.exception.Forbidden;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMergeData;
import org.sharedhealth.mci.web.service.DuplicatePatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.find;
import static org.sharedhealth.mci.web.infrastructure.security.UserProfile.ADMIN_TYPE;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/patients/duplicates")
public class DuplicatePatientController extends MciController {

    private static final Logger logger = LoggerFactory.getLogger(DuplicatePatientController.class);

    private DuplicatePatientService duplicatePatientService;

    @Autowired
    public DuplicatePatientController(DuplicatePatientService duplicatePatientService) {
        this.duplicatePatientService = duplicatePatientService;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Approver')")
    @RequestMapping(value = "/catchments/{catchmentId}", method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findAllByCatchment(@PathVariable String catchmentId) {

        UserInfo userInfo = getUserInfo();
        String message = format("Find list of patient duplicates for catchment %s", catchmentId);
        logAccessDetails(userInfo, message);
        logger.debug(message);

        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        if (!userInfo.getProperties().hasCatchmentForProfileType(catchmentId, asList(ADMIN_TYPE))) {
            String errorMessage = format("Access is denied to user %s for catchment %s",
                    userInfo.getProperties().getId(), catchmentId);
            deferredResult.setErrorResult(new Forbidden(errorMessage));
            logger.debug(errorMessage);
            return deferredResult;
        }

        List<DuplicatePatientData> response = findDuplicatesByCatchment(catchmentId);

        MCIMultiResponse mciMultiResponse;
        if (response != null) {
            mciMultiResponse = new MCIMultiResponse(response, null, OK);
        } else {
            mciMultiResponse = new MCIMultiResponse(emptyList(), null, OK);
        }
        deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));
        return deferredResult;
    }

    private List<DuplicatePatientData> findDuplicatesByCatchment(String catchmentId) {
        ArrayList<DuplicatePatientData> duplicates = new ArrayList<>(
                duplicatePatientService.findAllByCatchment(new Catchment(catchmentId)));
        DuplicatePatientData duplicate;
        for (Iterator<DuplicatePatientData> it = duplicates.iterator(); it.hasNext(); ) {
            duplicate = it.next();
            if (findDuplicate(duplicate.getHealthId2(), duplicate.getHealthId1(), duplicates)) {
                it.remove();
            }
        }
        return duplicates;
    }

    private boolean findDuplicate(final String healthId1, final String healthId2, List<DuplicatePatientData> duplicates) {
        return find(duplicates, new Predicate<DuplicatePatientData>() {
            @Override
            public boolean evaluate(DuplicatePatientData duplicate) {
                return duplicate.getHealthId1().equals(healthId1) && duplicate.getHealthId2().equals(healthId2);
            }
        }) != null;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Approver')")
    @RequestMapping(method = PUT, consumes = {APPLICATION_JSON_VALUE})
    public DeferredResult<ResponseEntity<MCIResponse>> merge(
            @Valid @RequestBody DuplicatePatientMergeData data,
            BindingResult bindingResult) {

        UserInfo userInfo = getUserInfo();
        String message = format("Merging duplicate patients. HIDs: %s and %s. Action: %s",
                data.getPatient1().getHealthId(), data.getPatient2().getHealthId(), data.getAction());
        logAccessDetails(userInfo, message);
        logger.debug(message);

        if (bindingResult.hasErrors()) {
            logger.debug("ValidationException while merging duplicate patients");
            throw new ValidationException(bindingResult);
        }

        setRequester(data, userInfo);
        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();
        duplicatePatientService.processDuplicates(data);

        MCIResponse mciResponse = new MCIResponse(ACCEPTED);
        deferredResult.setResult(new ResponseEntity<>(mciResponse, mciResponse.httpStatusObject));
        return deferredResult;
    }

    private void setRequester(DuplicatePatientMergeData data, UserInfo userInfo) {
        UserInfo.UserInfoProperties properties = userInfo.getProperties();
        data.getPatient1().setRequester(properties);
        data.getPatient2().setRequester(properties);
    }
}
