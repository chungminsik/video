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
public class Video {

    public Video() {}

    public Video(String title, String description, String filePath, String fileUrl, String thumbnailPath, String thumbnailUrl, LocalDateTime uploadedTime, User user) {
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.fileUrl = fileUrl;
        this.thumbnailPath = thumbnailPath;
        this.thumbnailUrl = thumbnailUrl;
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
    private String filePath; // 로컬 파일 시스템에서의 비디오 저장 경로

    @Column(nullable = false)
    private String fileUrl; // 브라우저에서 접근 가능한 비디오 URL

    @Column(nullable = false)
    private String thumbnailPath; // 로컬 파일 시스템에서의 썸네일 저장 경로

    @Column(nullable = false)
    private String thumbnailUrl; // 브라우저에서 접근 가능한 썸네일 URL

    @Column(nullable = false)
    private Long views = 0L;

    @CreationTimestamp
    private LocalDateTime uploadDate;



}