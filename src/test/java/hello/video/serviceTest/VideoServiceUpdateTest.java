package hello.video.serviceTest;

import hello.video.domain.User;
import hello.video.domain.Video;
import hello.video.repository.VideoRepository;
import hello.video.service.VideoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VideoServiceUpdateTest {

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private VideoService videoService;

    private AutoCloseable closeable;

    private Video video;
    private Path tempVideoFile;
    private Path tempOldThumbnailFile;
    private User user;

    // 하드코딩된 썸네일 저장 경로 (테스트용으로 미리 생성)
    private final Path thumbnailDir = Paths.get("/path/to/thumbnails/");

    @BeforeEach
    public void setUp() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);

        // 썸네일 저장 디렉토리가 없으면 생성 (테스트 환경에서 필요)
        if (!Files.exists(thumbnailDir)) {
            Files.createDirectories(thumbnailDir);
        }

        // 임시 비디오 파일 생성
        tempVideoFile = Files.createTempFile("test-video", ".mp4");
        // 임시 기존 썸네일 파일 생성
        tempOldThumbnailFile = Files.createTempFile("test-old-thumbnail", ".jpg");

        // 테스트용 사용자 생성 (setter 없으므로 ReflectionTestUtils 사용)
        user = new User();
        ReflectionTestUtils.setField(user, "email", "test@example.com");

        // 테스트용 비디오 엔티티 생성 (생성자 사용)
        video = new Video("Original Title", "Original Description",
                tempVideoFile.toString(), "http://dummyurl",
                tempOldThumbnailFile.toString(), "http://dummythumburl",
                LocalDateTime.now(), user);
        // id 필드 설정 (setter 없이 ReflectionTestUtils 사용)
        ReflectionTestUtils.setField(video, "id", 1L);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
        // 임시 파일 삭제 (존재하는 경우)
        if (Files.exists(tempVideoFile)) {
            Files.delete(tempVideoFile);
        }
        if (Files.exists(tempOldThumbnailFile)) {
            Files.delete(tempOldThumbnailFile);
        }
        // 테스트 동안 생성된 새 썸네일 파일 삭제 (thumbnailDir 하위)
        DirectoryStream<Path> stream = Files.newDirectoryStream(thumbnailDir);
        for (Path file : stream) {
            Files.deleteIfExists(file);
        }
        stream.close();
    }

    @Test
    public void testUpdateVideoWithNewThumbnailSuccess() throws IOException {
        // Arrange
        // 모의: 저장소에서 비디오 조회
        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));
        // updateVideo 메소드 마지막에 호출되는 getVideoList()를 스파이 처리하여 stub
        VideoService spyService = spy(videoService);
        doReturn(Collections.emptyList()).when(spyService).getVideoList();

        // 새 썸네일 파일 모의 생성
        String originalFileName = "newthumb.jpg";
        byte[] dummyContent = "dummy image content".getBytes();
        MultipartFile newThumbnail = new MockMultipartFile("editThumbnailFile", originalFileName, "image/jpeg", dummyContent);

        String newTitle = "Updated Title";
        String newDescription = "Updated Description";

        // Act
        List<Video> result = spyService.updateVideo(1L, newTitle, newDescription, newThumbnail);

        // Assert
        // 기존 썸네일 파일이 삭제되었는지 확인
        assertFalse(Files.exists(tempOldThumbnailFile), "기존 썸네일 파일은 삭제되어야 합니다.");
        // 비디오 엔티티의 제목과 설명이 업데이트되었는지 확인
        assertEquals(newTitle, video.getTitle());
        assertEquals(newDescription, video.getDescription());
        // 새 썸네일 경로와 URL에 새 파일명이 포함되어야 함
        assertTrue(video.getThumbnailPath().startsWith(thumbnailDir.toString()),
                "새 썸네일 경로는 지정된 썸네일 디렉토리 내여야 합니다.");
        assertTrue(video.getThumbnailPath().endsWith(originalFileName),
                "새 썸네일 경로는 원본 파일명을 포함해야 합니다.");
        assertTrue(video.getThumbnailUrl().contains(originalFileName),
                "새 썸네일 URL은 원본 파일명을 포함해야 합니다.");
        // 반환 결과가 stub 처리한 빈 리스트여야 함
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testUpdateVideoWithoutNewThumbnailSuccess() {
        // Arrange: 비디오 조회 stub
        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));
        VideoService spyService = spy(videoService);
        doReturn(Collections.emptyList()).when(spyService).getVideoList();

        String newTitle = "Updated Title Only";
        String newDescription = "Updated Description Only";

        // Act: 썸네일 파일 없이 제목과 설명만 수정
        List<Video> result = spyService.updateVideo(1L, newTitle, newDescription, null);

        // Assert: 제목과 설명은 업데이트되고, 썸네일 관련 필드는 그대로 유지됨
        assertEquals(newTitle, video.getTitle());
        assertEquals(newDescription, video.getDescription());
        assertEquals(tempOldThumbnailFile.toString(), video.getThumbnailPath());
        // URL은 원래 값 그대로
        assertEquals("http://dummythumburl", video.getThumbnailUrl());
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testUpdateVideoInvalidThumbnailType() {
        // Arrange: 비디오 조회 stub
        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));
        VideoService spyService = spy(videoService);
        doReturn(Collections.emptyList()).when(spyService).getVideoList();

        // 모의: 이미지가 아닌 파일 (예: application/pdf)
        MultipartFile invalidFile = new MockMultipartFile("editThumbnailFile", "file.pdf", "application/pdf", "dummy content".getBytes());

        // Act & Assert: IllegalArgumentException 발생 확인
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                spyService.updateVideo(1L, "Title", "Description", invalidFile)
        );
        assertEquals("썸네일은 이미지 파일만 가능합니다.", exception.getMessage());
    }

    @Test
    public void testUpdateVideoNotFound() {
        // Arrange: 저장소에서 비디오를 찾지 못하는 경우
        when(videoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: 존재하지 않는 비디오에 대해 RuntimeException 발생 확인
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                videoService.updateVideo(1L, "Title", "Description", null)
        );
        assertEquals("비디오를 찾을 수 없습니다.", exception.getMessage());
    }
}
