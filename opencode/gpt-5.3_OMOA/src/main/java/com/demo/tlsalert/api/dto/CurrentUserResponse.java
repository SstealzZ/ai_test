package com.demo.tlsalert.api.dto;

import java.util.List;

public record CurrentUserResponse(String username, String groupName, List<String> authorities) {
}
