package com.example.demo.mapper;

import com.example.demo.model.Employee;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeMapper implements RowMapper<Employee> {
    @Override
    public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
        Employee employee = new Employee();
        employee.setEmp_no(rs.getString("emp_no"));
        employee.setBirth_date(rs.getString("birth_date"));
        employee.setFirst_name(rs.getString("first_name"));
        employee.setLast_name(rs.getString("last_name"));
        employee.setHire_date(rs.getString("hire_date"));
        employee.setGender(rs.getString("gender"));
        return employee;
    }

}
