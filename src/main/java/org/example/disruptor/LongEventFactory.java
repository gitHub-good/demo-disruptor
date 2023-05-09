package org.example.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * <p>
 *  事件工厂
 * </p>
 *
 * @author liangliang.xu
 * @since 2023/5/9 16:58
 */
class LongEventFactory implements EventFactory<LongEvent> {
        @Override
        public LongEvent newInstance() {
            return new LongEvent();
        }
    }
