package com.example.rqchallenge;
import com.example.rqchallenge.employees.ControllerEmployee;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

@SpringBootApplication
public class RqChallengeApplication {

    public static void main(String[] args) throws IOException {
        ApplicationContext context = SpringApplication.run(RqChallengeApplication.class, args);

        // Get the ControllerEmployee bean from the context
        ControllerEmployee controllerEmployee = context.getBean(ControllerEmployee.class);

        // Call the getAllEmployees method and print the result
        try {
            controllerEmployee.getAllEmployees();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the first name you want to search:");
        String name = scanner.next();
        controllerEmployee.getEmployeesByNameSearch(name);

        try{
            System.out.println("Enter the id of who you want to search for:");
            String id = scanner.next();
            controllerEmployee.getEmployeeById(id);
        }catch (IOException e){
            e.printStackTrace();
        }

        controllerEmployee.getHighestSalaryOfEmployees();
        controllerEmployee.getTopTenHighestEarningEmployeeNames();

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode employeeJson = mapper.createObjectNode();
        System.out.println("Create New Employee");
        System.out.println("Enter id");
        Integer id = scanner.nextInt();

        employeeJson.put("id", id);
        System.out.println("Enter Name");
        String newName = scanner.next();
        employeeJson.put("employee_name", newName);
        System.out.println("Enter Salary");
        Integer salary = scanner.nextInt();
        employeeJson.put("employee_salary", salary);
        System.out.println("Enter age");
        Integer age = scanner.nextInt();
        employeeJson.put("employee_age", age);
        employeeJson.put("profile_image", ""); //default value
        Map<String, Object> employeeMap = mapper.convertValue(employeeJson, Map.class);

        controllerEmployee.createEmployee(employeeMap);

        System.out.println("Enter ID of a person to delete:");
        String deleteId = scanner.next();

        controllerEmployee.deleteEmployeeById(deleteId);
        System.exit(0);
    }

}
