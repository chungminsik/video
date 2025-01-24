package hello.video.web.controller;

import hello.video.domain.Video;
import hello.video.service.VideoService;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;

@Controller
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @GetMapping("videos/{id}")
    public String getStreamingVideo(@PathVariable("id") Long id, Model model){

        Video video = videoService.getVideoById(id);
        model.addAttribute("video", video);

        return "video";
    }

    @ResponseBody
    @GetMapping("videos/watch/{id}")
    public ResponseEntity<Resource> streamingVideo(
            @PathVariable("id") Long id, @RequestHeader(value = "Range", required = false) String rangeHeader) throws FileNotFoundException {

        return videoService.getVideoStream(id, rangeHeader);
    }
}
