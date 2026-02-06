package com.example.demo.login.toss.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossDisconnectRequest(
        Long userKey
) {}
