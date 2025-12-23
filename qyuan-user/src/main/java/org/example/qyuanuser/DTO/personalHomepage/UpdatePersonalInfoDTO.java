package org.example.qyuanuser.DTO.personalHomepage;

import lombok.Data;

@Data
public class UpdatePersonalInfoDTO {
    private String username;
    private String first_name;
    private String last_name;
    private String institution;
    private String research_direction;

    public boolean isFull() {
        return username != null && first_name != null && last_name != null && institution != null && research_direction != null;
    }
}
