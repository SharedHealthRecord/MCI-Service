package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class PatientAuditLogData {

    @JsonProperty(EVENT_TIME)
    private String eventTime;

    @JsonProperty(CHANGE_SET)
    private List<ChangeSet> changeSet;

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public List<ChangeSet> getChangeSet() {
        return changeSet;
    }

    public void setChangeSet(List<ChangeSet> changeSet) {
        this.changeSet = changeSet;
    }
}
