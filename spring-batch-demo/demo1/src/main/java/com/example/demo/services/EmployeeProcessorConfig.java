package com.example.demo.services;

import com.example.demo.model.Employee;
import com.example.demo.model.EmployeeWithSalary;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sql2o.Sql2o;

@Configuration
public class EmployeeProcessorConfig {

    @Autowired
    Sql2o sql2o;


    @Bean
    ItemProcessor<Employee, EmployeeWithSalary> employeeWithSalaryItemProcessor() {
        return  new EmployeeProcessor(sql2o);
    }
}
