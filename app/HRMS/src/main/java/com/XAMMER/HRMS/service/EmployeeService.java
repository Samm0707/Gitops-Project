// package com.XAMMER.HRMS.service;

// import com.XAMMER.HRMS.model.Employee;
// import com.XAMMER.HRMS.repository.EmployeeRepository;

// import lombok.extern.slf4j.Slf4j;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import java.util.List;
// import java.util.Optional;

// @Slf4j
// @Service
// public class EmployeeService {

//     @Autowired 
//     private EmployeeRepository employeeRepository;

//     public List<Employee> getAllEmployees() {
//         log.info("Fetching all employees");
//         return employeeRepository.findAll();
//     }

//     public Optional<Employee> getEmployeeById(Long id) {
//         log.info("Fetching employee with ID: {}", id);
//         return employeeRepository.findById(id);
//     }

//     public Employee createEmployee(Employee employee) {
//         log.info("Creating employee: {}", employee);
//         return employeeRepository.save(employee);
//     }

//     public Employee updateEmployee(Employee employee) {
//         log.info("Updating employee: {}", employee);
//         return employeeRepository.save(employee);
//     }

//     public void deleteEmployee(Long id) {
//         log.info("Deleting employee with ID: {}", id);
//         employeeRepository.deleteById(id);
//     }
// }
