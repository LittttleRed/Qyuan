package org.example.qyuanuser.Result;

import lombok.Data;

@Data
public class PersonalInfoResult {
    private boolean success;
    private String message;
    private PersonalInfo personalInfo;

    @Data
    public class PersonalInfo {
        private Integer user_id;
        private String username;
        private String email;
        private String first_name;
        private String last_name;
        private String institution;
        private String research_direction;
        private String orcid_code;
        private int permission_level;
        private String expire_time;
        private int use_times;
        private String avatar;
    }

    public void setData(Integer user_id,String username, String email, String first_name, String last_name, String institution, String research_direction, String orcid_code
                         , int permission_level, String expire_time, int use_times,String avatar) {
        personalInfo = new PersonalInfo();
        personalInfo.setUser_id(user_id);
        personalInfo.setUsername(username);
        personalInfo.setEmail(email);
        personalInfo.setFirst_name(first_name);
        personalInfo.setLast_name(last_name);
        personalInfo.setInstitution(institution);
        personalInfo.setResearch_direction(research_direction);
        personalInfo.setOrcid_code(orcid_code);
        personalInfo.setPermission_level(permission_level);
        personalInfo.setExpire_time(expire_time);
        personalInfo.setUse_times(use_times);
        personalInfo.setAvatar(avatar);
    }
}
