package com.example.bookmodule.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookFullDTO {
    private long id;
    private String name;
    private AuthorDTO author;

    @Data
    @Builder
    public static class AuthorDTO {

        private Long id;

        private String firstname;

        private String lastname;
    }

}


