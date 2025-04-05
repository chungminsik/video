package hello.video.domain.dto;

import hello.video.domain.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VideoHomeListResponseDTO {

    public VideoHomeListResponseDTO(){}

    public VideoHomeListResponseDTO(Long id, String thumbnailUrl, String title, User user, LocalDateTime uploadDate, Boolean likedByCurrentUser){
        this.id = id;
        this.thumbnailUrl = thumbnailUrl;
        this.user = user;
        this.title = title;
        this.uploadDate = uploadDate;
        this.likedByCurrentUser = likedByCurrentUser;
    }

    private Long id;
    private String thumbnailUrl;
    private String title;
    private User user;
    private LocalDateTime uploadDate;

    private boolean likedByCurrentUser;
}
