package com.example.authormodule.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "authors")
public class Author {

    @Id
    private Long id;

    private String firstname;

    private String lastname;
}
