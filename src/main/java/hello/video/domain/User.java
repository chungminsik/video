package hello.video.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    // User는 외래 키가 없으므로 mappedBy 설정
    // User가 제거되면 관련 동영상도 삭제됨. Video와 user는 생명주기가 같기 때문에 cascade, orphanRemoval설정
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Video> videos = new ArrayList<>();

    // User는 외래 키가 없으므로 mappedBy 설정
    // User가 제거되면 관련 동영상도 삭제됨. VideoLike user는 생명주기가 같기 때문에 cascade, orphanRemoval설정
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VideoLike> likes = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdTime;

    private String role;
}
