package hello.video2.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
public class User {

    public User(){}

    public User(Set<VideoLike> likes,
                List<VideoComment> videoComments,
                List<Video> videos,
                String userEmail,
                String userPassword,
                String userId,
                String userName,
                Long id
    ) {
        this.likes = likes;
        this.videoComments = videoComments;
        this.videos = videos;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userId = userId;
        this.userName = userName;
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userName;

    @Column(unique = true, nullable = false)
    private String userId;

    @Column(nullable = false)
    private String userPassword;

    @Column(unique = true, nullable = false)
    private String userEmail;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Video> videos = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<VideoComment> videoComments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<VideoLike> likes = new HashSet<>();

}
