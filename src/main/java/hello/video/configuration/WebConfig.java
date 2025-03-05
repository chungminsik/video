package hello.video.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.video-path}")
    private String videoPath;

    @Value("${file.upload.thumbnail-path}")
    private String thumbnailPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:" + thumbnailPath);
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:" + videoPath);
    }

}
