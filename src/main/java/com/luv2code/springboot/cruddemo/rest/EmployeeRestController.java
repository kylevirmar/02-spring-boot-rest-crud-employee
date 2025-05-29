package com.luv2code.springboot.cruddemo.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.luv2code.springboot.cruddemo.entity.Employee;
import com.luv2code.springboot.cruddemo.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EmployeeRestController {

    private EmployeeService employeeService;

    private ObjectMapper objectMapper;


    // quick and dirty: inject employee dao
    @Autowired
    public  EmployeeRestController(EmployeeService theEmployeeService, ObjectMapper theObjectMapper){
        employeeService = theEmployeeService;
        objectMapper = theObjectMapper;
    }
    //expose "/employees" and return a list of employees

    @GetMapping("/employees")
    public List<Employee> findAll(){
        return employeeService.findAll();
    }

    @GetMapping("/employees/{employeeId}")
    public Employee findById(@PathVariable int employeeId){
        Employee theEmployee = employeeService.findById(employeeId);

        if(theEmployee == null){
            throw new RuntimeException("Employee id not found - " + employeeId);
        }

        return theEmployee;
    }


    // add mapping for POST /employees - add new employees

    @PostMapping("/employees")
    public Employee addEmployee(@RequestBody Employee theEmployee){
        theEmployee.setId(0);
        Employee dbEmployee = employeeService.save(theEmployee);

        return  dbEmployee;
    }

    @PatchMapping("/employees/{employeeId}")
    public Employee updateEmployee(@PathVariable int employeeId, @RequestBody Map<String, Object> patchPayload){
        Employee tempEmployee = employeeService.findById(employeeId);
        // throw exception if null
        if(tempEmployee == null){
            throw new RuntimeException("Employee not found: " + employeeId);
        }

        if(patchPayload.containsKey("id")){
            throw new RuntimeException("Employee id not allowed in request body - " + employeeId);
        }

        Employee patchedEmployee = apply(patchPayload, tempEmployee);
        return employeeService.save(patchedEmployee);
    }

    @DeleteMapping("/employees/{employeeId}")
    public String deleteEmployee(@PathVariable int employeeId){
        Employee tempEmployee = employeeService.findById(employeeId);

        if(tempEmployee == null){
            throw new RuntimeException("Employee not found: " + employeeId);
        }

        employeeService.deleteById(employeeId);

        return "Deleted employee id - " + employeeId;
    }

    private Employee apply(Map<String, Object> patchPayload, Employee employee) {
        ObjectNode employeeNode = objectMapper.convertValue(employee, ObjectNode.class);

        ObjectNode patchNode = objectMapper.convertValue(patchPayload, ObjectNode.class);

        employeeNode.setAll(patchNode);

        return objectMapper.convertValue(employeeNode, Employee.class);
    }

}
