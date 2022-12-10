package com.example.usermodule;

import com.example.usermodule.feign.BookModuleClient;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.usermodule.repositories.UserRepository;

import java.io.*;
import java.util.List;

@RestController
@RequestMapping("/users")
public class Controller {

    private UserRepository userRepository;
    private MeterRegistry meterRegistry;
    private Timer syncExecution;
    private BookModuleClient bookModuleClient;

    @Autowired
    public Controller(UserRepository userRepository, MeterRegistry meterRegistry, BookModuleClient bookModuleClient) {
        this.userRepository = userRepository;
        this.meterRegistry = meterRegistry;
        this.syncExecution = this.meterRegistry.timer("sync.execution", "module", "book-module");
        this.bookModuleClient = bookModuleClient;
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
    public UserDto getById(@RequestHeader("Authorization")String token, @PathVariable("id") Long id) throws Exception {
        BooksList list = syncExecution.recordCallable(() ->
                bookModuleClient.getBooksByAuthor(token, id));
        User usr = userRepository.findUserById(id);
        UserDto res = new UserDto();
        res.setId(usr.getId());
        res.setLogin(usr.getLogin());
        res.setAge(usr.getAge());
        res.setBookList(list.getBooks());

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

    @GetMapping("/file/{id}")
    public ResponseEntity<Resource> getFileById(@PathVariable("id") String id) throws IOException {
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
