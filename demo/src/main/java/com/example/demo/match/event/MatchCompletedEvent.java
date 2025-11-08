package com.example.demo.match.event;

import com.example.demo.login.member.domain.member.Member;
import com.example.demo.match.domain.MatchMessage;

public record MatchCompletedEvent(
        Member requester,
        Member target,
        MatchMessage matchMessage
) {}
