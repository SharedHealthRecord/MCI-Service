package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.utils.LuhnChecksumGenerator;
import org.sharedhealth.mci.web.infrastructure.persistence.HealthIdRepository;
import org.sharedhealth.mci.web.model.GeneratedHidRange;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class HealthIdService {
    private static final String MCI_ORG_CODE = "MCI";

    private final Pattern invalidHidPattern;
    private final MCIProperties mciProperties;
    private HealthIdRepository healthIdRepository;
    private LuhnChecksumGenerator checksumGenerator;
    private GeneratedHidRangeService generatedHidRangeService;

    @Autowired
    public HealthIdService(MCIProperties mciProperties, HealthIdRepository healthIdRepository,
                           LuhnChecksumGenerator checksumGenerator, GeneratedHidRangeService generatedHidRangeService) {
        this.mciProperties = mciProperties;
        this.healthIdRepository = healthIdRepository;
        this.checksumGenerator = checksumGenerator;
        this.generatedHidRangeService = generatedHidRangeService;
        invalidHidPattern = Pattern.compile(mciProperties.getInvalidHidPattern());
    }

    public long generateAll() {
        Long start = mciProperties.getMciStartHid();
        Long end = mciProperties.getMciEndHid();
        long numberOfValidHids = 0L;
        for (long i = start; i <= end; i++) {
            numberOfValidHids = saveIfValidHID(numberOfValidHids, i);
        }
        saveGeneratedRange(start, end, numberOfValidHids);
        return numberOfValidHids;
    }

    public long generateBlock(long start, long blockSize) {
        long numberOfValidHids = 0L;
        int i;
        for (i = 0; numberOfValidHids < blockSize; i++) {
            numberOfValidHids = saveIfValidHID(numberOfValidHids, start + i);
        }
        saveGeneratedRange(start, start + i - 1 , numberOfValidHids);
        return numberOfValidHids;
    }

    public synchronized List<MciHealthId> getNextBlock() {
        return healthIdRepository.getNextBlock(mciProperties.getHealthIdBlockSize());
    }

    public synchronized List<MciHealthId> getNextBlock(int blockSize) {
        return healthIdRepository.getNextBlock(blockSize);
    }

    public void markUsed(MciHealthId nextMciHealthId) {
        healthIdRepository.removedUsedHid(nextMciHealthId);
    }

    private long saveIfValidHID(long numberOfValidHids, long currentNumber) {
        String possibleHid = String.valueOf(currentNumber);
        if (!invalidHidPattern.matcher(possibleHid).find()) {
            numberOfValidHids += 1;
            String newHealthId = possibleHid + checksumGenerator.generate(possibleHid.substring(1));
            healthIdRepository.saveHealthId(new MciHealthId(newHealthId));
        }
        return numberOfValidHids;
    }

    private void saveGeneratedRange(Long start, Long end, long numberOfValidHids) {
        if (numberOfValidHids > 0) {
            long blockBeginsAt = Long.parseLong(String.valueOf(start).substring(0, 2));
            GeneratedHidRange generatedHidRange = new GeneratedHidRange(blockBeginsAt, start, end, MCI_ORG_CODE, null);
            generatedHidRangeService.saveGeneratedHidRange(generatedHidRange);
        }
    }
}
