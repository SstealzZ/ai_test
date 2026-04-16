package com.example.certmanager.controller;

import com.example.certmanager.dto.request.AddMemberRequest;
import com.example.certmanager.dto.request.GroupRequest;
import com.example.certmanager.dto.response.ApiResponse;
import com.example.certmanager.dto.response.GroupResponse;
import com.example.certmanager.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.certmanager.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupResponse>>> listGroups(
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long userId = principal.getUserId();
        List<GroupResponse> response = groupService.listGroups(userId);

        return ResponseEntity.ok(ApiResponse.success("Groups retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroup(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {

        Long userId = principal.getUserId();
        GroupResponse response = groupService.getGroupById(id, userId);

        return ResponseEntity.ok(ApiResponse.success("Group retrieved successfully", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody GroupRequest request) {

        Long userId = principal.getUserId();
        GroupResponse response = groupService.createGroup(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Group created successfully", response));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GroupResponse>> addMember(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody AddMemberRequest request) {

        Long userId = principal.getUserId();
        GroupResponse response = groupService.addMember(id, request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Member added successfully", response));
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {

        Long userId = principal.getUserId();
        groupService.removeMember(groupId, memberId, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("Member removed successfully", null));
    }
}
