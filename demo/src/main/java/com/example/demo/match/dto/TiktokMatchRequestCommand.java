package com.example.demo.match.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TiktokMatchRequestCommand {
    private String targetPhone;
    private String targetTiktok;
    private String targetName;
    private int requesterDesire;
}
