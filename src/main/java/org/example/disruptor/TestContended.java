package org.example.disruptor;

import lombok.Data;
import sun.misc.Contended;

public class TestContended {
    public static void main(String[] args) throws InterruptedException {
        testDemo(new TestDemo());
        testContended(new TestDemoContended());
    }

    private static void testDemo(TestDemo testDemo) throws InterruptedException {
        long start  = System.currentTimeMillis();
        Thread t1 = new Thread(() -> {
            for(int i=0;i<1000000000;i++){
                testDemo.x++;
            }
        });

        Thread t2 = new Thread(() -> {
            for(int i=0;i<1000000000;i++){
                testDemo.y++;
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("正常执行时间："+ (System.currentTimeMillis()-start));
        System.out.println(testDemo);
    }

    private static void testContended(TestDemoContended testDemo) throws InterruptedException {
        long start  = System.currentTimeMillis();
        Thread t1 = new Thread(() -> {
            for(int i=0;i<1000000000;i++){
                testDemo.x++;
            }
        });

        Thread t2 = new Thread(() -> {
            for(int i=0;i<1000000000;i++){
                testDemo.y++;
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("解决伪共享执行时间："+ (System.currentTimeMillis()-start));
        System.out.println(testDemo);
    }

    @Contended
    @Data
    static class TestDemoContended{
        long x;
        long y;
    }

    @Data
    static class TestDemo{
        long x;
        long y;
    }
}
