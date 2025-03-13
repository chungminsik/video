package hello.video.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequestDTO {

    private String userName;
    private String password;
    private String email;

}
