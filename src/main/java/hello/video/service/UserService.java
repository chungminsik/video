package hello.video.service;

import hello.video.domain.User;
import hello.video.domain.Video;
import hello.video.domain.dto.UserMypageResponseDTO;
import hello.video.domain.dto.UserRegisterRequestDTO;
import hello.video.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser_ROLE_USER(UserRegisterRequestDTO userRegisterRequestDTO){

        if (userRepository.findByEmail(userRegisterRequestDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 이메일 입니다");
        }

        User user = new User(
                userRegisterRequestDTO.getUserName(),
                passwordEncoder.encode(userRegisterRequestDTO.getPassword()),
                userRegisterRequestDTO.getEmail()
        );

        user.setRole("ROLE_USER");
        User saveUser = userRepository.save(user);

        return saveUser;
    }

    @Transactional
    public UserMypageResponseDTO getUserMypageData(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("회원가입 되어있지 않은 이메일 입니다 : " + email));

        List<Video> videos = user.getVideos();
        videos.size();

        return new UserMypageResponseDTO(user, videos);
    }

    public User findUserByEmail(String email){
        Optional<User> user = userRepository.findByEmail(email);
        return user.get();
    }
}
