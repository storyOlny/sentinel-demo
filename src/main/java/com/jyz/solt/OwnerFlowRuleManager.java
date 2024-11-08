package com.jyz.solt;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.metric.MetricTimerListener;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OwnerFlowRuleManager {
    private static volatile Map<String, List<FlowRule>> flowRules = new HashMap();
    private static final OwnerFlowRuleManager.FlowPropertyListener LISTENER = new OwnerFlowRuleManager.FlowPropertyListener();
    private static SentinelProperty<List<FlowRule>> currentProperty = new DynamicSentinelProperty();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1, new NamedThreadFactory("sentinel-metrics-record-task", true));

    public OwnerFlowRuleManager() {
    }

    private static void startMetricTimerListener() {
        long flushInterval = SentinelConfig.metricLogFlushIntervalSec();
        if (flushInterval <= 0L) {
            RecordLog.info("[FlowRuleManager] The MetricTimerListener isn't started. If you want to start it, please change the value(current: {}) of config({}) more than 0 to start it.", new Object[]{flushInterval, "csp.sentinel.metric.flush.interval"});
        } else {
            SCHEDULER.scheduleAtFixedRate(new MetricTimerListener(), 0L, flushInterval, TimeUnit.SECONDS);
        }
    }

    public static void register2Property(SentinelProperty<List<FlowRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            RecordLog.info("[FlowRuleManager] Registering new property to flow rule manager", new Object[0]);
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    public static List<FlowRule> getRules() {
        List<FlowRule> rules = new ArrayList();
        Iterator var1 = flowRules.entrySet().iterator();

        while (var1.hasNext()) {
            Map.Entry<String, List<FlowRule>> entry = (Map.Entry) var1.next();
            rules.addAll((Collection) entry.getValue());
        }

        return rules;
    }

    public static void loadRules(List<FlowRule> rules) {
        currentProperty.updateValue(rules);
    }

    static Map<String, List<FlowRule>> getFlowRuleMap() {
        return flowRules;
    }

    public static boolean hasConfig(String resource) {
        return flowRules.containsKey(resource);
    }

    public static boolean isOtherOrigin(String origin, String resourceName) {
        if (StringUtil.isEmpty(origin)) {
            return false;
        } else {
            List<FlowRule> rules = (List) flowRules.get(resourceName);
            if (rules != null) {
                Iterator var3 = rules.iterator();

                while (var3.hasNext()) {
                    FlowRule rule = (FlowRule) var3.next();
                    if (origin.equals(rule.getLimitApp())) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    static {
        currentProperty.addListener(LISTENER);
        startMetricTimerListener();
    }

    private static final class FlowPropertyListener implements PropertyListener<List<FlowRule>> {
        private FlowPropertyListener() {
        }

        @Override
        public synchronized void configUpdate(List<FlowRule> value) {
            Map<String, List<FlowRule>> rules = FlowRuleUtil.buildFlowRuleMap(value);
            if (rules != null) {
                OwnerFlowRuleManager.flowRules = rules;
            }

            RecordLog.info("[FlowRuleManager] Flow rules received: {}", new Object[]{rules});
        }

        @Override
        public synchronized void configLoad(List<FlowRule> conf) {
            Map<String, List<FlowRule>> rules = FlowRuleUtil.buildFlowRuleMap(conf);
            if (rules != null) {
                OwnerFlowRuleManager.flowRules = rules;
            }

            RecordLog.info("[FlowRuleManager] Flow rules loaded: {}", new Object[]{rules});
        }
    }
}
