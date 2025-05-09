package hello.video.web.controller;

import hello.video.domain.Video;
import hello.video.domain.dto.VideoHomeListResponseDTO;
import hello.video.service.UserService;
import hello.video.service.VideoService;
import hello.video.web.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final VideoService videoService;
    private final UserService userService;

    @GetMapping("/")
    public String getMain(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long currentUserId = userService.findUserByEmail(userDetails.getUsername()).getId();

        List<VideoHomeListResponseDTO> videoListResponseDTOList =
                getHomeVideoListResponseDTO(videoService.getVideoList(), currentUserId);

        model.addAttribute("videos", videoListResponseDTOList);
        return "index";
    }

    private List<VideoHomeListResponseDTO> getHomeVideoListResponseDTO(List<Video> videoList, Long currentUserId) {
        List<VideoHomeListResponseDTO> returnList = new ArrayList<>();

        for (Video video : videoList) {
            boolean likedByCurrentUser = video.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUserId));

            VideoHomeListResponseDTO videoDTO = new VideoHomeListResponseDTO(
                    video.getId(),
                    video.getThumbnailUrl(),
                    video.getTitle(),
                    video.getUser(),
                    video.getUploadDate(),
                    likedByCurrentUser,
                    video.getViews()
            );
            returnList.add(videoDTO);
        }

        return returnList;
    }
}