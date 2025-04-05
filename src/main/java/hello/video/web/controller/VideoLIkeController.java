package hello.video.web.controller;

import hello.video.domain.dto.LikeRequestDTO;
import hello.video.service.VideoLikeService;
import hello.video.web.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@Slf4j
public class VideoLIkeController {

    private final VideoLikeService videoLikeService;

    @PostMapping("/like")
    public Map<String, Object> like(@RequestBody LikeRequestDTO dto, @AuthenticationPrincipal CustomUserDetails userDetails){

        Long videoId = Long.valueOf(dto.getVideoId());
        String userEmail = userDetails.getUsername();
        boolean isLiked = dto.getIsLiked();

        videoLikeService.setLike(isLiked, videoId, userEmail);

        return Map.of("success", true);
    }
}
