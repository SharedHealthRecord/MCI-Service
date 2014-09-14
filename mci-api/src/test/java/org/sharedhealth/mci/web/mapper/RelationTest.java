package org.sharedhealth.mci.web.mapper;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import org.hibernate.validator.HibernateValidator;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RelationTest {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void shouldFailIf_Marriage_Id_LengthIsNotEqual_8() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "marriageId", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIf_Marriage_Id_ContainSpecialCharacter() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "marriageId", "123456*8901");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIf_Marriage_Id_Is_8_DigitAlphaNumeric() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "marriageId", "MM001177");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfGivenNameIsMoreThan_100_Characters() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "givenName", "janagiralamkabirkhanjahanaliahmadpuri janagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpuri");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfGivenNameIsValid() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "givenName", "imran");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfSurNameIsValid() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "surName", "imran");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfSurNameIsMoreThan_25_Characters() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "surName", "janagiralamkabirkhanjahanaliahmadpuri");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfFullNameBanglaIsValid() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "nameBangla", "এ বি এম আখতার হোসেন মন্ডল");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfFullNameBanglaIsMoreThan_125_Characters() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "nameBangla", "এ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডল");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfNationalIdIsInvalid() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "nationalId", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfNationalIdIs_13_DigitLong() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "nationalId", "1234567890123");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfNationalIdIs_17_DigitLong() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "nationalId", "12345678901234567");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIf_UID_LengthIsNotEqual_11() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "uid", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIf_UUID_ContainSpecialCharacter() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "uid", "123456*8901");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIf_UID_Is_11_DigitAlphaNumeric() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "uid", "UID45678901");
        assertEquals(0, constraintViolations.size());
    }
    @Test
    public void shouldPassIfBirthRegistrationNumberIsValid() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "birthRegistrationNumber", "12345674891234567");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfBirthRegistrationNumberIsLessThan17() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "birthRegistrationNumber", "1234567489123456");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfBirthRegistrationNumberIsMoreThan17() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "birthRegistrationNumber", "123456748912345644");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfRelationalStatusIsValid() {
        for(int i = 3; i<6; i++) {
            Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "relationalStatus", Integer.toString(i));
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldFailIfRelationalStatusIsInvalid() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "relationalStatus", "8");
        assertEquals(1, constraintViolations.size());
        assertEquals("1004", constraintViolations.iterator().next().getMessage());
    }

}
