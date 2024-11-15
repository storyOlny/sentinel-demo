package com.jyz.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.jyz.solt.OwnerFlowRuleManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
public class DemoController {

    @Autowired
    private RestTemplate template;

    @GetMapping("/test")
//    @SentinelResource(value = "localhost-8080", fallback = "fallback")
    public String test() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("localhost-8080");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Set limit QPS to 20.
        rule.setCount(0);
        rules.add(rule);
        OwnerFlowRuleManager.loadRules(rules);
        template.getForEntity("http://localhost:8080/test2", String.class);
        return "hello world";
    }

    // value：资源名称 必须的
    // entryType：资源类型，默认为OUt，可选为IN,发送请求是OUt，接受请求是IN.消费者发送请求是OUT


    @GetMapping("/get")

    public String get() {

        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("localhost-8090");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Set limit QPS to 20.
        rule.setCount(0);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
//        int i = 1 / 0;

//        template.getForEntity("http://localhost:8080/test2", String.class);
        return getValue();
    }

    @SentinelResource(value = "localhost-8090", blockHandler = "blockHandler")
    public String getValue() {
        return "hello world";
    }

    public String fallback() {
        return "fallback";
    }

    public String blockHandler(BlockException blockException) {
        System.out.println(blockException.getMessage());
        return "blockHandler";
    }


}
