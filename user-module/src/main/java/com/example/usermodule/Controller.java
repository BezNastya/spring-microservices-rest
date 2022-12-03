package com.example.usermodule;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.example.usermodule.repositories.UserRepository;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
public class Controller {

    private RestTemplate restTemplate;
    private UserRepository userRepository;
    private MeterRegistry meterRegistry;
    private Timer syncExecution;

    @Autowired
    public Controller(RestTemplate restTemplate, UserRepository userRepository, MeterRegistry meterRegistry) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.meterRegistry = meterRegistry;
        this.syncExecution = this.meterRegistry.timer("sync.execution", "module", "book-module");
    }

    @GetMapping("/")
    public String test() {
        return "Hello I'm Utility!";
    }

    @GetMapping("/all")
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public UserDto getById(@RequestHeader("Authorization")String token, @PathVariable("id") String id) throws Exception {
        HttpHeaders headers = new HttpHeaders();

        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        headers.add("Authorization",token);
        HttpEntity<Long> entity= new HttpEntity(id,headers);
        URI uri = new URI("http://localhost:8002/books/" + id);

        BooksList list = syncExecution.recordCallable(() ->
                restTemplate.exchange(uri, HttpMethod.GET, entity, BooksList.class).getBody());
        User usr = userRepository.findById(Long.valueOf(id)).get();
        UserDto res = new UserDto();
        res.setId(usr.getId());
        res.setLogin(usr.getLogin());
        res.setAge(usr.getAge());
        res.setBooks(list.getBooks());

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
