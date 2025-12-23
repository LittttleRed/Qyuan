package org.example.qyuanuser.DTO.system;

import lombok.Data;

@Data
public class UpdateThemeDTO {
    private String theme_type;
    
    public boolean isFull() {
        return theme_type != null;
    }
}
