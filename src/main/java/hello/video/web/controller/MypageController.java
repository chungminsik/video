package hello.video.web.controller;

import hello.video.domain.Video;
import hello.video.domain.dto.UserProfileResponseDTO;
import hello.video.service.UserService;
import hello.video.service.VideoService;
import hello.video.web.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MypageController {

    private final UserService userService;
    private final VideoService videoService;

    @GetMapping("/mypage")
    public String getMypage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model){
        UserProfileResponseDTO userProfileResponseDTO = userService.getMypageDTO(userDetails.getUsername());
        model.addAttribute("mypage", userProfileResponseDTO);

        return "mypage";
    }

    @PostMapping("/mypage/upload")
    public String uploadVideo(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestParam("file") MultipartFile file,
                              @RequestParam("title") String title,
                              @RequestParam("description") String description,
                              @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
                              Model model) {
        String email = userDetails.getUsername();
        List<Video> videoList = videoService.uploadVideo(file, title, description, thumbnail, email);
        model.addAttribute("myVideos", videoList);

        return "redirect:/mypage";
    }

    @PostMapping("videos/delete")
    public String deleteVideo(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestParam("videoId") Long videoId){

        String email = userDetails.getUsername();
        videoService.HardDeleteVideo(videoId, email);

        return "redirect:/mypage";
    }

    @PostMapping("/mypage/editVideo")
    public String editVideo(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @RequestParam("editVideoId") Long id,
                            @RequestParam("editTitle") String editTitle,
                            @RequestParam("editDescription") String editDescription,
                            @RequestParam(value = "editThumbnailFile", required = false) MultipartFile editThumbnailFile,
                            Model model
                            ){
        UserProfileResponseDTO customUser = userService.getMypageDTO(userDetails.getUsername());

        if (customUser == null){
            return "";
        }

        videoService.updateVideo(id, editTitle, editDescription, editThumbnailFile);

        return "redirect:/mypage";
    }


}
