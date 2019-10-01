package cn.yjl.game.mapper;

import cn.yjl.game.pojo.CardPojo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CardMapper {

    @Select("select cardId, colorId, point, number, label from T_CARD")
    List<CardPojo> getAllCards();
}
