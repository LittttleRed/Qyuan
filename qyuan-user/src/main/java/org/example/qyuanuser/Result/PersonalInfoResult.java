package org.example.qyuanuser.Result;

import lombok.Data;

@Data
public class PersonalInfoResult {
    private boolean success;
    private String message;
    private PersonalInfo personalInfo;

    @Data
    public class PersonalInfo { 
        private String username;
        private String email;
        private String first_name;
        private String last_name;
        private String institution;
        private String research_direction;
        private String orcid_code;
    }

    public void setData(String username, String email, String first_name, String last_name, String institution, String research_direction, String orcid_code) { 
        personalInfo = this.getPersonalInfo();
        personalInfo.setUsername(username);
        personalInfo.setEmail(email);
        personalInfo.setFirst_name(first_name);
        personalInfo.setLast_name(last_name);
        personalInfo.setInstitution(institution);
        personalInfo.setResearch_direction(research_direction);
        personalInfo.setOrcid_code(orcid_code);
    }
}
