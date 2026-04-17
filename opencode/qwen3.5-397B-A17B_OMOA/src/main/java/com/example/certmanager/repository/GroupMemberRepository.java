package com.example.certmanager.repository;

import com.example.certmanager.entity.Group;
import com.example.certmanager.entity.GroupMember;
import com.example.certmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    Optional<GroupMember> findByGroupAndUser(Group group, User user);

    List<GroupMember> findByGroup(Group group);

    List<GroupMember> findByUser(User user);

    boolean existsByGroupAndUser(Group group, User user);

    void deleteByGroupAndUser(Group group, User user);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.user = :user AND gm.group = :group")
    Optional<GroupMember> findMember(@Param("user") User user, @Param("group") Group group);

    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group = :group AND gm.role = :role")
    long countByGroupAndRole(@Param("group") Group group, @Param("role") GroupMember.Role role);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group = :group")
    List<GroupMember> findAllByGroup(@Param("group") Group group);
}
