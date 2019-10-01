package cn.yjl.game.listener;

import cn.yjl.game.event.DataInitCompleteEvent;
import cn.yjl.game.mapper.SqlMapper;
import cn.yjl.game.util.IoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

@Component
@Slf4j
public class AppListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private SqlMapper sqlMapper;

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
