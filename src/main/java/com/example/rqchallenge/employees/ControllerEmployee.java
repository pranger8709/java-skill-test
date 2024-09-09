package com.example.rqchallenge.employees;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class ControllerEmployee implements IEmployeeController{
    private static final String BASE_URL = "https://dummy.restapiexample.com/api/v1";
    public List<Employee> employeeList;

    private List<Employee> individualEmployeeByName;

    private Employee individualEmployeeById;
    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() throws IOException {
        System.out.println("getAllEmployees");

        URL url = new URL(BASE_URL + "/employees");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Check if the response is 200 OK
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // Parse JSON response using ObjectMapper
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(conn.getInputStream(), new TypeReference<Map<String, Object>>() {});
            employeeList = mapper.convertValue(responseMap.get("data"), new TypeReference<List<Employee>>() {});
//            System.out.println(employeeList);
            for (Employee employee : employeeList) {
                System.out.println(employee);
            }
            return ResponseEntity.ok(employeeList);
        } else {
            throw new IOException("Failed to fetch employees. HTTP Code: " + conn.getResponseCode());
        }
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        System.out.println("getEmployeesByNameSearch name= " + searchString);
        if(this.employeeList.isEmpty()){
            System.out.println("Employee List is empty");
            return null;
        }
        individualEmployeeByName = employeeList.stream().filter(employee -> employee.getEmployee_name().toLowerCase().contains(searchString.toLowerCase())).collect(Collectors.toList());
        System.out.println(individualEmployeeByName);
        return ResponseEntity.ok(individualEmployeeByName);
    }

    @Override
    public Employee getEmployeeById(String id) throws IOException {
        System.out.println("getEmployeeById id= " + id);
        int retryCount = 0;
        int maxRetries = 5;
        long waitTime = 10000; // 1 second initial wait time
        HttpURLConnection conn = null;
        while(retryCount < maxRetries){
            System.out.println("Attempt " + (retryCount + 1) + "/" + maxRetries);
            URL url = new URL(BASE_URL + "/employee/"+ id);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            // Check if the response is 200 OK
            int code = conn.getResponseCode();
            if (code == 200) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> responseMap = mapper.readValue(conn.getInputStream(), new TypeReference<Map<String, Object>>() {});

                individualEmployeeById = mapper.convertValue(responseMap.get("data"), Employee.class);
                System.out.println(individualEmployeeById);

                // Close the connection
                conn.disconnect();
                return individualEmployeeById;
            } else if(code == 429){
                retryCount++;
                try{
                    Thread.sleep(waitTime);
                }catch(Exception e){
                    e.printStackTrace();
                }
                // Close the connection between hits
                conn.disconnect();
                System.out.println("CODE 429: issue with hitting the API too often");
                waitTime *= 2;
            }else {
                throw new IOException("Failed to fetch employees. HTTP Code: " + conn.getResponseCode());
            }
        }


//      This would be better so we aren't doing so many hits
//        if(this.employeeList.isEmpty()){
//            System.out.println("Employee List is empty");
//            return null;
//        }
//
//        individualEmployeeById = employeeList.stream().filter(employee -> employee.getId().toLowerCase().contains(id.toLowerCase())).collect(Collectors.toList());
//        System.out.println(individualEmployeeById);

        return null;
    }

    @Override
    public ResponseEntity<Optional<Employee>> getHighestSalaryOfEmployees() {
        System.out.println("getHighestSalaryOfEmployees");

        if(this.employeeList.isEmpty()){
            return null;
        }

        Optional<Employee> salary = this.employeeList.stream().max(Comparator.comparingInt(employee -> Integer.parseInt(employee.getEmployee_salary())));
        System.out.println(salary);
        return ResponseEntity.ok(salary);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        System.out.println("getTopTenHighestEarningEmployeeNames");

        List<String> topTen = this.employeeList.stream().sorted((i1, i2) ->
                Integer.compare(Integer.parseInt(i2.getEmployee_salary()),
                        Integer.parseInt(i1.getEmployee_salary()))).limit(10).map(Employee::getEmployee_name)
                        .collect(Collectors.toList());
        System.out.println(topTen);
        return ResponseEntity.ok(topTen);


    }

    @Override
    public ResponseEntity<Employee> createEmployee(Map<String, Object> employeeInput) throws IOException {
        HttpURLConnection conn = null;
        int retryCount = 0;
        int maxRetries = 5;
        long waitTime = 10000; // 10 seconds initial wait time

        while (retryCount < maxRetries) {
            System.out.println("Attempt " + (retryCount + 1) + "/" + maxRetries);
            URL url = new URL(BASE_URL + "/create");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST"); // Use POST for creating resources
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            // Convert employeeInput to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonInputString = objectMapper.writeValueAsString(employeeInput);

            // Send the JSON input to the server
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get the response code from the server
            int code = conn.getResponseCode();
            System.out.println("Response Code: " + code);

            if (code == 200 || code == 201) { // 201 Created is expected for POST requests
                // Read the response from the server
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    // Parse the entire response as a Map
                    Map<String, Object> responseMap = objectMapper.readValue(response.toString(), new TypeReference<Map<String, Object>>() {});
                    System.out.println("Response Map: " + responseMap);

                    // Extract the 'data' field from the response
                    Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");

                    // Convert the 'data' map to an Employee object
                    Employee createdEmployee = objectMapper.convertValue(dataMap, Employee.class);

                    // Close the connection and return the Employee
                    conn.disconnect();
                    return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
                }
            } else if (code == 429) {
                // Handle 429 Too Many Requests (rate-limiting)
                retryCount++;
                try {
                    Thread.sleep(waitTime); // Exponential backoff
                } catch (Exception e) {
                    e.printStackTrace();
                }
                conn.disconnect();
                waitTime *= 2;
            } else {
                throw new IOException("Failed to create employee. HTTP Code: " + code);
            }
        }

        return new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT); // Return timeout status if retries fail
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) throws IOException {
        System.out.println("Deleting Employee with id= " + id);
        int retryCount = 0;
        int maxRetries = 5;
        long waitTime = 10000; // 10 seconds initial wait time
        HttpURLConnection conn = null;

        while (retryCount < maxRetries) {
            System.out.println("Attempt " + (retryCount + 1) + "/" + maxRetries);
            URL url = new URL(BASE_URL + "/delete/" + id);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // Check if the response is 200 OK
            int code = conn.getResponseCode();
            if (code == 200) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> responseMap = mapper.readValue(conn.getInputStream(), new TypeReference<Map<String, Object>>() {});
                String message = (String) responseMap.get("message");
                System.out.println("Response Message: " + message);

                // Close the connection
                conn.disconnect();
                return new ResponseEntity<>(message, HttpStatus.OK);
            } else if (code == 429) {
                retryCount++;
                try {
                    Thread.sleep(waitTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Close the connection between hits
                conn.disconnect();
                System.out.println("CODE 429: Issue with hitting the API too often");
                waitTime *= 2;
            } else {
                throw new IOException("Failed to delete employee. HTTP Code: " + conn.getResponseCode());
            }
        }
        return new ResponseEntity<>("Failed after retries", HttpStatus.REQUEST_TIMEOUT);
    }
}
