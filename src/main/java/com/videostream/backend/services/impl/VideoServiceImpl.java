package com.videostream.backend.services.impl;

import com.videostream.backend.entities.Video;
import com.videostream.backend.repositories.VideoRepository;
import com.videostream.backend.services.VideoService;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    VideoRepository videoRepository;

    @Value("${files.video}")
    String DIR;

    @PostConstruct
    public void init(){
        File file = new File(DIR);
        if(!file.exists()){
            file.mkdir();
            System.out.println("Folder created");
        }
        else {
            System.out.println("Folder already created");
        }
    }


    @Override
    public Video save(Video video, MultipartFile file) {
        try{
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();

            //create folder path to save video
             String cleanFileName = StringUtils.cleanPath(fileName);
             String cleanFolder = StringUtils.cleanPath(DIR);
             Path path = Paths.get(cleanFolder, cleanFileName);
             System.out.println(path);

            //copy file to folder
            Files.copy(inputStream,path, StandardCopyOption.REPLACE_EXISTING);

            //Video metadata
            video.setFilePath(path.toString());
            video.setContentType(file.getContentType());

            //Save metadata
            videoRepository.save(video);

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return video;
    }

    @Override
    public Video get(String id) {
        return null;
    }

    @Override
    public Video getByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getALl() {
        return null;
    }
}
