package cn.yjl.game.dto.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class JoinGameEventDto extends BaseEventDto {
    private List<String> userList;
}
