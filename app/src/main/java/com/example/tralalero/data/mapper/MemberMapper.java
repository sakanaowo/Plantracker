package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.member.MemberDTO;
import com.example.tralalero.domain.model.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberMapper {
    
    public static Member toDomain(MemberDTO dto) {
        if (dto == null) return null;
        
        Member member = new Member();
        member.setId(dto.getId());
        member.setUserId(dto.getUserId());
        member.setRole(dto.getRole());
        member.setAddedBy(dto.getAddedBy());
        member.setCreatedAt(dto.getCreatedAt());
        
        // Map nested user info
        if (dto.getUser() != null) {
            Member.User user = new Member.User();
            user.setId(dto.getUser().getId());
            user.setName(dto.getUser().getName());
            user.setEmail(dto.getUser().getEmail());
            user.setAvatarUrl(dto.getUser().getAvatarUrl());
            member.setUser(user);
        }
        
        return member;
    }
    
    public static List<Member> toDomainList(List<MemberDTO> dtoList) {
        if (dtoList == null) return new ArrayList<>();
        
        List<Member> members = new ArrayList<>();
        for (MemberDTO dto : dtoList) {
            members.add(toDomain(dto));
        }
        return members;
    }
    
    public static MemberDTO toDTO(Member member) {
        if (member == null) return null;
        
        MemberDTO dto = new MemberDTO();
        dto.setId(member.getId());
        dto.setUserId(member.getUserId());
        dto.setRole(member.getRole());
        dto.setAddedBy(member.getAddedBy());
        dto.setCreatedAt(member.getCreatedAt());
        
        // Map nested user info
        if (member.getUser() != null) {
            MemberDTO.UserInfo userInfo = new MemberDTO.UserInfo();
            userInfo.setId(member.getUser().getId());
            userInfo.setName(member.getUser().getName());
            userInfo.setEmail(member.getUser().getEmail());
            userInfo.setAvatarUrl(member.getUser().getAvatarUrl());
            dto.setUser(userInfo);
        }
        
        return dto;
    }
}
