package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.sharedhealth.mci.web.model.OrgHealthId;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PatientHealthIdService {
    private static final Logger logger = getLogger(PatientHealthIdService.class);

    private Queue<MciHealthId> MciHealthIds = new ConcurrentLinkedQueue<>();

    private HealthIdService healthIdService;
    private final MCIProperties mciProperties;

    @Autowired
    public PatientHealthIdService(HealthIdService healthIdService, MCIProperties mciProperties) {
        this.healthIdService = healthIdService;
        this.mciProperties = mciProperties;
    }

    public MciHealthId getNextHealthId() throws InterruptedException {
        return MciHealthIds.remove();
    }

    public void putBackHealthId(MciHealthId MciHealthId) {
        MciHealthIds.add(MciHealthId);
    }

    public void replenishIfNeeded() {
        logger.debug("Replenish, Remaining Health IDs :" + MciHealthIds.size());
        if (MciHealthIds.size() < mciProperties.getHealthIdBlockSizeThreshold()) {
            MciHealthIds.addAll(healthIdService.getNextBlock());
        }
    }

    public void markUsed(MciHealthId nextMciHealthId) {
        healthIdService.markMCIHealthIdUsed(nextMciHealthId);
    }

    public int getHealthIdBlockSize() {
        return MciHealthIds.size();
    }

    public OrgHealthId findOrgHealthId(String healthId) {
        return healthIdService.findOrgHealthId(healthId);
    }

    public void markOrgHealthIdUsed(OrgHealthId orgHealthId) {
        healthIdService.markOrgHealthIdUsed(orgHealthId);
    }
}
