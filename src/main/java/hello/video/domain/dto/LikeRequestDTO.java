package hello.video.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeRequestDTO {
    private Long videoId;
    private Boolean isLiked; //그냥 boolean의 경우 Lombok과 Jackson 라이브러리 파싱 규칙으로 인해 이상하게 됨

}
