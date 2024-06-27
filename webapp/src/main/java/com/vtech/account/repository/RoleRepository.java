package com.vtech.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vtech.account.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long>{
}
