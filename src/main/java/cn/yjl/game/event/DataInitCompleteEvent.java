package cn.yjl.game.event;

import org.springframework.context.ApplicationEvent;

public class DataInitCompleteEvent extends ApplicationEvent {

    public DataInitCompleteEvent(Object source) {
        super(source);
    }
}
