package com.nowcoder.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)

public class KafkaTest {

    @Autowired
    private KafkaProducer producer;

    @Test
    public void testKafka() throws InterruptedException {

        Thread.sleep(1000*5);
        producer.sendMessage("test1","在吗");
    }

}

@Component
class KafkaProducer{
    @Autowired
    KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic,String content){
        kafkaTemplate.send(topic,content);
    }
}

@Component
class KafkaConsumer{

    @KafkaListener(topics = {"test1"})
    public void handleMessage(ConsumerRecord record){
        System.out.println(record.value());
    }
}
