package com.example.usermodule.startData;


import com.example.usermodule.User;
import com.example.usermodule.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;



@Component
public class StartupData implements CommandLineRunner {


 private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(StartupData.class);



    @Autowired
    public StartupData(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        createStudents();

    }

    private void createStudents(){
        User user = new User();
        user.setLogin("user");
        user.setAge(5);
        user.setId(1L);

        userRepository.save(user);
    }

}