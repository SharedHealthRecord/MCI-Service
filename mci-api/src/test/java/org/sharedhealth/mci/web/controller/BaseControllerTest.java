package org.sharedhealth.mci.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.sharedhealth.mci.web.mapper.Relation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class BaseControllerTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    @Qualifier("MCICassandraTemplate")
    protected CassandraOperations cqlTemplate;

    protected MockMvc mockMvc;
    protected PatientMapper patientMapper;
    protected ObjectMapper mapper = new ObjectMapper();
    public static final String API_END_POINT = "/api/v1/patients";
    public static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";

    protected MvcResult createPatient(String json) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(API_END_POINT).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    protected MCIMultiResponse getMciMultiResponse(MvcResult result) {
        final ResponseEntity asyncResult = (ResponseEntity< MCIMultiResponse>) result.getAsyncResult();

        return (MCIMultiResponse)asyncResult.getBody();
    }

    protected MCIResponse getMciResponse(MvcResult result) {
        final ResponseEntity asyncResult = (ResponseEntity< MCIResponse>) result.getAsyncResult();

        return (MCIResponse)asyncResult.getBody();
    }

    protected PatientMapper getPatientObjectFromString(String json) throws Exception  {
        return mapper.readValue(json, PatientMapper.class);
    }

    protected boolean isRelationsEqual(List<Relation> original, List<Relation> patient) {
        return original.containsAll(patient) && patient.containsAll(original);
    }

    protected void assertPatientEquals(PatientMapper original, PatientMapper patient) {
        synchronizeAutoGeneratedFields(original, patient);
        Assert.assertEquals(original, patient);
    }

    protected void synchronizeAutoGeneratedFields(PatientMapper original, PatientMapper patient) {
        original.setHealthId(patient.getHealthId());
        original.setCreatedAt(patient.getCreatedAt());
        original.setUpdatedAt(patient.getUpdatedAt());
        synchronizeRelationsId(original, patient);
    }

    protected void synchronizeRelationsId(PatientMapper original, PatientMapper patient) {
        int y = original.getRelations().size();

        for(int x = 0; x < y; x = x+1) {
            original.getRelations().get(x).setId(patient.getRelationOfType(original.getRelations().get(x).getType()).getId());
        }
    }

    protected PatientMapper getPatientObjectFromResponse(ResponseEntity asyncResult) throws Exception {
        return getPatientObjectFromString(mapper.writeValueAsString((PatientMapper) asyncResult.getBody()));
    }

}
