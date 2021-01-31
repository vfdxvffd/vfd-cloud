package com.vfd.demo.messaging;

import com.vfd.demo.service.RedisService;
import com.vfd.demo.utils.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @PackageName: com.vfd.demo.messaging
 * @ClassName: AccountConsumer
 * @Description: 处理有关账号信息的消息队列
 * @author: vfdxvffd
 * @date: 1/14/21 7:55 PM
 */
@Component
public class AccountConsumer {

    @Autowired
    private RedisService redisService;
    @Autowired
    SendMessage sendMessage;

    //记录日志
    Logger logger = LoggerFactory.getLogger(getClass());

    //向redis中存入验证码或者UUID
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,     //不指定名字则为临时队列
                    exchange = @Exchange(name = "account.topic", type = "topic"),     //交换机名字和类型
                    key = {"account.sendCode","account.sendUUid"}
            )
    })
    public void saveIntoCache (Map<String, String> map) {
        //redisService.set(email+":verificationCode",verificationCode,60);
        redisService.set(map.get("key"),map.get("val"),Integer.parseInt(map.get("time")));
        logger.info("redis存入缓存成功，缓存内容：" + map.entrySet());
    }

    //删除缓存中的验证码或者UUID
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue, //如果不指定名字则为临时队列
                    exchange = @Exchange(name = "account.topic", type = "topic"),
                    key = {"account.register","account.reset"}
            )
    })
    public void delFromCache (Map<String, String> map) {
        redisService.del(map.get("del"));
        logger.info("redis缓存删除成功，删除的信息：" + map.entrySet());
    }

    //记录日志信息
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue, //如果不指定名字则为临时队列
                    exchange = @Exchange(name = "account.topic", type = "topic"),
                    key = {"account.*"}
            )
    })
    public void logSuccess (Map<String, String> map) {
        logger.info(map.get("log"));
    }

    //发送邮件
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue, //如果不指定名字则为临时队列
                    exchange = @Exchange(name = "account.topic", type = "topic"),
                    key = {"account.sendCode","account.sendUUid"}
            )
    })
    public void sendMsg (Map<String, String> map) {
        if ("true".equals(map.get("html"))) {
            sendMessage.sendMeg(map.get("source_email"),map.get("dest_email"),map.get("title"),map.get("context"),true);
        } else {
            sendMessage.sendMeg(map.get("source_email"),map.get("dest_email"),map.get("title"),map.get("context"));
        }
        logger.info("邮件发送成功，邮件内容如下：" + map.entrySet());
    }
}