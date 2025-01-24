package hello.video.web.controller;

import hello.video.domain.User;
import hello.video.domain.Video;
import hello.video.domain.dto.MypageDTO;
import hello.video.service.UserService;
import hello.video.service.VideoService;
import hello.video.web.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        MypageDTO mypageDTO = userService.getMypageDTO(userDetails.getUsername());

        model.addAttribute("mypage", mypageDTO);

        return "mypage";
    }

    @PostMapping("/mypage/upload")
    public String uploadVideo(@RequestParam("file") MultipartFile file,
                              @RequestParam("title") String title,
                              @RequestParam("description") String description,
                              @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        String email = userDetails.getUsername();
        List<Video> videoList = videoService.uploadVideo(file, title, description, thumbnail, email);

        model.addAttribute("myVideos", videoList);

        return "redirect:/mypage";
    }


}
