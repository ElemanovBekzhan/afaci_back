package org.example.affaci.Models.DTO.Mapper;


import org.example.affaci.Models.DTO.UserRegisterDTO;
import org.example.affaci.Models.Entity.User;
import org.example.affaci.Models.Entity.User_session;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {


    public User toEntityUser(UserRegisterDTO dto){
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setFio(dto.getFio());
        user.setUser_type(dto.getUser_type());
        user.setGoogle_id(dto.getGoogle_id());
        user.setRole(dto.getRole());
        return user;
    }

    public UserRegisterDTO toDtoUser(User user){
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setFio(user.getFio());
        dto.setUser_type(user.getUser_type());
        dto.setGoogle_id(user.getGoogle_id());
        dto.setRole(user.getRole());
        return dto;
    }



    public User_session toEntitySession(UserRegisterDTO dto){
        User_session session = new User_session();
        session.setJwt_token(dto.getJwt_token());
        session.setIp_address(dto.getIp_address());
        session.setLogin_time(dto.getLogin_time());
        return session;
    }

    public UserRegisterDTO toDtoSession(User_session session){
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setJwt_token(session.getJwt_token());
        dto.setIp_address(session.getIp_address());
        dto.setLogin_time(session.getLogin_time());
        return dto;
    }

}
