package com.example.demo.services;

import com.example.demo.model.Employee;
import com.example.demo.model.EmployeeWithSalary;
import org.springframework.batch.item.ItemProcessor;
import org.sql2o.Sql2o;

import java.math.BigDecimal;

public class EmployeeProcessor implements ItemProcessor<Employee, EmployeeWithSalary> {

    Sql2o sql2o;

    public EmployeeProcessor(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public EmployeeWithSalary process(Employee item) throws Exception {

        EmployeeWithSalary employeeWithSalary = new EmployeeWithSalary();
        employeeWithSalary.setEmp_no(item.getEmp_no());
        employeeWithSalary.setFirst_name(item.getFirst_name());
        employeeWithSalary.setLast_name(item.getLast_name());
        employeeWithSalary.setBirth_date(item.getBirth_date());
        employeeWithSalary.setHire_date(item.getHire_date());
        employeeWithSalary.setGender(item.getGender());
        employeeWithSalary.setSalary(new BigDecimal(0));
        return employeeWithSalary;
    }
}
