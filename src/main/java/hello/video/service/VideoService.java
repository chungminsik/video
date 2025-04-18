package hello.video.service;

import hello.video.domain.User;
import hello.video.domain.Video;
import hello.video.repository.UserRepository;
import hello.video.repository.VideoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    @Value("${file.upload.video-path}")
    private String videoPath;

    @Value("${file.upload.thumbnail-path}")
    private String thumbnailPath;

    private final String videoUrlPrefix = "/videos/";
    private final String thumbnailUrlPrefix = "/thumbnails/";


    public List<Video> getVideoList(){
        //List<Video> videoList = videoRepository.findAll();
        //List<Video> videoList = videoRepository.findAllWithUserOrderedByUploadDate();
        List<Video> videoList = videoRepository.getVideoListWithLikes();

        return videoList;
    }

    public Video getVideoById(Long id){
        return videoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Video not found with id: " + id));
    }


    /**
     * 비디오를 업로드 하는 메소드
     * - 비디오 확장자 점검
     * - 비디오를 디렉토리에 저장
     * - 비디오를 찾을 때 필요한 정보를 제조
     * - 비디오 객체 생성 후 저장
     *
     * @param file
     * @param title
     * @param description
     * @param thumbnail
     * @param email
     * @return 마이페이지에 보여주기 위한 비디오 파일들을 리턴
     */
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

            //이메일로 저장한 유저를 찾아옴
            String videoPathToString = videoPath.toString() + videoFileName;
            String thumbnailFileNameToString = thumbnailFileName != null ? thumbnailPath + thumbnailFileName : null;

            //요청 주소를 생성
            String videoUrl = videoUrlPrefix + videoFileName;
            String thumbnailUrl = thumbnailUrlPrefix + thumbnailFileName;

            User user = userRepository.findByEmail(email).get();

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


    /**
     * 비디오를 스트리밍 하는 메소드
     * - 비디오를 찾아서 실제 Path로 실제 주소를 찾아오기
     * - 재생하고 있는 비디오 바이너리의 시작, 끝, 시작과 끝길이를 찾음
     * - 실제 비디오에서 리턴할 바이너리 부분을 찾아서 body에 담음, header도 동영상 기준으로 설정
     *
     * @param id
     * @param rangeHeader
     * @return rangeHeader가 있으면 요청이 온 부분부터 일정 부분까지만 리턴, rangeHeader가 없으면 전체 파일을 리턴
     */
    @Transactional
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
                //range 헤더는 bytes=1000- 같은 방식으로 오게됨
                String[] range = rangeHeader.substring(6).split("-");
                //동영상의 현재 재생되고 있는 바이너리 위치
                long start = Long.parseLong(range[0]);
                long end = range.length > 1 && !range[1].isEmpty() ? Long.parseLong(range[1]) : videoLength - 1;

                long contentLength = end - start + 1;

                //실제 비디오에서 응답할 바이트를 배열에 담기
                try(RandomAccessFile videoFile = new RandomAccessFile(videoPath.toFile(), "r")){
                    videoFile.seek(start);
                    videoBytes = new byte[(int) contentLength];
                    videoFile.readFully(videoBytes);
                }

                //206 Partial Content 응답 반환
                InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(videoBytes));

                increaseVideoViews(video, videoLength, start);

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                        .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + videoLength)
                        .body(resource);
            }

            //range 요청이 안왔을 경우 파일 전체를 전송
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

    /**
     * 조회수 올리는 메소드
     * @param video
     * @param videoLength
     * @param start
     */
    private void increaseVideoViews(Video video, long videoLength, long start){
        try{
            //동영상을 3분의 1이상 보면 조회수 올리기(브라우저가 범위를 중복으로 부를 가능성도 있어 조회수가 중복될 가능성도 있음)
            long viewCheck = videoLength/3;
            if (start >= viewCheck){
                video.increaseViews();
                videoRepository.save(video);
            }
        } catch (ObjectOptimisticLockingFailureException e){
            log.warn("조회수 증가 중 버전 충돌 발생. videoId={}, error={}", video.getId(), e.getMessage());
        }
    }


    /**
     *
     * @param videoId
     * @param editTitle
     * @param editDescription
     * @param editThumbnailFile
     * @return
     */
    @Transactional
    public List<Video> updateVideo(Long videoId, String editTitle, String editDescription, MultipartFile editThumbnailFile){

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("비디오를 찾을 수 없습니다."));

        // 새 썸네일 파일이 제공된 경우 처리
        if (editThumbnailFile != null && !editThumbnailFile.isEmpty()) {

            if (!editThumbnailFile.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("썸네일은 이미지 파일만 가능합니다.");
            }

            // 기존 썸네일 파일 삭제 (파일이 존재하면 삭제)
            Path oldThumbnailPath = Paths.get(video.getThumbnailPath());
            try {
                Files.deleteIfExists(oldThumbnailPath);
            } catch (IOException e) {
                throw new RuntimeException("기존 썸네일 삭제에 실패했습니다: " + e.getMessage(), e);
            }

            // 새 썸네일 파일 저장
            String newThumbnailFileName = UUID.randomUUID().toString() + "_" + editThumbnailFile.getOriginalFilename();
            // 실제 파일 저장 경로 (프로젝트 환경에 맞게 조정)
            Path newThumbnailPath = Paths.get(thumbnailPath, newThumbnailFileName);
            try {
                Files.copy(editThumbnailFile.getInputStream(), newThumbnailPath);
            } catch (IOException e) {
                throw new RuntimeException("새 썸네일 저장에 실패했습니다: " + e.getMessage(), e);
            }

            // 엔티티 업데이트: 제목, 설명, 새 썸네일 파일 경로/URL 적용
            // (비디오 엔티티에 updateVideo(...)와 같은 도메인 메서드를 추가했다고 가정)
            video.updateVideo(editTitle, editDescription,
                    newThumbnailPath.toString(), "/url/to/thumbnails/" + newThumbnailFileName);
        } else {
            // 썸네일 파일이 없는 경우, 제목과 설명만 업데이트
            video.updateVideo(editTitle, editDescription,
                    video.getThumbnailPath(), video.getThumbnailUrl());
        }

        // 변경된 엔티티는 트랜잭션 내에서 자동으로 반영되므로, 수정 후 최신 목록을 반환
        return getVideoList();
    }


    /**
     *
     * @param videoId
     * @param userName
     * @return
     */
    @Transactional
    public List<Video> HardDeleteVideo(Long videoId, String userName){
        // 비디오 존재 여부 확인
        Optional<Video> videoOptional = videoRepository.findById(videoId);
        if (!videoOptional.isPresent()) {
            throw new RuntimeException("비디오를 찾을 수 없습니다.");
        }
        Video video = videoOptional.get();

        // 사용자 소유권 검증
        if (!video.getUser().getEmail().equals(userName)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        // 파일 삭제
        String videoFilePath = video.getFilePath();  // 비디오 파일 경로
        String thumbnailFilePath = video.getThumbnailPath(); // 썸네일 파일 경로

        Path videoPath = Paths.get(videoFilePath);
        Path thumbnailPath = Paths.get(thumbnailFilePath);

        try {
            Files.delete(videoPath);
            Files.delete(thumbnailPath);
        } catch (IOException e) {
            throw new RuntimeException("비디오 파일 삭제에 실패했습니다: " + e.getMessage(), e);
        }

        // DB 레코드 삭제
        videoRepository.delete(video);

        return getVideoList();
    }

    /**
     * 비디오를 실제 디렉토리에 저장하는 메소드
     * - 유일한 파일 이름을 생성
     * - 실제 디렉토리 주소와 비디오 파일 이름을 통해 Path로 간접 주소 생성(직접 주소는 보안 상 이유로 접근 불가)
     * - Files를 통해 간접 주소로 디렉토리에 저장
     *
     * @param file
     * @return 저장한 파일 이름 반환
     * @throws IOException
     */
    private String saveVideoFileInDirectory(MultipartFile file) throws IOException {
        String videoDir = videoPath;

        String videoFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path videoPath = Paths.get(videoDir, videoFileName);
        Files.createDirectories(videoPath.getParent());
        file.transferTo(videoPath.toFile());

        return videoFileName;
    }

    /**
     * 썸네일을 실제 디렉토리에 저장하는 메소드
     * - 유일한 파일 이름을 생성
     * - 실제 디렉토리 주소와 비디오 파일 이름을 통해 Path로 간접 주소 생성(직접 주소는 보안 상 이유로 접근 불가)
     * - Files를 통해 간접 주소로 디렉토리에 저장
     *
     * @param thumbnail
     * @return 저장한 파일 이름 반환
     * @throws IOException
     */
    private String saveThumbnailFileInDirectory(MultipartFile thumbnail) throws IOException {
        String thumbnailDir = thumbnailPath;

        String thumbnailFileName = UUID.randomUUID() + "_" + thumbnail.getOriginalFilename();
        Path thumbnailPath = Paths.get(thumbnailDir, thumbnailFileName);
        Files.createDirectories(thumbnailPath.getParent());
        thumbnail.transferTo(thumbnailPath.toFile());

        return thumbnailFileName;
    }
}
