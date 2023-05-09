package org.example.disruptor;

import lombok.Data;
/**
 * <p>
 *  自定义事件
 * </p>
 *
 * @author liangliang.xu
 * @since 2023/5/9 16:57
 */
@Data
public class LongEvent {
    private long value;
}