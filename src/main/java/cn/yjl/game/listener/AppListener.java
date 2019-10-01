package cn.yjl.game.listener;

import cn.yjl.game.event.DataInitCompleteEvent;
import cn.yjl.game.mapper.SqlMapper;
import cn.yjl.game.util.IoUtil;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Stream;

@Component
@Slf4j
public class AppListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private SqlMapper sqlMapper;

//    @Autowired
//    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent applicationEvent) {
        InputStream inputStream = this.getClass().getResourceAsStream("/ddl/create_table.sql");
        byte[] bytes = IoUtil.readBytesInputStream(inputStream);
        Integer tableCount = this.sqlMapper.searchOneSql("select count(*) from SYS.SYSTABLES where TABLENAME = 'T_CARD'");
        if (tableCount == 0) {
            String sql = new String(bytes, StandardCharsets.UTF_8);
            Stream.of(sql.split(";")).forEach(this.sqlMapper::executeSql);
        }
        this.applicationContext.publishEvent(new DataInitCompleteEvent(this));
    }
}
