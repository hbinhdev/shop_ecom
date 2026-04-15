package com.example.datn_shop_ecom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve ảnh upload từ folder ngoài project
        // URL: /uploads/san-pham/abc.jpg → file: D:/uploads/peak-sneaker/san-pham/abc.jpg
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ /uploads/** vào thư mục vật lý để hiển thị ảnh ngay lập tức sau khi upload
        exposeDirectory("src/main/resources/static/uploads", registry);
        
        // Đảm bảo các tài nguyên tĩnh khác vẫn hoạt động
        registry.addResourceHandler("/assets/**").addResourceLocations("classpath:/static/assets/");
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/client/css/", "classpath:/static/admin/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/client/js/", "classpath:/static/admin/js/");
    }

    private void exposeDirectory(String dirName, ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(dirName);
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        if (dirName.startsWith("../")) dirName = dirName.replace("../", "");
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:/" + uploadPath + "/");
    }
}
