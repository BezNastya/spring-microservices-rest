package com.example.authormodule.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "authors")
@AllArgsConstructor
@NoArgsConstructor
public class Author {

    @Id
    private Long id;

    private String firstname;

    private String lastname;
}
