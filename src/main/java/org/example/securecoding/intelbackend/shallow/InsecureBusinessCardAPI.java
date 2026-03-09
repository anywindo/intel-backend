package org.example.securecoding.intelbackend.shallow;

public class InsecureBusinessCardAPI {
    public String generateAnonymousToken(){
        return "token";
    }

    public String getEmployeeData(String token, String searchFilter){
        return "employeeData";
    }
}
