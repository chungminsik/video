package hello.video.repository;

import hello.video.domain.User;
import hello.video.domain.Video;
import hello.video.domain.VideoLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoLikeRepository extends JpaRepository<VideoLike, Long> {
    Optional<VideoLike> findByVideoAndUser(Video video, User user);
}
