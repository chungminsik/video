package hello.video.service;

import hello.video.domain.User;
import hello.video.domain.Video;
import hello.video.repository.UserRepository;
import hello.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    @Value("${file.upload.video-path}")
    private String videoPath;

    @Value("${file.upload.thumbnail-path}")
    private String thumbnailPath;

    private final String videoUrlPrefix = "/videos/";
    private final String thumbnailUrlPrefix = "/thumbnails/";

    @Transactional
    public List<Video> uploadVideo(MultipartFile file, String title, String description, MultipartFile thumbnail, String email) {
        try{
            if (!file.getContentType().startsWith("video/")){
                throw new IllegalArgumentException("동영상 파일만 업로드 가능합니다.");
            }
            if (thumbnail != null && !thumbnail.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("썸네일은 이미지 파일만 가능합니다.");
            }

            String videoDir = videoPath;
            String thumbnailDir = thumbnailPath;

            String videoFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path videoPath = Paths.get(videoDir, videoFileName);
            Files.createDirectories(videoPath.getParent());
            file.transferTo(videoPath.toFile());

            String thumbnailFileName = null;
            if (thumbnail != null && !thumbnail.isEmpty()) {
                thumbnailFileName = UUID.randomUUID() + "_" + thumbnail.getOriginalFilename();
                Path thumbnailPath = Paths.get(thumbnailDir, thumbnailFileName);
                Files.createDirectories(thumbnailPath.getParent());
                thumbnail.transferTo(thumbnailPath.toFile());
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 입니다"));

            String videoPathToString = videoPath.toString();
            String thumbnailFileNameToString = thumbnailFileName != null ? thumbnailDir + thumbnailFileName : null;

            String videoUrl = videoUrlPrefix + videoFileName;
            String thumbnailUrl = thumbnailUrlPrefix + thumbnailFileName;

            Video video = new Video(
                    title,
                    description,
                    videoPathToString,
                    videoUrl,
                    thumbnailFileNameToString,
                    thumbnailUrl,
                    LocalDateTime.now(),
                    user);

            videoRepository.save(video);
            return user.getVideos();

        }catch (IOException e){
            throw new RuntimeException("파일 업로드 오류", e);
        }

    }

    public List<Video> getVideoList(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 입니다"));

        List<Video> videoList = user.getVideos();
        return videoList;
    }

    public List<Video> getAllVideoList(){
        List<Video> videoList = videoRepository.findAll();

        return videoList;
    }
}
