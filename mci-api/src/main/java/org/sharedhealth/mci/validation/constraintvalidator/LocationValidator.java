package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import com.datastax.driver.core.exceptions.InvalidQueryException;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.validation.constraints.Location;
import org.sharedhealth.mci.web.model.Address;
import org.sharedhealth.mci.web.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocationValidator implements ConstraintValidator<Location, Address> {

    private static final Logger logger = LoggerFactory.getLogger(LocationValidator.class);

    private LocationService locationService;

    @Autowired
    public LocationValidator(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public void initialize(Location constraintAnnotation) {

    }

    @Override
    public boolean isValid(Address value, ConstraintValidatorContext context) {
        if(value == null) return true;

        String geoCode = value.getGeoCode();

        logger.debug("Validation testing for code : [" + geoCode + "]");

        if(!(Pattern.compile("[\\d]{2,10}").matcher(geoCode).matches())) return false;

        try {
            org.sharedhealth.mci.web.model.Location location = locationService.findByGeoCode(geoCode).get();

            if(!StringUtils.isBlank(location.getGeoCode())) {
                return true;
            }

        } catch (Exception e) {
            Throwable exception = e instanceof ExecutionException && e.getCause() != null ? e.getCause() : e;
            if(exception.getCause().getClass() == InvalidQueryException.class){
                //Ignoring validation if failed to communicate with Location Registry
                return true;
            }
        }

        return false;
    }
}
