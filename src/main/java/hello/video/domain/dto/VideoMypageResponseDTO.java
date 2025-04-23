package hello.video.domain.dto;

import hello.video.domain.Video;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VideoMypageResponseDTO {

    public VideoMypageResponseDTO(Video video) {
        this.id = video.getId();
        this.title = video.getTitle();
        this.description = video.getDescription();
        this.thumbnailUrl = video.getThumbnailUrl();
        this.uploadDate = video.getUploadDate();
        this.liked = video.getLiked();

    }

    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private LocalDateTime uploadDate;
    private Long liked;
    private Long views;
}