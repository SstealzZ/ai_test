package com.certalert.tlscert.repository;

import com.certalert.tlscert.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
}
