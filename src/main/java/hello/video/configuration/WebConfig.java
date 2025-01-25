package hello.video.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:/Users/jeongminsig/coding/Java/uploads/thumbnails/");
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:/Users/jeongminsig/coding/Java/uploads/videos/");
    }

}
