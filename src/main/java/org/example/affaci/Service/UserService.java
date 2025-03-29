package org.example.affaci.Service;


import org.example.affaci.Models.DTO.Mapper.UserMapper;
import org.example.affaci.Models.DTO.UserRegisterDTO;
import org.example.affaci.Models.Entity.User;
import org.example.affaci.Models.Entity.User_session;
import org.example.affaci.Repo.SessionRepo;
import org.example.affaci.Repo.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final SessionRepo sessionRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepo userRepo, SessionRepo sessionRepo, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.sessionRepo = sessionRepo;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserRegisterDTO save(UserRegisterDTO userRegisterDTO) {
        User user = userMapper.toEntityUser(userRegisterDTO);

        user.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));

        User_session session = userMapper.toEntitySession(userRegisterDTO);
        userRepo.save(user);
        sessionRepo.save(session);

        return userMapper.toDtoUser(user);
    }


    public Boolean findByUsername(String username) {
        return userRepo.findByUsername(username) != null;
    }


 /*   public Boolean login(String username, String password) {
        User user = userRepo.findByUsername(username);

    }*/

}
