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
        this.videos = videos;
        this.createdTime = user.getCreatedTime();
        this.role = user.getRole();
    }

    private Long id;
    private String userName;
    private String email;
    private List<Video> videos;
    private LocalDateTime createdTime;
    private String role;

}
