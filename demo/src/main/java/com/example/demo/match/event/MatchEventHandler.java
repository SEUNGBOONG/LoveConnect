package com.example.demo.match.event;

import com.example.demo.login.util.AligoSmsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchEventHandler {

    private final AligoSmsUtil smsUtil;

    @EventListener
    public void handle(MatchCompletedEvent event) {
        String msg = "[LoveConnect] 🎉 매칭 완료!\n" + event.matchMessage().getMessage();
        smsUtil.sendSms(event.requester().getPhoneNumber(), msg);
        smsUtil.sendSms(event.target().getPhoneNumber(), msg);
    }
}
