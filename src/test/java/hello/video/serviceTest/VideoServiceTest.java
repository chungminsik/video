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
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private VideoService videoService;

    private AutoCloseable closeable;

    private Video video;
    private Path tempVideoFile;
    private Path tempThumbnailFile;
    private User user;

    @BeforeEach
    public void setUp() throws IOException{
        closeable = MockitoAnnotations.openMocks(this);

        // 임시 비디오 파일 생성
        tempVideoFile = Files.createTempFile("test-video", ".mp4");
        // 임시 썸네일 파일 생성
        tempThumbnailFile = Files.createTempFile("test-thumbnail", ".jpg");

        // 테스트용 사용자 생성 (setter가 없으므로 ReflectionTestUtils 사용)
        user = new User();
        ReflectionTestUtils.setField(user, "email", "test@example.com");

        // 테스트용 비디오 엔티티 생성 (임시 파일 경로와 사용자 할당)
        video = new Video("Test Title", "Test Description",
                tempVideoFile.toString(), "http://dummyurl",
                tempThumbnailFile.toString(), "dummyThumbUrl",
                LocalDateTime.now(), user);
        // id 필드 설정 (setter 없이 ReflectionTestUtils 사용)
        ReflectionTestUtils.setField(video, "id", 1L);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
        // 만약 임시 파일이 남아있다면 삭제
        if (Files.exists(tempVideoFile)) {
            Files.delete(tempVideoFile);
        }
    }

    @Test
    public void testHardDeleteVideoSuccess() {
        // Arrange: 비디오 존재 및 정상 조회
        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));
        // getVideoList()는 간단하게 빈 리스트를 반환한다고 가정
        VideoService spyService = spy(videoService);
        doReturn(Collections.emptyList()).when(spyService).getVideoList();

        // Act: HardDeleteVideo 메소드 호출
        List<Video> result = spyService.HardDeleteVideo(1L, "test@example.com");

        // Assert:
        // 1. 영상 파일 삭제 확인
        assertFalse(Files.exists(tempVideoFile), "임시 영상 파일은 삭제되어야 합니다.");
        // 2. 썸네일 파일 삭제 확인
        assertFalse(Files.exists(tempThumbnailFile), "임시 썸네일 파일은 삭제되어야 합니다.");
        // 3. 비디오 삭제가 repository.delete()를 통해 호출되었는지 검증
        verify(videoRepository, times(1)).delete(video);
        // 4. 반환 결과가 null이 아니어야 함
        assertNotNull(result);
    }

    @Test
    public void testHardDeleteVideoNotFound() {
        // Arrange: 저장소에서 비디오를 찾지 못하는 상황
        when(videoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: 예외 발생 여부 확인
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                videoService.HardDeleteVideo(1L, "test@example.com")
        );
        assertEquals("비디오를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    public void testHardDeleteVideoNotAuthorized() {
        // Arrange: 저장소에서 정상 조회되었으나, 요청한 사용자가 소유자가 아님
        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));

        // Act & Assert: 삭제 권한 부족 예외 확인
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                videoService.HardDeleteVideo(1L, "wrong@example.com")
        );
        assertEquals("삭제 권한이 없습니다.", exception.getMessage());
    }

    // 파일 삭제 실패 케이스는 static 메서드인 Files.delete()를 모의(mock)하기 어려워
    // PowerMockito와 같은 도구를 사용해야 하나, 여기서는 생략합니다.
}

