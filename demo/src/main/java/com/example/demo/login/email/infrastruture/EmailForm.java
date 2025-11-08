package com.example.demo.login.email.infrastruture;

import com.example.demo.common.exception.Setting;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;

@Component
public class EmailForm {

    public static final String UTF_8 = "utf-8";
    public static final String HTML = "html";
    public static final String CODE = "code";
    public static final String MAIL = "mail";

    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailForm(final JavaMailSender emailSender, final SpringTemplateEngine templateEngine) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    public MimeMessage createEmailForm(String email, String authNum) throws MessagingException, UnsupportedEncodingException {
        return getMimeMessage(email, authNum);
    }

    private MimeMessage getMimeMessage(final String email, final String authNum) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject(String.valueOf(Setting.AUTH_NUMBER));
        message.setFrom(String.valueOf(Setting.EMAIL));
        message.setText(setContext(authNum), UTF_8, HTML);
        return message;
    }

    private String setContext(String code) {
        Context context = new Context();
        context.setVariable(CODE, code);
        return templateEngine.process(MAIL, context);
    }
}
