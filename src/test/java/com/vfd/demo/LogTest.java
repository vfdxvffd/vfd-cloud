package com.vfd.demo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @PackageName: com.vfd.cloud
 * @ClassName: LogTest
 * @Description:
 * @author: vfdxvffd
 * @date: 11/27/20 9:24 PM
 */
@SpringBootTest
public class LogTest {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void test() {
        logger.trace("这是trace日志...");
        logger.debug("这是debug日志...");
        //springboot默认使用info级别，可以在application.properties中修改
        logger.info("这是info日志...");
        logger.warn("这是warn日志..."); //警告信息
        logger.error("这是error日志...");//错误信息
    }
}