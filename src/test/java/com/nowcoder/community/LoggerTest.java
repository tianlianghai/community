package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class LoggerTest {

    private static final Logger log= LoggerFactory.getLogger(LoggerTest.class);

    @Test
    public void loggerTest(){
        System.out.println(log.getName());
        log.debug("debug log");
        log.info("info log");
        log.warn("warn log");
        log.error("error log");

    }
}
