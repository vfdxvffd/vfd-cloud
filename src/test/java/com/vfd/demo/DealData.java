package com.vfd.demo;

import com.vfd.demo.mapper.UserLoginMapper;
import com.vfd.demo.service.RedisService;
import com.vfd.demo.utils.SendMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;


/**
 * @PackageName: com.vfd.cloud
 * @ClassName: DealData
 * @Description:
 * @author: vfdxvffd
 * @date: 11/26/20 10:13 PM
 */
@SpringBootTest
public class DealData {

    @Autowired
    UserLoginMapper userLoginMapper;

    @Autowired
    RedisService redisService;

    @Autowired
    SendMessage sendMessage;

    @Test
    public void test() {
        redisService.set("key","123456",1000);
        for (int i = 0; i < 20; i++) {
            System.out.println(redisService.get("key"));
        }
    }

    @Test
    public void gen_uuid() {
        UUID uuid = UUID.randomUUID();
        System.out.println(uuid.toString());
    }

    @Test
    public void test01() {
        sendMessage.sendMeg("1649282885@qq.com","2201986113@qq.com","filter","context");
    }
}