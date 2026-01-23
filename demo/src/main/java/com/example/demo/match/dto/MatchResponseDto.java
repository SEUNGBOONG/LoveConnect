package com.example.demo.match.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchResponseDto {
    private String targetPhone;
    private String targetInsta;
    private String targetName;
    private int requesterDesire;
    private boolean matched;
    private String matchMessage;
}
