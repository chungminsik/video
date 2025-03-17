package hello.video.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequestDTO {

    private String password;
    private String email;

}
