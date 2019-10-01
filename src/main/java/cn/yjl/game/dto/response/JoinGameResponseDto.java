package cn.yjl.game.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class JoinGameResponseDto extends BaseResponseDto {
    private List<String> userList;
}
