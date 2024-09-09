package com.example.rqchallenge;

import com.example.rqchallenge.employees.ControllerEmployee;
import com.example.rqchallenge.employees.Employee;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class RqChallengeApplicationTests {

    @InjectMocks
    private ControllerEmployee controllerEmployee;

    @Mock
    private ObjectMapper objectMapper;  // Mock the ObjectMapper used in the controller

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testGetAllEmployees() throws IOException {
        // Mocking the URL connection and data fetching

        List<Employee> expectedEmployees = List.of(
                new Employee("1", "John Doe", "10000", "30", ""),
                new Employee("2", "Jane Doe", "20000", "25", "")
        );

        when(objectMapper.readValue((JsonParser) any(), any(TypeReference.class)))
                .thenReturn(Map.of("data", expectedEmployees));

        ResponseEntity<List<Employee>> response = controllerEmployee.getAllEmployees();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testGetEmployeeById_Success() throws IOException {
        Employee expectedEmployee = new Employee("1", "John Doe", "10000", "30", "");

        when(objectMapper.readValue((JsonParser) any(), any(TypeReference.class)))
                .thenReturn(Map.of("data", expectedEmployee));

        Employee employee = controllerEmployee.getEmployeeById("1");
        assertNotNull(employee);
        assertEquals("1", employee.getId());
        assertEquals("John Doe", employee.getEmployee_name());
    }

    @Test
    void testGetHighestSalaryOfEmployees_Success() {
        // Setup the employee list
        List<Employee> employees = List.of(
                new Employee("1", "John Doe", "10000", "30", ""),
                new Employee("2", "Jane Doe", "50000", "28", ""),
                new Employee("3", "Mike Smith", "70000", "35", "")
        );

        controllerEmployee.employeeList = employees;
        ResponseEntity<Optional<Employee>> response = controllerEmployee.getHighestSalaryOfEmployees();
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isPresent());
        assertEquals("Mike Smith", response.getBody().get().getEmployee_name());
    }

    @Test
    void testCreateEmployee_Success() throws IOException {
        Map<String, Object> employeeInput = Map.of(
                "id", 1,
                "employee_name", "John Doe",
                "employee_salary", "50000",
                "employee_age", 30,
                "profile_image", ""
        );

        // Mock the ObjectMapper response
        Employee createdEmployee = new Employee("1", "John Doe", "50000", "30", "");
        when(objectMapper.readValue((JsonParser) any(), any(TypeReference.class)))
                .thenReturn(Map.of("data", createdEmployee));

        // Call the createEmployee method
        ResponseEntity<Employee> response = controllerEmployee.createEmployee(employeeInput);
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("John Doe", response.getBody().getEmployee_name());
    }

    @Test
    void testDeleteEmployeeById_Success() throws IOException {
        when(objectMapper.readValue((JsonParser) any(), any(TypeReference.class)))
                .thenReturn(Map.of("message", "<Successfully! Record has been deleted>"));

        ResponseEntity<String> response = controllerEmployee.deleteEmployeeById("20");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Successfully! Record has been deleted", response.getBody());
    }
}