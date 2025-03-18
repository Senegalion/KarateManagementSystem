package com.karate.management.karatemanagementsystem.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
@Table(name = "roles")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "roleEntity")
    private Set<UserRoleEntity> userRoleEntities;
}
