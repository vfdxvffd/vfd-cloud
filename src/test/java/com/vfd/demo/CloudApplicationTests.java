package com.vfd.demo;

import com.vfd.demo.service.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.ArrayList;

@SpringBootTest
class CloudApplicationTests {

    //注入邮件发送器
    @Autowired
    JavaMailSenderImpl mailSender;

    @Autowired
    RedisService redisService;

    /**
     * 发送简单邮件
     */
    @Test
    void contextLoads() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("骚扰邮件");
        message.setText("我是骚扰邮件你信吗？");
        message.setFrom("2201986113@qq.com");
        message.setTo("824681091@qq.com");
        mailSender.send(message);
    }

    /**
     * 发送复杂邮件
     */
    @Test
    public void test() throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        //支持文件上传
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setSubject("我是标题");
        helper.setText("<h1>我是内容,我支持HTML标签哦~</h1>",true);
        helper.setFrom("373675032@qq.com");
        helper.setTo("3318438484@qq.com");
        //上传文件
        helper.addAttachment("1.jpg",new File("E:\\壁纸\\1.png"));
        helper.addAttachment("2.jpg",new File("E:\\壁纸\\2.png"));
        mailSender.send(mimeMessage);
    }

    @Test
    public void test01() {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        //redisService.set("test",new com.vfd.demo.bean.Test("a","string",3,true,list),30);
        System.out.println(redisService.get("test"));
    }

}
