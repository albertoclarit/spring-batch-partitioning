package com.example.demo.services;

import com.example.demo.mapper.EmployeeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
public class EmployeeReaderConfig {

    private static final String QUERY = "Select * from employees where  emp_no MOD 5 = :index and emp_no  < 300000"; //

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Bean
    @StepScope
    public SynchronizedItemStreamReader employeeReader(@Value("#{stepExecutionContext[partition_index]}") String partition_index ){

        JdbcCursorItemReader reader = new JdbcCursorItemReader();
        reader.setDataSource(jdbcTemplate.getDataSource());
        reader.setSql(QUERY.replace(":index",partition_index));
        log.info("Query:" +  QUERY.replace(":index",partition_index));
        reader.setFetchSize(1000);
        reader.setRowMapper(new EmployeeMapper());
        SynchronizedItemStreamReader wrapperReader = new SynchronizedItemStreamReader();
        wrapperReader.setDelegate(reader);
        return wrapperReader;
    }

}
