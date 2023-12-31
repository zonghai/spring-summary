package com.spring.rabbitmq.controller;

import com.spring.rabbitmq.constant.Constants;
import com.spring.rabbitmq.producer.DelayProducer;
import com.spring.rabbitmq.producer.TransactionProducer;
import com.spring.rabbitmq.producer.TtlProducer;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @auth 十三先生
 * @date 2023/12/14
 * @desc
 */
@RestController
public class SendMessageController {

    @Resource
    private TransactionProducer transactionProducer;
    @Resource
    private TtlProducer ttlProducer;
    @Resource
    private DelayProducer delayProducer;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/pull")
    public String pull(String message) {
        rabbitTemplate.convertAndSend(Constants.PULL_EXCHANGE, Constants.PULL_ROUTING, message);
        return "success";
    }

    @PostMapping("/single")
    public String single(String message) {
        //带有发送确认的方式。
        CorrelationData correlationData = new CorrelationData(message);
        rabbitTemplate.convertAndSend(Constants.SINGLE_EXCHANGE, Constants.SINGLE_ROUTING, message, correlationData);
        return "success";
    }

    //发送事务消息
    @PostMapping("/transaction")
    public String transaction(String message) {
        //spring.rabbitmq.publisher-confirms一定要配置为false，否则会与事务处理相冲突，启动时会报异常
        //事务消息成功保证消息一定发送到broker。
        transactionProducer.sendMessage(message);
        return "success";
    }

    @PostMapping("/direct")
    public String direct(String message) {
        //带有发送确认的方式。
        CorrelationData correlationData = new CorrelationData(message);
        rabbitTemplate.convertAndSend(Constants.EXCHANGE, Constants.ROUTING, message, correlationData);
        return "success";
    }

    @PostMapping("/fanout")
    public String fanout(String message) {
        CorrelationData correlationData = new CorrelationData(message);
        rabbitTemplate.convertAndSend(Constants.FANOUT_EXCHANGE, "", message, correlationData);
        return "success";
    }

    @PostMapping("/topic")
    public String topic(String message) {
        CorrelationData correlationData = new CorrelationData(message);
        rabbitTemplate.convertAndSend(Constants.TOPIC_EXCHANGE, "topic.test." + message, message, correlationData);
        return "success";
    }

    @PostMapping("/header")
    public String header(String message) {
        Message nameMsg = MessageBuilder.withBody(message.getBytes()).setHeader("name", "aaa").build();
        CorrelationData correlationData = new CorrelationData(message);
        rabbitTemplate.convertAndSend(Constants.HEADER_EXCHANGE, "" + message, nameMsg, correlationData);
        return "success";
    }

    @PostMapping("/ttl")
    public String ttl(String message) {
        ttlProducer.sendTtlMessage(message);
        return "success" + message;
    }

    @PostMapping("/delay")
    public String delay(String message) {
        delayProducer.sendDelayMessage(message);
        return "success";
    }

    @PostMapping("/quorum")
    public String quorum(String message) {
        CorrelationData correlationData = new CorrelationData(message);
        rabbitTemplate.convertAndSend(Constants.QUORUM_MSG_EXCHANGE, Constants.QUORUM_MSG_ROUTING, message, correlationData);
        return "success";
    }

    @PostMapping("/lazy")
    public String lazy(String message) {
        //惰性队列，消息现存磁盘，需要消费时再加载到内存中。
        CorrelationData correlationData = new CorrelationData(message);
        rabbitTemplate.convertAndSend(Constants.LAZY_MSG_EXCHANGE, Constants.LAZY_MSG_ROUTING, message, correlationData);
        return "success";
    }


    @PostMapping("/sync")
    public String sync(String message) {
        //带有发送确认的方式。result为Listener的返回值。
        CorrelationData correlationData = new CorrelationData(message);
        Object result = rabbitTemplate.convertSendAndReceive(Constants.EXCHANGE, Constants.ROUTING, message, correlationData);
        return "success" + result.toString();
    }


}
