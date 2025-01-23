package hello.video.service;

import hello.video.domain.User;
import hello.video.domain.Video;
import hello.video.repository.UserRepository;
import hello.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    public void uploadVideo(
            MultipartFile file,
            String title,
            String description,
            MultipartFile thumbnail,
            String email) {
        try{
            if (!file.getContentType().startsWith("video/")){
                throw new IllegalArgumentException("동영상 파일만 업로드 가능합니다.");
            }
            if (thumbnail != null && !thumbnail.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("썸네일은 이미지 파일만 가능합니다.");
            }

            String videoDir = "";
            String thumbnailDir = "";

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

            //TODO
            Video video = new Video();




        }catch (IOException e){
            throw new RuntimeException("파일 업로드 오류", e);
        }

    }
}
