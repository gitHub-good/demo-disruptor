package org.example.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LongEventSimpleDemo {

    public static void main(String[] args) throws Exception {
        //简单Disruptor
        testSimpleDisruptor();

        //简单Disruptor - 事件转换器
        testSimpleDisruptorWithTranslator();

        //简单测试 - Lambda
        testSimpleDisruptorWithLambda();
    }

    /**
     * 简单Disruptor demo
     * @throws InterruptedException
     */
    public static void testSimpleDisruptor() throws InterruptedException {
        //消费者线程池
        Executor executor = Executors.newCachedThreadPool();

        // 事件工厂
        LongEventFactory eventFactory = new LongEventFactory();

        // 环形队列大小，2的指数
        int bufferSize = 1024;

        // 构造  分裂者 （事件分发者）
        Disruptor<LongEvent> disruptor = new Disruptor<>(eventFactory, bufferSize, executor);

        // 连接 消费者 处理器
        disruptor.handleEventsWith(new LongEventHandler());

        // 开启 分裂者（事件分发）
        disruptor.start();

        // 获取环形队列，用于生产 事件
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        LongEventProducer producer = new LongEventProducer(ringBuffer);

        for (long i = 0; true; i++) {
            //发布事件
            producer.onData(i);
            Thread.sleep(1000);
        }
    }

    /**
     * 简单测试 - 事件转换器
     * @throws InterruptedException
     */
    public static void testSimpleDisruptorWithTranslator() throws InterruptedException {
        // 消费者线程池
        Executor executor = Executors.newCachedThreadPool();
        // 事件工厂
        LongEventFactory eventFactory = new LongEventFactory();
        // 环形队列大小，2的指数
        int bufferSize = 1024;

        // 构造  分裂者 （事件分发者）
        Disruptor<LongEvent> disruptor = new Disruptor<>(eventFactory, bufferSize, executor);

        // 连接 消费者 处理器
        disruptor.handleEventsWith(new LongEventHandler());
        // 开启 分裂者（事件分发）
        disruptor.start();

        // 获取环形队列，用于生产 事件
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        LongEventProducerWithTranslator producer = new LongEventProducerWithTranslator(ringBuffer);

        for (long i = 0; true; i++) {
            //发布事件
            producer.onData(i);
            Thread.sleep(1000);
        }
    }

    /**
     * 简单测试 - Lambda
     * @throws InterruptedException
     */
    public static void testSimpleDisruptorWithLambda() throws InterruptedException {
        // 消费者线程池
        Executor executor = Executors.newCachedThreadPool();
        // 环形队列大小，2的指数
        int bufferSize = 1024;

        // 构造  分裂者 （事件分发者）
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, executor);

        // 连接 消费者 处理器
        // 可以使用lambda来注册一个EventHandler
        disruptor.handleEventsWith((event, sequence, endOfBatch) -> System.out.println("Event: " + event.getValue()));
        // 开启 分裂者（事件分发）
        disruptor.start();

        // 获取环形队列，用于生产 事件
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        LongEventProducerWithTranslator producer = new LongEventProducerWithTranslator(ringBuffer);

        for (long i = 0; true; i++) {
            //发布事件
            producer.onData(i);
            Thread.sleep(1000);
        }
    }

    public static void testSimpleDisruptorWithMethodRef() throws InterruptedException {
        // 消费者线程池
        Executor executor = Executors.newCachedThreadPool();
        // 环形队列大小，2的指数
        int bufferSize = 1024;

        // 构造  分裂者 （事件分发者）
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, executor);

        // 连接 消费者 处理器
        // 可以使用lambda来注册一个EventHandler
        disruptor.handleEventsWith(LongEventSimpleDemo::handleEvent);
        // 开启 分裂者（事件分发）
        disruptor.start();

        // 获取环形队列，用于生产 事件
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        LongEventProducerWithTranslator producer = new LongEventProducerWithTranslator(ringBuffer);

        for (long i = 0; true; i++) {
            //发布事件
            producer.onData(i);
            Thread.sleep(1000);
        }
    }

    public static void handleEvent(LongEvent event, long sequence, boolean endOfBatch) {
        System.out.println(event.getValue());
    }

}
