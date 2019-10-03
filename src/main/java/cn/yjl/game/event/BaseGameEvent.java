package cn.yjl.game.event;

import cn.yjl.game.service.GameService;
import cn.yjl.game.util.AppUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class BaseGameEvent extends ApplicationEvent {

    private String requestUser;

    private int gameId;

    public BaseGameEvent(GameService source) {
        super(source);
    }

    @Override
    public GameService getSource() {
        return AppUtil.autoCast(this.source);
    }
}
