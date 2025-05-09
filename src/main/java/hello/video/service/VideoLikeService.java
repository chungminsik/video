package hello.video.service;

import hello.video.domain.User;
import hello.video.domain.Video;
import hello.video.domain.VideoLike;
import hello.video.domain.dto.VideoLikeCountResponseDTO;
import hello.video.repository.UserRepository;
import hello.video.repository.VideoLikeRepository;
import hello.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VideoLikeService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final VideoLikeRepository videoLikeRepository;

    @Transactional
    public void setLike(Boolean isLiked, Long videoId, String userEmail) {

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("영상이 존재하지 않습니다"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다"));

        Optional<VideoLike> existingLike = videoLikeRepository.findByVideoAndUser(video, user);

        if (isLiked) {
            // 좋아요 추가
            if (existingLike.isEmpty()) {
                video.increaseLiked();

                VideoLike like = new VideoLike();
                like.setUser(user);
                like.setVideo(video);
                videoLikeRepository.save(like);
            }
        } else {
            // 좋아요 취소
            video.decreaseLiked();

            VideoLike like = existingLike.get();
            videoLikeRepository.delete(like);
        }
    }

    //vidoe를 리스트로 받나?
    public void videoLikeCount(Video video){

        //video에 대한 좋아요 갯수 계산


        //VideoLikeCountResponseDTO에 저장해서 리턴 (이걸 화면에서 사용할 것임)




    }


    public void UserLikeVideo(User user){


    }


}
