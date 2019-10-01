package cn.yjl.game.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface SqlMapper {

    @Update("${sql}")
    void executeSql(@Param("sql")final String sql);

    @Select("${sql}")
    <T> T searchOneSql(@Param("sql")final String sql);

    @Select("${sql}")
    <T> List<T> searchListSql(@Param("sql")final String sql);
}
