package hello.video.web.controller;

import hello.video.service.VideoLikeService;
import hello.video.web.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class VideoLIkeController {

    private final VideoLikeService videoLikeService;

    @PostMapping("/like")
    public String getLike(@RequestBody Map<String, Long> request, @AuthenticationPrincipal CustomUserDetails userDetails){

        videoLikeService.plusLiked(request.get("videoId"), userDetails.getUsername());

        //email, videoId

        return "";
    }
}
