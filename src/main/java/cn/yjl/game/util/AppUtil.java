package cn.yjl.game.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppUtil {

	@SuppressWarnings("unchecked")
	public static <T> T autoCast(Object obj) {
		return (T) obj;
	}
}
