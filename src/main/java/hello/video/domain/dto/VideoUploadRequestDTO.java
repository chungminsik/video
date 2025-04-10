package hello.video.domain.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class VideoUploadRequestDTO {

    private MultipartFile file;
    private String title;
    private String description;
    private MultipartFile thumbnail;
}
