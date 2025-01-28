package hello.video.web.controller;

import hello.video.domain.Video;
import hello.video.service.VideoService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.List;

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

    // HLS 동영상 재생 화면
    @GetMapping("/hlsVideo/{id}")
    public String getHlsVideo(@PathVariable("id") Long id, Model model, HttpServletRequest request) {

        System.out.println("fdsofjoisajhfoidsajhfdsjofjodasjfoso");

        // 동영상 정보 가져오기
        Video video = videoService.getVideoById(id);

        // HLS Playlist URL 생성 (예: "/videos/{id}/playlist.m3u8")
        String hlsUrl = "/videos/1/playlist.m3u8"; //String.format(, id);

        model.addAttribute("video", video);
        model.addAttribute("hlsUrl", hlsUrl);
        return "hlsVideo"; // hlsVideo.html로 렌더링
    }
}
