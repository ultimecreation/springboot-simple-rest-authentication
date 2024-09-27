package com.authapi.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.authapi.demo.models.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {

    public AppUser findByUsername(String username);

    public AppUser findByEmail(String email);
}
