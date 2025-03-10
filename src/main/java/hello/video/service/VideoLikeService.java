package hello.video.service;

import hello.video.domain.User;
import hello.video.domain.VideoLike;
import hello.video.repository.UserRepository;
import hello.video.repository.VideoLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VideoLikeService {

    private final UserRepository userRepository;


    @Transactional
    public void plusLiked(Long videoId, String userEmail) {

        User user = userRepository.findByEmail(userEmail).get();

        List<VideoLike> videoLike = user.getLikes();






    }
}
