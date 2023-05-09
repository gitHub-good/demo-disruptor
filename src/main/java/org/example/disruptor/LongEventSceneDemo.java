package org.example.disruptor;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * <p>
 *  场景使用Demo
 * </p>
 *
 * @author liangliang.xu
 * @since 2023/5/3 16:41
 */
public class LongEventSceneDemo {

    public static void main(String[] args) throws InterruptedException {
        //自定义单生产者 对应 多个消费者同时消费
//        testSimpleProducerDisruptorWithMethodRef();

//        //多个生产者对应多个消费者
//        testMultiProducerDisruptorWithMethodRef();
//
//        //串行消费者场景
//        testMiltiSerialConsumerDisruptorWithMethodRef();
//
//        //先并行处理再串行处理
//        testCurrentThenSerialConsumerDisruptorWithMethodRef();
//
//        //多组隔离并行组内串行处理
//        testlinkSerialConsumerDisruptorWithMethodRef();
//
//        //多组隔离并行WorkHandler处理
//        testIsolateDisruptorWithMethodRef();
//
//        //多组串行处理
//        testChannelModelDisruptorWithMethodRef();
//
//        //六边形消费处理
        testHexagonConsumerDisruptorWithMethodRef();
    }


    public static void handleEvent1(LongEvent event, long sequence, boolean endOfBatch) {
        System.out.println("handleEvent1:"+event.getValue());
    }

    public static void handleEvent2(LongEvent event, long sequence, boolean endOfBatch) {
        System.out.println("handleEvent2:"+event.getValue());
    }

    public static void handleEvent3(LongEvent event, long sequence, boolean endOfBatch) {
        System.out.println("handleEvent3:"+event.getValue());
    }

    public static void handleEvent4(LongEvent event, long sequence, boolean endOfBatch) {
        System.out.println("handleEvent4:"+event.getValue());
    }

