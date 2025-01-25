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
            //업로드 된 비디오와 썸네일이 비디오와 사진 형식의 파일인지 검사(아니면 오류 발생)
            if (!file.getContentType().startsWith("video/")){
                throw new IllegalArgumentException("동영상 파일만 업로드 가능합니다.");
            }
            if (thumbnail != null && !thumbnail.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("썸네일은 이미지 파일만 가능합니다.");
            }

            //디렉토리에 비디오,썸네일 파일을 저장하고, 이름을 반환
            String videoFileName = saveVideoFileInDirectory(file);
            String thumbnailFileName = saveThumbnailFileInDirectory(thumbnail);

            // 이메일로 저장한 유저를 찾아옴
            String videoPathToString = videoPath.toString() + videoFileName;
            String thumbnailFileNameToString = thumbnailFileName != null ? thumbnailPath + thumbnailFileName : null;

            //요청 주소를 생성
            String videoUrl = videoUrlPrefix + videoFileName;
            String thumbnailUrl = thumbnailUrlPrefix + thumbnailFileName;

            User user = getUser(email);
            //비디오 객체 생성 및 저장
            Video video = new Video(
                    title,
                    description,
                    videoPathToString,
                    videoUrl,
                    thumbnailFileNameToString,
                    thumbnailUrl,
                    LocalDateTime.now(),
                    user
            );
            videoRepository.save(video);

            //마이페이지에 보여줄 비디오를 가져감
            return user.getVideos();

        }catch (IOException e){
            throw new RuntimeException("파일 업로드 오류", e);
        }
    }

    private String saveVideoFileInDirectory(MultipartFile file) throws IOException {
        String videoDir = videoPath;

        String videoFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path videoPath = Paths.get(videoDir, videoFileName);
        Files.createDirectories(videoPath.getParent());
        file.transferTo(videoPath.toFile());

        return videoFileName;
    }

    private String saveThumbnailFileInDirectory(MultipartFile thumbnail) throws IOException {
        String thumbnailDir = thumbnailPath;

        String thumbnailFileName = UUID.randomUUID() + "_" + thumbnail.getOriginalFilename();
        Path thumbnailPath = Paths.get(thumbnailDir, thumbnailFileName);
        Files.createDirectories(thumbnailPath.getParent());
        thumbnail.transferTo(thumbnailPath.toFile());

        return thumbnailFileName;
    }

    @Transactional
    public List<Video> deleteVideo(){


        return null;
    }

    @Transactional
    public List<Video> updateVideo(Long videoId, String editTitle, String editDescription, MultipartFile editThumbnailFile){




        return null;
    }

    public ResponseEntity<Resource> getVideoStream(Long id, String rangeHeader) {
        try{
            //Id를 통해 요청이 들어온 동영상 검색(재생 화면을 열 때도 검색을 진행하는데 중복된 검색이 진행됨)
            Video video = getVideoById(id);
            //video의 실제 주소를 가져옴
            Path videoPath = Paths.get(video.getFilePath());

            //파일이 없으면 NOT FOUND
            if (!videoPath.toFile().exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            //비디오 길이
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

    private User getUser(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 입니다"));
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
