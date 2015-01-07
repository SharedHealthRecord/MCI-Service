package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.validation.constraints.Code;
import org.sharedhealth.mci.validation.group.RequiredOnUpdateGroup;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_INVALID;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_PATTERN;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

@JsonIgnoreProperties({"geoCode"})
public class Address {

    @JsonProperty(ADDRESS_LINE)
    @NotNull(message = "1001", groups = RequiredOnUpdateGroup.class)
    @Size(min = 3, max = 255, message = ERROR_CODE_PATTERN)
    private String addressLine;

    @JsonProperty(DIVISION_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = ERROR_CODE_PATTERN)
    private String divisionId;

    @JsonProperty(DISTRICT_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = ERROR_CODE_PATTERN)
    private String districtId;

    @JsonProperty(UPAZILA_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = ERROR_CODE_PATTERN)
    private String upazilaId;

    @JsonProperty(CITY_CORPORATION_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = ERROR_CODE_PATTERN)
    private String cityCorporationId;

    @JsonProperty(UNION_OR_URBAN_WARD_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = ERROR_CODE_PATTERN)
    private String unionOrUrbanWardId;

    @JsonProperty(RURAL_WARD_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = ERROR_CODE_PATTERN)
    private String ruralWardId;

    @JsonProperty(HOLDING_NUMBER)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = ERROR_CODE_PATTERN)
    private String holdingNumber;

    @JsonProperty(STREET)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = ERROR_CODE_PATTERN)
    private String street;

    @JsonProperty(AREA_MOUJA)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = ERROR_CODE_PATTERN)
    private String areaMouja;

    @JsonProperty(VILLAGE)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = ERROR_CODE_PATTERN)
    private String village;

    @JsonProperty(POST_OFFICE)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = ERROR_CODE_PATTERN)
    private String postOffice;

    @JsonProperty(POST_CODE)
    @JsonInclude(NON_EMPTY)
    private String postCode;


    @JsonProperty(COUNTRY_CODE)
    @JsonInclude(NON_EMPTY)
    @Code(type = COUNTRY_CODE, regexp = "[\\d]{3}", message = ERROR_CODE_INVALID)
    private String countryCode = "050";

    public Address() {
    }

    public Address(String divisionId, String districtId, String upazilaId) {
        this.divisionId = divisionId;
        this.districtId = districtId;
        this.upazilaId = upazilaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;

        Address that = (Address) o;

        if (!defaultString(addressLine).equals(defaultString(that.addressLine))) return false;
        if (!defaultString(divisionId).equals(defaultString(that.divisionId))) return false;
        if (!defaultString(districtId).equals(defaultString(that.districtId))) return false;
        if (!defaultString(upazilaId).equals(defaultString(that.upazilaId))) return false;
        if (!defaultString(cityCorporationId).equals(defaultString(that.cityCorporationId))) return false;
        if (!defaultString(unionOrUrbanWardId).equals(defaultString(that.unionOrUrbanWardId))) return false;
        if (!defaultString(ruralWardId).equals(defaultString(that.ruralWardId))) return false;
        if (!defaultString(holdingNumber).equals(defaultString(that.holdingNumber))) return false;
        if (!defaultString(street).equals(defaultString(that.street))) return false;
        if (!defaultString(areaMouja).equals(defaultString(that.areaMouja))) return false;
        if (!defaultString(village).equals(defaultString(that.village))) return false;
        if (!defaultString(postOffice).equals(defaultString(that.postOffice))) return false;
        if (!defaultString(postCode).equals(defaultString(that.postCode))) return false;
        if (!defaultString(countryCode).equals(defaultString(that.countryCode))) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = addressLine != null ? addressLine.hashCode() : 0;
        result = 31 * result + (divisionId != null ? divisionId.hashCode() : 0);
        result = 31 * result + (districtId != null ? districtId.hashCode() : 0);
        result = 31 * result + (upazilaId != null ? upazilaId.hashCode() : 0);
        result = 31 * result + (cityCorporationId != null ? cityCorporationId.hashCode() : 0);
        result = 31 * result + (unionOrUrbanWardId != null ? unionOrUrbanWardId.hashCode() : 0);
        result = 31 * result + (ruralWardId != null ? ruralWardId.hashCode() : 0);
        result = 31 * result + (holdingNumber != null ? holdingNumber.hashCode() : 0);
        result = 31 * result + (street != null ? street.hashCode() : 0);
        result = 31 * result + (areaMouja != null ? areaMouja.hashCode() : 0);
        result = 31 * result + (village != null ? village.hashCode() : 0);
        result = 31 * result + (postOffice != null ? postOffice.hashCode() : 0);
        result = 31 * result + (postCode != null ? postCode.hashCode() : 0);
        result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Address{" +
                "addressLine='" + addressLine + '\'' +
                ", divisionId='" + divisionId + '\'' +
                ", districtId='" + districtId + '\'' +
                ", upazilaId='" + upazilaId + '\'' +
                ", cityCorporationId='" + cityCorporationId + '\'' +
                ", unionOrUrbanWardId='" + unionOrUrbanWardId + '\'' +
                ", ruralWardId='" + ruralWardId + '\'' +
                ", holdingNumber='" + holdingNumber + '\'' +
                ", street='" + street + '\'' +
                ", areaMouja='" + areaMouja + '\'' +
                ", village='" + village + '\'' +
                ", postOffice='" + postOffice + '\'' +
                ", postCode='" + postCode + '\'' +
                ", countryCode='" + countryCode + '\'' +
                '}';
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(String divisionId) {
        this.divisionId = divisionId;
    }

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    public String getUpazilaId() {
        return upazilaId;
    }

    public void setUpazilaId(String upazilaId) {
        this.upazilaId = upazilaId;
    }

    public String getUnionOrUrbanWardId() {
        return unionOrUrbanWardId;
    }

    public void setUnionOrUrbanWardId(String unionOrUrbanWardId) {
        this.unionOrUrbanWardId = unionOrUrbanWardId;
    }

    public String getHoldingNumber() {
        return holdingNumber;
    }

    public void setHoldingNumber(String holdingNumber) {
        this.holdingNumber = holdingNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getAreaMouja() {
        return areaMouja;
    }

    public void setAreaMouja(String areaMouja) {
        this.areaMouja = areaMouja;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getPostOffice() {
        return postOffice;
    }

    public void setPostOffice(String postOffice) {
        this.postOffice = postOffice;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCityCorporationId() {
        return cityCorporationId;
    }

    public void setCityCorporationId(String cityCorporationId) {
        this.cityCorporationId = cityCorporationId;
    }

    public String getRuralWardId() {
        return ruralWardId;
    }

    public void setRuralWardId(String ruralWardId) {
        this.ruralWardId = ruralWardId;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getGeoCode() {
        String gCode = "";

        if (StringUtils.isNotBlank(this.getDivisionId())) {
            gCode += this.getDivisionId();
        }

        if (StringUtils.isNotBlank(this.getDistrictId())) {
            gCode += this.getDistrictId();
        }

        if (StringUtils.isNotBlank(this.getUpazilaId())) {
            gCode += this.getUpazilaId();
        }

        if (StringUtils.isNotBlank(this.getCityCorporationId())) {
            gCode += this.getCityCorporationId();
        }

        if (StringUtils.isNotBlank(this.getUnionOrUrbanWardId())) {
            gCode += this.getUnionOrUrbanWardId();
        }

        if (StringUtils.isNotBlank(this.getRuralWardId())) {
            gCode += this.getRuralWardId();
        }

        return gCode;
    }
}
