package org.example.disruptor;

import com.lmax.disruptor.WorkHandler;

/**
 * 类似于消费者
 * disruptor会回调此处理器的方法
 */
class LongEventWorkHandler2 implements WorkHandler<LongEvent> {
    private final Integer number;

    public LongEventWorkHandler2(Integer number) {
        this.number = number;
    }

    @Override
    public void onEvent(LongEvent event) throws Exception {
        System.out.println(number+"-LongEventWorkHandler2:"+event.getValue());
    }
}