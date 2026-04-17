package com.example.certmanager.service;

import com.example.certmanager.dto.request.AddMemberRequest;
import com.example.certmanager.dto.request.GroupRequest;
import com.example.certmanager.dto.response.GroupMemberResponse;
import com.example.certmanager.dto.response.GroupResponse;
import com.example.certmanager.entity.Group;
import com.example.certmanager.entity.GroupMember;
import com.example.certmanager.entity.User;
import com.example.certmanager.exception.AccessDeniedException;
import com.example.certmanager.exception.ResourceNotFoundException;
import com.example.certmanager.exception.ValidationException;
import com.example.certmanager.repository.GroupMemberRepository;
import com.example.certmanager.repository.GroupRepository;
import com.example.certmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing groups and group memberships.
 */
@Service
@Transactional
public class GroupService {

    private static final Logger log = LoggerFactory.getLogger(GroupService.class);

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public GroupService(GroupRepository groupRepository,
                        GroupMemberRepository groupMemberRepository,
                        UserRepository userRepository,
                        AuditService auditService) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    /**
     * Create a new group.
     */
    public Group createGroup(String name, String description, User creator) {
        log.info("Creating group: {}", name);

        if (name == null || name.trim().isEmpty()) {
            throw ValidationException.invalidGroupName("Group name cannot be empty");
        }

        if (groupRepository.existsByName(name)) {
            throw ValidationException.duplicateGroupName(name);
        }

        Group group = new Group();
        group.setName(name.trim());
        group.setDescription(description);
        group.setCreatedBy(creator);

        Group savedGroup = groupRepository.save(group);

        // Add creator as OWNER
        GroupMember membership = new GroupMember();
        membership.setGroup(savedGroup);
        membership.setUser(creator);
        membership.setRole(GroupMember.Role.OWNER);
        groupMemberRepository.save(membership);

        log.info("Created group with id: {}", savedGroup.getId());

        auditService.logAction(
                creator.getId(),
                "CREATE_GROUP",
                "GROUP",
                savedGroup.getId(),
                "Created group: " + name,
                null
        );

        return savedGroup;
    }

