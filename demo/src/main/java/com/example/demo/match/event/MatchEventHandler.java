package com.example.demo.match.event;

import com.example.demo.common.util.AESUtil;
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
        String msg = "[LoveConnect]  매칭 완료!\n" + event.matchMessage().getMessage();

        // 🔥 전화번호 복호화 후 문자 전송
        String requesterPhone = AESUtil.decrypt(event.requester().getPhoneNumber());
        String targetPhone = AESUtil.decrypt(event.target().getPhoneNumber());

        smsUtil.sendSms(requesterPhone, msg);
        smsUtil.sendSms(targetPhone, msg);
    }
}
