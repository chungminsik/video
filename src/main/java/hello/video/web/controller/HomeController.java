package hello.video.web.controller;

import hello.video.domain.Video;
import hello.video.service.UserService;
import hello.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final VideoService videoService;

    @GetMapping("/")
    public String getMain(Model model){
        List<Video> videoList = videoService.getAllVideoList();
        model.addAttribute("videos", videoList);

        return "index";
    }
}
