package com.vfd.demo.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @PackageName: com.vfd.demo.messaging
 * @ClassName: LogConsumer
 * @Description:
 * @author: vfdxvffd
 * @date: 1/14/21 8:49 PM
 */
@Component
public class LogConsumer {

    //记录日志
    Logger logger = LoggerFactory.getLogger(getClass());

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,     //不指定名字则为临时队列
                    exchange = @Exchange(name = "log.direct", type = "direct"),     //交换机名字和类型
                    key = {"info"}
            )
    })
    public void logInfo(String msg) {
        logger.info(msg);
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,     //不指定名字则为临时队列
                    exchange = @Exchange(name = "log.direct", type = "direct"),     //交换机名字和类型
                    key = {"warn"}
            )
    })
    public void logWarn(String msg) {
        logger.warn(msg);
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,     //不指定名字则为临时队列
                    exchange = @Exchange(name = "log.direct", type = "direct"),     //交换机名字和类型
                    key = {"error"}
            )
    })
    public void logError(String msg) {
        logger.error(msg);
    }
}