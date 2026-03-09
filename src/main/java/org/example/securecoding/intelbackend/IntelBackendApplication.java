package org.example.securecoding.intelbackend;

import org.example.securecoding.intelbackend.shallow.InsecureBusinessCardAPI;
import org.example.securecoding.intelbackend.shallow.InsecureUserSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IntelBackendApplication {

    public static void main(String[] args) {
        InsecureBusinessCardAPI api = new InsecureBusinessCardAPI();
        InsecureUserSession session = new InsecureUserSession("user1", api.generateAnonymousToken(), true);

        System.out.println(session.getUsername());
        System.out.println(session.getMsalToken());

        System.out.println(api.getEmployeeData(session.getMsalToken(), "what i do"));

//        SpringApplication.run(IntelBackendApplication.class, args);
    }

}
