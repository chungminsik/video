package hello.video.service;

import hello.video.domain.User;
import hello.video.domain.Video;
import hello.video.domain.dto.MypageDTO;
import hello.video.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser_ROLE_USER(User user){

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 이메일 입니다");
        }

        user.setRole("ROLE_USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Transactional
    public MypageDTO getMypageDTO(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("회원가입 되어있지 않은 이메일 입니다 : " + email));

        List<Video> videos = user.getVideos();
        videos.size();

        return new MypageDTO(user, videos);
    }

}
