package com.survey.response.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "location_data")
public class LocationDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "response_id", nullable = false)
    private Long responseId;

    @Column(name = "coordinates")
    private String coordinates;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "ip_info", columnDefinition = "TEXT")
    private String ipInfo;

    @Column(name = "device_info", columnDefinition = "TEXT")
    private String deviceInfo;

    @OneToOne
    @JoinColumn(name = "response_id", insertable = false, updatable = false)
    private SurveyResponseEntity surveyResponse;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResponseId() {
        return responseId;
    }

    public void setResponseId(Long responseId) {
        this.responseId = responseId;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getIpInfo() {
        return ipInfo;
    }

    public void setIpInfo(String ipInfo) {
        this.ipInfo = ipInfo;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public SurveyResponseEntity getSurveyResponse() {
        return surveyResponse;
    }

    public void setSurveyResponse(SurveyResponseEntity surveyResponse) {
        this.surveyResponse = surveyResponse;
    }
}
