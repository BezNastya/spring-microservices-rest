package com.example.usermodule;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.List;

@RestController
public class Controller {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String test() {
        return "Hello I'm Utility!";
    }

    @GetMapping("/all")
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable("id") String id) {
        User usr = userRepository.findById(Long.valueOf(id)).get();
        BooksList usrBooks =
                restTemplate.getForObject("http://localhost:8002/books/"+id, BooksList.class);

        UserDto res = new UserDto();
        res.setId(usr.getId());
        res.setLogin(usr.getLogin());
        res.setAge(usr.getAge());
        res.setBooks(usrBooks.getBooks());

        return res;
    }

    @GetMapping("/short/{id}")
    public UserDto getByIdShort(@PathVariable("id") String id) {
        User usr = userRepository.findById(Long.valueOf(id)).get();
        UserDto res = new UserDto();
        res.setId(usr.getId());
        res.setLogin(usr.getLogin());
        res.setAge(usr.getAge());
        return res;
    }

    @GetMapping("/save/{id}")
    public ResponseEntity<Resource> getFileById(@PathVariable("id") String id) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        User usr = userRepository.findById(Long.valueOf(id)).get();
        UserDto res = new UserDto();
        res.setId(usr.getId());
        res.setLogin(usr.getLogin());
        res.setAge(usr.getAge());

        String fileName = null;

        if(!id.isBlank() && id != null) {
            fileName = "user" + id + ".text";
        }

        File file = new File(fileName);
        // write to object
        FileOutputStream f = new FileOutputStream(file);
        ObjectOutputStream o = new ObjectOutputStream(f);
        o.writeObject(res);
        o.close();
        f.close();

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                // Content-Type
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                // Contet-Length
                .contentLength(file.length()) //
                .body(resource);
    }
}
