package cn.yjl.game.algorithm;

import cn.yjl.game.dto.CardWrapDto;

import java.util.List;

public interface AlgIf<T extends AlgIf> {
	
	int compare(T t);
	
	int getPriority();
	
	int getType();
	
	boolean isLegal(List<CardWrapDto> cards);
}
