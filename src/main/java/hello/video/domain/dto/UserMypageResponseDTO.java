package hello.video.domain.dto;

import hello.video.domain.User;
import hello.video.domain.Video;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UserMypageResponseDTO {

    public UserMypageResponseDTO(User user, List<Video> videos){
        this.id = user.getId();
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.videos = convertToVideoResponseList(videos);
        this.createdTime = user.getCreatedTime();
        this.role = user.getRole();
        this.likeCount = calculateTotalLikeCount(videos);
    }

    private Long id;
    private String userName;
    private String email;
    private List<VideoMypageResponseDTO> videos;
    private LocalDateTime createdTime;
    private String role;
    private Long likeCount;


    private List<VideoMypageResponseDTO> convertToVideoResponseList(List<Video> videos) {
        return videos.stream()
                .map(VideoMypageResponseDTO::new)
                .toList();
    }

    private Long calculateTotalLikeCount(List<Video> videos) {
        return videos.stream()
                .mapToLong(v -> v.getLikes() != null ? v.getLikes().size() : 0)
                .sum();
    }
}
