package hello.video.service;

import hello.video.domain.User;
import hello.video.domain.Video;
import hello.video.domain.dto.UserProfileResponseDTO;
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
    public User registerUser_ROLE_USER(User user){

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 이메일 입니다");
        }

        user.setRole("ROLE_USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User saveUser = userRepository.save(user);

        return saveUser;
    }

    @Transactional
    public UserProfileResponseDTO getMypageDTO(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("회원가입 되어있지 않은 이메일 입니다 : " + email));

        List<Video> videos = user.getVideos();
        videos.size();

        return new UserProfileResponseDTO(user, videos);
    }

    public User findUserByEmail(String email){
        Optional<User> user = userRepository.findByEmail(email);
        return user.get();
    }
}
