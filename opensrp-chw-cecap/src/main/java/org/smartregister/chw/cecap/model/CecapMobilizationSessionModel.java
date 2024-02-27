package org.smartregister.chw.cecap.model;

public class CecapMobilizationSessionModel {
    private String sessionDate;

    private String healthEducation;

    private String sessionId;

    private String sessionParticipants;

    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(String sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getHealthEducation() {
        return healthEducation;
    }

    public void setHealthEducation(String healthEducation) {
        this.healthEducation = healthEducation;
    }

    public String getSessionParticipants() {
        return sessionParticipants;
    }

    public void setSessionParticipants(String sessionParticipants) {
        this.sessionParticipants = sessionParticipants;
    }
}
