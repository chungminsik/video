package hello.video.domain.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class VideoEditRequestDTO {

    private Long id;
    private String editTitle;
    private String editDescription;
    private MultipartFile editThumbnailFile;
}
