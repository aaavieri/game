package cn.yjl.game.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class OnceSendCardDto {
    private String userId;
    private List<CardWrapDto> sentCards;
}
