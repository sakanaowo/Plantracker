package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.workspace.MembershipDTO;
import com.example.tralalero.domain.model.Membership;
import com.example.tralalero.domain.model.Role;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Mapper for Membership entity
 */
public class MembershipMapper {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
    );

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static Membership toDomain(MembershipDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Membership(
                dto.getId(),
                dto.getUserId(),
                dto.getWorkspaceId(),
                parseRole(dto.getRole()),
                parseDate(dto.getCreatedAt())
        );
    }

    public static MembershipDTO toDTO(Membership membership) {
        if (membership == null) {
            return null;
        }

        MembershipDTO dto = new MembershipDTO();
        dto.setId(membership.getId());
        dto.setUserId(membership.getUserId());
        dto.setWorkspaceId(membership.getWorkspaceId());
        dto.setRole(formatRole(membership.getRole()));
        dto.setCreatedAt(formatDate(membership.getCreatedAt()));

        return dto;
    }

    public static List<Membership> toDomainList(List<MembershipDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<Membership> memberships = new ArrayList<>();
        for (MembershipDTO dto : dtos) {
            Membership membership = toDomain(dto);
            if (membership != null) {
                memberships.add(membership);
            }
        }
        return memberships;
    }

    public static List<MembershipDTO> toDTOList(List<Membership> memberships) {
        if (memberships == null) {
            return new ArrayList<>();
        }

        List<MembershipDTO> dtos = new ArrayList<>();
        for (Membership membership : memberships) {
            MembershipDTO dto = toDTO(membership);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        return dtos;
    }

    private static Role parseRole(String roleString) {
        if (roleString == null || roleString.isEmpty()) {
            return Role.MEMBER; // Default
        }

        try {
            return Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Role.MEMBER; // Default fallback
        }
    }

    private static String formatRole(Role role) {
        if (role == null) {
            return "MEMBER";
        }
        return role.name();
    }

    private static Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            return DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return DATE_FORMAT.format(date);
    }
}