    /**
     * Get group by ID.
     */
    @Transactional(readOnly = true)
    public Group getGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.groupNotFound(id));
    }

    /**
     * List all groups for a user.
     */
    @Transactional(readOnly = true)
    public Page<Group> listUserGroups(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));
        return groupRepository.findByMembersUser(user, pageable);
    }

    /**
     * List all groups for a user (unpaginated).
     */
    @Transactional(readOnly = true)
    public List<Group> listUserGroups(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));
        return groupRepository.findByMembersUser(user);
    }

    /**
     * Add a member to a group.
     */
    public GroupMember addMember(Long groupId, Long userId, GroupMember.Role role, User addedBy) {
        log.info("Adding user {} to group {} with role {}", userId, groupId, role);

        Group group = getGroupById(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));

        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw ValidationException.invalidGroupName("User is already a member of this group");
        }

        GroupMember.Role adderRole = getUserRoleInGroup(addedBy.getId(), groupId);
        if (adderRole != GroupMember.Role.OWNER && adderRole != GroupMember.Role.ADMIN) {
            throw AccessDeniedException.insufficientRole("OWNER or ADMIN", addedBy.getId());
        }

        GroupMember membership = new GroupMember();
        membership.setGroup(group);
        membership.setUser(user);
        membership.setRole(role);

        GroupMember savedMembership = groupMemberRepository.save(membership);

        log.info("Added user {} to group {} with role {}", userId, groupId, role);

        auditService.logAction(
                addedBy.getId(),
                "ADD_MEMBER",
                "GROUP_MEMBER",
                savedMembership.getId(),
                "Added user " + userId + " to group " + groupId + " with role " + role,
                null
        );

        return savedMembership;
    }

    /**
     * Remove a member from a group.
     */
    public void removeMember(Long groupId, Long userId, User removedBy) {
        log.info("Removing user {} from group {}", userId, groupId);

        Group group = getGroupById(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));

        GroupMember.Role removerRole = getUserRoleInGroup(removedBy.getId(), groupId);
        if (removerRole != GroupMember.Role.OWNER && removerRole != GroupMember.Role.ADMIN) {
            throw AccessDeniedException.insufficientRole("OWNER or ADMIN", removedBy.getId());
        }

        if (removerRole == GroupMember.Role.OWNER) {
            long ownerCount = groupMemberRepository.countByGroupAndRole(group, GroupMember.Role.OWNER);
            if (ownerCount == 1 && userId.equals(removedBy.getId())) {
                throw ValidationException.invalidGroupName("Cannot remove the last owner from the group");
            }
        }

        groupMemberRepository.deleteByGroupAndUser(group, user);

        log.info("Removed user {} from group {}", userId, groupId);

        auditService.logAction(
                removedBy.getId(),
                "REMOVE_MEMBER",
                "GROUP_MEMBER",
                null,
                "Removed user " + userId + " from group " + groupId,
                null
        );
    }

    /**
     * Get user's role in a group.
     */
    @Transactional(readOnly = true)
    public GroupMember.Role getUserRoleInGroup(Long userId, Long groupId) {
        Group group = getGroupById(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));

        Optional<GroupMember> membership = groupMemberRepository.findByGroupAndUser(group, user);
        return membership.map(GroupMember::getRole).orElse(null);
    }

    /**
     * Check if user is a member of a group.
     */
    @Transactional(readOnly = true)
    public boolean isMemberOfGroup(Long userId, Long groupId) {
        return getUserRoleInGroup(userId, groupId) != null;
    }

    /**
     * Get all members of a group.
     */
    @Transactional(readOnly = true)
    public List<GroupMember> getGroupMembers(Long groupId) {
        Group group = getGroupById(groupId);
        return groupMemberRepository.findAllByGroup(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> listGroups(Long userId) {
        return listUserGroups(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroupById(Long id, Long userId) {
        Group group = getGroupById(id);
        return mapToResponse(group);
    }

    public GroupResponse createGroup(GroupRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));
        Group group = createGroup(request.getName(), request.getDescription(), user);
        return mapToResponse(group);
    }

    public GroupResponse addMember(Long groupId, AddMemberRequest request, Long userId) {
        User addedBy = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));
        User member = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ResourceNotFoundException.userNotFoundByEmail(request.getEmail()));
        GroupMember.Role role = GroupMember.Role.valueOf(request.getRole());
        addMember(groupId, member.getId(), role, addedBy);
        return mapToResponse(getGroupById(groupId));
    }

    public void removeMember(Long groupId, Long memberId, Long userId) {
        User removedBy = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(memberId));
        removeMember(groupId, member.getId(), removedBy);
    }

    private GroupResponse mapToResponse(Group group) {
        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setDescription(group.getDescription());
        if (group.getCreatedBy() != null) {
            response.setCreatedBy(group.getCreatedBy().getId());
            response.setCreatedByName(group.getCreatedBy().getName());
        }
        response.setCreatedAt(group.getCreatedAt());
        response.setUpdatedAt(group.getUpdatedAt());
        response.setCertificateCount(group.getCertificates() != null ? group.getCertificates().size() : 0);
        if (group.getMembers() != null) {
            response.setMembers(group.getMembers().stream().map(this::mapMemberToResponse).toList());
        }
        return response;
    }

    private GroupMemberResponse mapMemberToResponse(GroupMember member) {
        GroupMemberResponse response = new GroupMemberResponse();
        response.setId(member.getId());
        if (member.getUser() != null) {
            response.setUserId(member.getUser().getId());
            response.setUserEmail(member.getUser().getEmail());
            response.setUserName(member.getUser().getName());
            response.setUserPicture(member.getUser().getPicture());
        }
        response.setRole(member.getRole() != null ? member.getRole().name() : null);
        response.setCreatedAt(member.getCreatedAt());
        response.setUpdatedAt(member.getUpdatedAt());
        return response;
    }
}
