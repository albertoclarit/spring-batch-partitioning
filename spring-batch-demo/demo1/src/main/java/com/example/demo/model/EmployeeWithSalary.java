package com.example.demo.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmployeeWithSalary {

    private String emp_no;
    private String birth_date;
    private String first_name;
    private String last_name;
    private String hire_date;
    private String gender;
    private BigDecimal salary;


}
