package org.example.disruptor;

import com.lmax.disruptor.WorkHandler;

/**
 * <p>
 *  事件消费业务逻辑 - WorkProcessor事件流程回调该类
 * </p>
 *
 * @author liangliang.xu
 * @since 2023/5/9 17:11
 */
public class LongEventWorkHandler implements WorkHandler<LongEvent> {
    private final Integer number;

    public LongEventWorkHandler(Integer number) {
        this.number = number;
    }

    @Override
    public void onEvent(LongEvent event) throws Exception {
        System.out.println(number+"-LongEventWorkHandler:"+event.getValue());
    }
}