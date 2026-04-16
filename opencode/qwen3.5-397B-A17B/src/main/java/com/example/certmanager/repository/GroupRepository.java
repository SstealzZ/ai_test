package com.example.certmanager.repository;

import com.example.certmanager.entity.Group;
import com.example.certmanager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT g FROM Group g JOIN g.members gm WHERE gm.user = :user")
    Page<Group> findByMembersUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT g FROM Group g JOIN g.members gm WHERE gm.user = :user")
    List<Group> findByMembersUser(@Param("user") User user);

    @Query("SELECT COUNT(g) FROM Group g JOIN g.members gm WHERE gm.user = :user")
    long countByMembersUser(@Param("user") User user);
}
