package com.vfd.demo.utils;

import com.vfd.demo.exception.VerificationCodeLengthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;

/**
 * @PackageName: com.vfd.cloud.utils
 * @ClassName: SendMessage
 * @Description:
 * @author: vfdxvffd
 * @date: 11/26/20 4:22 PM
 */
@Component
public class SendMessage {

    @Autowired
    JavaMailSenderImpl mailSender;

    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 发送普通信息到邮箱
     * @param source
     * @param dest
     * @param title
     * @param context
     */
    public void sendMeg(String source, String dest, String title, String context) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(context);
        message.setFrom(source);
        message.setTo(dest);
        mailSender.send(message);
    }

    /**
     * 发送支持html的信息到邮箱
     * @param source
     * @param dest
     * @param title
     * @param context
     * @param html
     * @throws MessagingException
     */
    public void sendMeg(String source, String dest, String title, String context, boolean html) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setSubject(title);
            helper.setText(context,html);
            helper.setFrom(source);
            helper.setTo(dest);
        } catch (MessagingException e) {
            logger.error(e.toString());
            logger.error("带有html的邮件发送失败");
        }
        mailSender.send(mimeMessage);
    }

    /**
     * 生成6位验证码
     * @return
     */
    public String getVerificationCode() {
        Random random = new Random();
        String verificationCode = "";
        for (int i = 0; i < 6; i++) {
            verificationCode += random.nextInt(10);
        }
        if (verificationCode.length() == 6) {
            return verificationCode;
        } else {
            throw new VerificationCodeLengthException(verificationCode.length());
        }
    }
}