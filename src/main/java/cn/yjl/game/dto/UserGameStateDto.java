package cn.yjl.game.dto;

import cn.yjl.game.enumeration.UserGameStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class UserGameStateDto {
    private String userId;
    private UserGameStatusEnum status;
    private List<CardWrapDto> gameCards;
    private List<CardWrapDto> sentCards;
    private List<CardWrapDto> unsentCards;
}
