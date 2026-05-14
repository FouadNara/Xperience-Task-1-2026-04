package com.xperience.hero.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RSVPRequest {
    private String status; // YES, NO, MAYBE
}
