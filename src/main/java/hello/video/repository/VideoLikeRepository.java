package hello.video.repository;

import hello.video.domain.VideoLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoLikeRepository extends JpaRepository<VideoLike, Long> {
}
