package hello.video.service;

import hello.video.domain.User;
import hello.video.domain.Video;
import hello.video.repository.UserRepository;
import hello.video.repository.VideoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    @Value("${file.upload.video-path}")
    private String videoPath;

    @Value("${file.upload.thumbnail-path}")
    private String thumbnailPath;

    private final String videoUrlPrefix = "/videos/";
    private final String thumbnailUrlPrefix = "/thumbnails/";

    private final String VIDEO_STORAGE_PATH = "/Users/jeongminsig/coding/Java/uploads/videos";


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

    public ResponseEntity<Resource> getVideoStream(Long id, String rangeHeader) throws FileNotFoundException {
        try{
            Video video = videoRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid video id"));

            String videoFileName = video.getFilePath();
            //Path videoPath = Paths.get(VIDEO_STORAGE_PATH, videoFileName);
            Path videoPath = Paths.get(video.getFilePath());

            if (!videoPath.toFile().exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            long videoLength = videoPath.toFile().length();
            byte[] videoBytes;

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")){
                String[] range = rangeHeader.substring(6).split("-");
                long start = Long.parseLong(range[0]);
                long end = range.length > 1 && !range[1].isEmpty() ? Long.parseLong(range[1]) : videoLength - 1;

                long contentLength = end - start + 1;

                try(RandomAccessFile videoFile = new RandomAccessFile(videoPath.toFile(), "r")){
                    videoFile.seek(start);
                    videoBytes = new byte[(int) contentLength];
                    videoFile.readFully(videoBytes);
                }

                InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(videoBytes));

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                        .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + videoLength)
                        .body(resource);
            }

            try (RandomAccessFile videoFile = new RandomAccessFile(videoPath.toFile(), "r")) {
                videoBytes = new byte[(int) videoLength];
                videoFile.readFully(videoBytes);
            }

            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(videoBytes));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(videoLength))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(resource);

        } catch (IOException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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

    public Video getVideoById(Long id){
        return videoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Video not found with id: " + id));
    }


}
