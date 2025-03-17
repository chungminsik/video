package hello.video.web.controller;

import hello.video.domain.Video;
import hello.video.domain.dto.VideoHomeListResponseDTO;
import hello.video.service.VideoService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final VideoService videoService;

    @GetMapping("/")
    public String getMain(Model model){
        List<VideoHomeListResponseDTO> videoListResponseDTOList =
                getHomeVideoListResponseDTO(videoService.getVideoList());

        model.addAttribute("videos", videoListResponseDTOList);

        return "index";
    }

    private List<VideoHomeListResponseDTO> getHomeVideoListResponseDTO(List<Video> videoList){
        List<VideoHomeListResponseDTO> returnList = new ArrayList<>();

        for (Video video : videoList){
            VideoHomeListResponseDTO videoDTO = new VideoHomeListResponseDTO(
                    video.getId(),
                    video.getThumbnailUrl(),
                    video.getTitle(),
                    video.getUser(),
                    video.getUploadDate()
            );
            returnList.add(videoDTO);
        }

        return returnList;
    }
}
