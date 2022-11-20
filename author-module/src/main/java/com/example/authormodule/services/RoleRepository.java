package com.example.authormodule.services;

import com.example.authormodule.entities.Author;
import com.example.authormodule.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}
