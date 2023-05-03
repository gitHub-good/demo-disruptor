package org.example.disruptor;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
/**
 * <p>
 *  事件转换器
 * </p>
 *
 * @author liangliang.xu
 * @since 2023/5/3 16:37
 */
class LongEventProducerWithTranslator {
        //一个translator可以看做一个事件初始化器，publicEvent方法会调用它，填充Event
        private static final EventTranslatorOneArg<LongEvent, Long> TRANSLATOR = (event, sequence, data) -> event.setValue(data);

        private final RingBuffer<LongEvent> ringBuffer;

        public LongEventProducerWithTranslator(RingBuffer<LongEvent> ringBuffer) {
            this.ringBuffer = ringBuffer;
        }

        public void onData(Long data) {

            System.out.println("生产一个数据：" + data);
            ringBuffer.publishEvent(TRANSLATOR, data);
        }
    }