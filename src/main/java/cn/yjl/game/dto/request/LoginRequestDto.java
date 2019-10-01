package cn.yjl.game.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(fluent = true)
public class LoginRequestDto extends BaseRequestDto {
    private String password;
}
