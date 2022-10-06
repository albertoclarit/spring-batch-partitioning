package com.example.demo.services;

import com.example.demo.model.EmployeeWithSalary;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class EmployeeWithSalaryWriterConfig {
    private static final String INSERT_STATEMENT = "INSERT INTO employees.employees_salary " +
            "(emp_no, birth_date, first_name, last_name, gender, hire_date, salary) " +
            "VALUES(:emp_no, :birth_date, :first_name,:last_name, :gender, :hire_date, :salary);";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Bean
    public ItemWriter<EmployeeWithSalary> employeeWithSalaryItemWriter() {

        JdbcBatchItemWriter<EmployeeWithSalary> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(jdbcTemplate.getDataSource());
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider());
        writer.setSql(INSERT_STATEMENT);


        return writer;
    }

}
