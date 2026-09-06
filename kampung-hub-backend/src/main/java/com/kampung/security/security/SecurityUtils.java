package com.kampung.security.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// BE-Req-2 & 4: Static context holder interface to access the authenticated JwtUser principal
public class SecurityUtils {

    public static JwtUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof JwtUser) {
            return (JwtUser) auth.getPrincipal();
        }
        return null;
    }

    public static boolean isAuthenticated() {
        return getCurrentUser() != null;
    }

    public static String getCurrentNeighborhoodId() {
        JwtUser user = getCurrentUser();
        return user != null ? user.getNeighborhoodId() : null;
    }

    public static String getCurrentMembershipId() {
        JwtUser user = getCurrentUser();
        return user != null ? user.getMembershipId() : null;
    }
}
