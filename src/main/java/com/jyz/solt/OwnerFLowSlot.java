package com.jyz.solt;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleChecker;
import com.alibaba.csp.sentinel.spi.Spi;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Function;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Spi(order = -4000)
public class OwnerFLowSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

    private final FlowRuleChecker checker;
    private final Function<String, Collection<FlowRule>> ruleProvider;

    public OwnerFLowSlot() {
        this(new FlowRuleChecker());
    }

    OwnerFLowSlot(FlowRuleChecker checker) {
        this.ruleProvider = new Function<String, Collection<FlowRule>>() {
            @Override
            public Collection<FlowRule> apply(String resource) {
                Map<String, List<FlowRule>> flowRules = OwnerFlowRuleManager.getFlowRuleMap();
                return (Collection) flowRules.get(resource);
            }
        };
        AssertUtil.notNull(checker, "flow checker should not be null");
        this.checker = checker;
    }

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count, boolean prioritized, Object... args) throws Throwable {
        this.checkFlow(resourceWrapper, context, node, count, prioritized);
        this.fireEntry(context, resourceWrapper, node, count, prioritized, args);
    }

    void checkFlow(ResourceWrapper resource, Context context, DefaultNode node, int count, boolean prioritized) throws BlockException {
        this.checker.checkFlow(this.ruleProvider, resource, context, node, count, prioritized);
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        this.fireExit(context, resourceWrapper, count, args);
    }
}
