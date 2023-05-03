package org.example.disruptor;

import com.lmax.disruptor.EventFactory;

class LongEventFactory implements EventFactory<LongEvent> {
        @Override
        public LongEvent newInstance() {
            return new LongEvent();
        }
    }
