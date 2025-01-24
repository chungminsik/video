package hello.video.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Video {

    public Video() {}

    public Video(String title, String description, String file_path, String thumbnail_path, LocalDateTime uploadedTime, User user){
        this.title = title;
        this.description = description;
        this.file_path = file_path;
        this.thumbnailPath = thumbnail_path;
        this.uploadDate = uploadedTime;
        this.user = user;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VideoLike> likes = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String file_path;

    @Column(nullable = false)
    private String thumbnailPath;

    @Column(nullable = false)
    private Long views = 0L;

    @CreationTimestamp
    private LocalDateTime uploadDate;
}
