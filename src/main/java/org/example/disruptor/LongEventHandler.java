package org.example.disruptor;

import com.lmax.disruptor.EventHandler;

/**
 * <p>
 *  事件消费业务逻辑 - BatchEventProcessor事件流程回调该类
 * </p>
 *
 * @author liangliang.xu
 * @since 2023/5/9 16:58
 */
class LongEventHandler implements EventHandler<LongEvent> {
    @Override
    public void onEvent(LongEvent longEvent, long l, boolean b) throws Exception {
        System.out.println(longEvent.getValue());
    }
}