    /**
     * 自定义单生产者 对应 多个消费者
     * @throws InterruptedException
     */
    public static void testSimpleProducerDisruptorWithMethodRef() throws InterruptedException {
        // 消费者线程池
        Executor executor = Executors.newCachedThreadPool();
        // 环形队列大小，2的指数
        int bufferSize = 1024;
        // 构造  分裂者 （事件分发者）
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize,
                executor,
                ProducerType.SINGLE, //单生产者
                new YieldingWaitStrategy()); //注意使用对应的等待策略，使用不当会出现服务CPU飙高
        // 连接 消费者 处理器
        // 可以使用lambda来注册多个EventHandler消费者同时消费
        disruptor.handleEventsWith(
                LongEventSceneDemo::handleEvent1,
                LongEventSceneDemo::handleEvent1,
                LongEventSceneDemo::handleEvent1
        );
        // 开启 分裂者（事件分发）
        disruptor.start();
        // 获取环形队列，用于生产 事件
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        //1生产者，并发生产数据
        LongEventProducerWithTranslator producer = new LongEventProducerWithTranslator(ringBuffer);
        publishEvent(producer);
    }


    /**
     * 多个生产者对应多个消费者
     * @throws InterruptedException
     */
    public static void testMultiProducerDisruptorWithMethodRef() throws InterruptedException {
        // 消费者线程池
        Executor executor = Executors.newCachedThreadPool();
        // 环形队列大小，2的指数
        int bufferSize = 1024;
        // 构造  分裂者 （事件分发者）
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize,
                executor,
                ProducerType.MULTI,  //多个生产者
                new YieldingWaitStrategy());
        // 连接 消费者 处理器
        // 可以使用WorkerPool 注册WorkHandler
        disruptor.handleEventsWithWorkerPool(new LongEventWorkHandler(1), new LongEventWorkHandler(2));
        // 开启 分裂者（事件分发）
        disruptor.start();
        // 获取环形队列，用于生产 事件
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        //3生产者，并发生产数据
        for (int l = 0; l < 3; l++) {
            LongEventProducerWithTranslator producer = new LongEventProducerWithTranslator(ringBuffer);

            Thread thread = new Thread(() -> {
                for (long i = 0; true; i++) {
                    //发布事件
                    producer.onData(i);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.setName("producer thread " + l);
            thread.start();
        }
        Thread.sleep(5000);

    }

    /**
     * 串行消费者场景
     * @throws InterruptedException
     */
    public static void testMiltiSerialConsumerDisruptorWithMethodRef() throws InterruptedException {
        // 消费者线程池
        Executor executor = Executors.newCachedThreadPool();
        // 环形队列大小，2的指数
        int bufferSize = 1024;
        // 构造  分裂者 （事件分发者）
        Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(LongEvent::new, bufferSize,
                executor,
                ProducerType.SINGLE,
                new YieldingWaitStrategy());
        // 连接 消费者 处理器
        // 消费者handleEvent1  ---》handleEvent2 ---》handleEvent3
        disruptor.handleEventsWith(LongEventSceneDemo::handleEvent1)
                .then(LongEventSceneDemo::handleEvent2)
                .then(LongEventSceneDemo::handleEvent3);
        // 开启 分裂者（事件分发）
        disruptor.start();
        // 获取环形队列，用于生产 事件
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        //1生产者，并发生产数据
        LongEventProducerWithTranslator producer = new LongEventProducerWithTranslator(ringBuffer);
        publishEvent(producer);
    }


    /**
     *  先并行处理再串行处理
     * @throws InterruptedException
     */
    public static void testCurrentThenSerialConsumerDisruptorWithMethodRef() throws InterruptedException {
        // 消费者线程池
        Executor executor = Executors.newCachedThreadPool();
        // 环形队列大小，2的指数
        int bufferSize = 1024;
        // 构造  分裂者 （事件分发者）
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize,
                executor,
                ProducerType.SINGLE,
                new YieldingWaitStrategy());
        // 连接 消费者 处理器
        // 并行处理handleEvent1和handleEvent2 再串行handleEvent3
        disruptor.handleEventsWith(LongEventSceneDemo::handleEvent1, LongEventSceneDemo::handleEvent2)
                .then(LongEventSceneDemo::handleEvent3);
        // 开启 分裂者（事件分发）
        disruptor.start();
        // 获取环形队列，用于生产 事件
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        //1生产者，并发生产数据
        LongEventProducerWithTranslator producer = new LongEventProducerWithTranslator(ringBuffer);
        publishEvent(producer);
    }

    private static void publishEvent(LongEventProducerWithTranslator producer) throws InterruptedException {
        Thread thread = new Thread(() -> {
            for (long i = 0; true; i++) {
                //发布事件
                producer.onData(i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        Thread.sleep(5000);
    }

    /**
     * 多组并行组内串行处理
     * @throws InterruptedException
     */
    public static void testlinkSerialConsumerDisruptorWithMethodRef() throws InterruptedException {
        // 消费者线程池
        Executor executor = Executors.newCachedThreadPool();
        // 环形队列大小，2的指数
        int bufferSize = 1024;
        // 构造  分裂者 （事件分发者）
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize,
                executor,
                ProducerType.SINGLE,
                new YieldingWaitStrategy());
        // 连接 消费者 处理器
        // 多组并行组内串行处理
        disruptor.handleEventsWith(LongEventSceneDemo::handleEvent1)
                .then(LongEventSceneDemo::handleEvent2);

        disruptor.handleEventsWith(LongEventSceneDemo::handleEvent3)
                .then(LongEventSceneDemo::handleEvent4);
        // 开启 分裂者（事件分发）
        disruptor.start();
        // 获取环形队列，用于生产 事件
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        //1生产者，并发生产数据
        LongEventProducerWithTranslator producer = new LongEventProducerWithTranslator(ringBuffer);
        publishEvent(producer);
    }


    /**
     * 多组隔离并行WorkHandler处理
     * @throws InterruptedException
     */
    public static void testIsolateDisruptorWithMethodRef() throws InterruptedException {
        // 消费者线程池
        Executor executor = Executors.newCachedThreadPool();
        // 环形队列大小，2的指数
        int bufferSize = 1024;
        // 构造  分裂者 （事件分发者）
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize,
                executor,
                ProducerType.SINGLE,
                new YieldingWaitStrategy());
        // 连接 消费者 处理器
        // 多组隔离并行WorkHandler处理
        disruptor.handleEventsWithWorkerPool(new LongEventWorkHandler(1), new LongEventWorkHandler(2));
        disruptor.handleEventsWithWorkerPool(new LongEventWorkHandler2(1), new LongEventWorkHandler2(2));
        // 开启 分裂者（事件分发）
        disruptor.start();
        // 获取环形队列，用于生产 事件
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        //1生产者，并发生产数据
        LongEventProducerWithTranslator producer = new LongEventProducerWithTranslator(ringBuffer);
        publishEvent(producer);
    }

    /**
     * 多组串行处理
     * @throws InterruptedException
     */
    public static void testChannelModelDisruptorWithMethodRef() throws InterruptedException {
        // 消费者线程池
        Executor executor = Executors.newCachedThreadPool();
        // 环形队列大小，2的指数
        int bufferSize = 1024;
        // 构造  分裂者 （事件分发者）
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize,
                executor,
                ProducerType.SINGLE,
                new YieldingWaitStrategy());
        // 连接 消费者 处理器
        // 多组串行处理
        disruptor.handleEventsWithWorkerPool(new LongEventWorkHandler(1), new LongEventWorkHandler(2))
                .thenHandleEventsWithWorkerPool(new LongEventWorkHandler2(1), new LongEventWorkHandler2(2));
        // 开启 分裂者（事件分发）
        disruptor.start();
        // 获取环形队列，用于生产 事件
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        //1生产者，并发生产数据
        LongEventProducerWithTranslator producer = new LongEventProducerWithTranslator(ringBuffer);
       publishEvent(producer);
    }


    /**
     * 六边形消费处理
     * @throws InterruptedException
     */
    public static void testHexagonConsumerDisruptorWithMethodRef() throws InterruptedException {
        // 消费者线程池
        Executor executor = Executors.newCachedThreadPool();
        // 环形队列大小，2的指数
        int bufferSize = 1024;
        // 构造  分裂者 （事件分发者）
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize,
                executor,
                ProducerType.SINGLE,
                new YieldingWaitStrategy());

        EventHandler<LongEvent> consumer1 = new LongEventHandlerWithName("consumer 1");
        EventHandler<LongEvent> consumer2 = new LongEventHandlerWithName("consumer 2");
        EventHandler<LongEvent> consumer3 = new LongEventHandlerWithName("consumer 3");
        EventHandler<LongEvent> consumer4 = new LongEventHandlerWithName("consumer 4");
        EventHandler<LongEvent> consumer5 = new LongEventHandlerWithName("consumer 5");
        // 连接 消费者 处理器
        // 消费编排处理
        disruptor.handleEventsWith(consumer1, consumer2);
        disruptor.after(consumer1).handleEventsWith(consumer3);
        disruptor.after(consumer2).handleEventsWith(consumer4);
        disruptor.after(consumer3, consumer4).handleEventsWith(consumer5);
        // 开启 分裂者（事件分发）
        disruptor.start();
        // 获取环形队列，用于生产 事件
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        //1生产者，并发生产数据
        LongEventProducerWithTranslator producer = new LongEventProducerWithTranslator(ringBuffer);
        publishEvent(producer);
    }

}
