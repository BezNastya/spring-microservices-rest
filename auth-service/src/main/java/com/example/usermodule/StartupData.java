package com.example.usermodule;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.usermodule.repositories.RoleRepository;
import com.example.usermodule.repositories.UserRepository;


@Component
public class StartupData implements CommandLineRunner {


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private static final Logger logger = LoggerFactory.getLogger(StartupData.class);

    @Autowired
    public StartupData(UserRepository userRepository, RoleRepository roleRepository) {
        this.roleRepository=roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        createRoles();
        createStudents();
    }

    private void createStudents() {


        final Role userRole=roleRepository.findByName("ROLE_USER");
        final List<Role>roleUser= List.of(userRole);
        final Role adminRole=roleRepository.findByName("ROLE_ADMIN");
        final List<Role>roleAdmin= List.of(adminRole);

        User user = new User();
        user.setLogin("user");
        user.setPassword("$2a$04$cmvr8QTVpTxrz2XW3loxWORcgy5t0SzR4gQI.WrRnGEQaKVapAjW6");
        user.setAge(5);
        user.setId(1L);
        user.setRoles(roleUser);

        User admin = new User();
        admin.setLogin("admin");
        admin.setPassword("$2a$04$0swp2JawQzpHDC90bxFog.5s8HmglaWIVLzevnJX9z1fsk6mcvxzK");
        admin.setAge(5);
        admin.setId(2L);
        admin.setRoles(roleAdmin);


        userRepository.save(user);
        userRepository.save(admin);
    }

    private void createRoles() {
        Role admin = new Role();
        admin.setName("ROLE_ADMIN");
        Role user = new Role();
        user.setName("ROLE_USER");

        roleRepository.save(admin);
        roleRepository.save(user);

    }

}