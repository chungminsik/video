package hello.video.web.controller;

import hello.video.domain.Video;
import hello.video.domain.dto.UserMypageResponseDTO;
import hello.video.domain.dto.VideoEditRequestDTO;
import hello.video.domain.dto.VideoUploadRequestDTO;
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
        UserMypageResponseDTO userMypageResponseDTO =
                userService.getUserMypageData(userDetails.getUsername());

        model.addAttribute("editDto", new VideoEditRequestDTO());
        model.addAttribute("uploadDto", new VideoUploadRequestDTO());
        model.addAttribute("mypage", userMypageResponseDTO);

        return "mypage";
    }
    /*
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

     */

    @PostMapping("/mypage/upload")
    public String uploadVideo(@AuthenticationPrincipal CustomUserDetails userDetails,
                              VideoUploadRequestDTO uploadRequestDTO,
                              Model model) {
        String email = userDetails.getUsername();

        //여기 dto로 변환해서 보내야됨
        List<Video> videoList = videoService.uploadVideo(
                uploadRequestDTO.getFile(),
                uploadRequestDTO.getTitle(),
                uploadRequestDTO.getDescription(),
                uploadRequestDTO.getThumbnail(),
                email);

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
/*
    @PostMapping("/mypage/editVideo")
    public String editVideo(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @RequestParam("editVideoId") Long id,
                            @RequestParam("editTitle") String editTitle,
                            @RequestParam("editDescription") String editDescription,
                            @RequestParam(value = "editThumbnailFile", required = false) MultipartFile editThumbnailFile,
                            Model model){
        UserMypageResponseDTO customUser = userService.getUserMypageData(userDetails.getUsername());

        if (customUser == null){
            return "";
        }

        videoService.updateVideo(id, editTitle, editDescription, editThumbnailFile);

        return "redirect:/mypage";
    }
    */


    @PostMapping("/mypage/editVideo")
    public String editVideo(@AuthenticationPrincipal CustomUserDetails userDetails,
                            VideoEditRequestDTO videoEditRequestDTO){
        UserMypageResponseDTO customUser = userService.getUserMypageData(userDetails.getUsername());

        if (customUser == null){
            return "";
        }

        videoService.updateVideo(
                videoEditRequestDTO.getId(),
                videoEditRequestDTO.getEditTitle(),
                videoEditRequestDTO.getEditDescription(),
                videoEditRequestDTO.getEditThumbnailFile());

        return "redirect:/mypage";
    }




}
