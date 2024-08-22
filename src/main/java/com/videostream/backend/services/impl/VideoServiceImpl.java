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
import java.io.IOException;
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

    @Value("${files.video.hsl}")
    String HSL_DIR;


    @PostConstruct
    public void init(){
        File file = new File(DIR);
        try {
            Files.createDirectories(Paths.get(HSL_DIR));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

            //Set Video metadata
            video.setFilePath(path.toString());
            video.setContentType(file.getContentType());

            //Save to database
            videoRepository.save(video);

            //Process video after saving
            processVideo(video.getVideoId(),file);
            return video;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public Video get(String id) {
        Video video = videoRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        return video;
    }

    @Override
    public Video getByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getALl() {
        return videoRepository.findAll();
    }

    @Override
    public String processVideo(String videoId, MultipartFile file) {
        Video video = this.get(videoId);
        String filePath = video.getFilePath();

        //Where to store data
        Path videoPath = Paths.get(filePath);

//        String output360p=HSL_DIR +"/" + videoId+"/360p/";
//        String output720p=HSL_DIR + "/" +videoId+"/720p/";
//        String output1080p=HSL_DIR + "/" + videoId+"/1080p/";

        try {
//            Files.createDirectories(Paths.get(output360p));
//            Files.createDirectories(Paths.get(output720p));
//            Files.createDirectories(Paths.get(output1080p));

            Path outputPath =Paths.get(HSL_DIR,videoId);
            Files.createDirectories(outputPath);

            String ffmpegCmd = String.format(
                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"  \"%s/master.m3u8\" ",
                    videoPath, outputPath, outputPath
            );
            System.out.println("FFMPEG CMD :: " + ffmpegCmd);

            //ffmpeg command
//            StringBuilder ffmpegCmd = new StringBuilder();
//            ffmpegCmd.append("ffmpeg  -i ")
//                    .append(videoPath.toString())
//                    .append(" -c:v libx264 -c:a aac")
//                    .append(" ")
//                    .append("-map 0:v -map 0:a -s:v:0 640x360 -b:v:0 800k ")
//                    .append("-map 0:v -map 0:a -s:v:1 1280x720 -b:v:1 2800k ")
//                    .append("-map 0:v -map 0:a -s:v:2 1920x1080 -b:v:2 5000k ")
//                    .append("-var_stream_map \"v:0,a:0 v:1,a:0 v:2,a:0\" ")
//                    .append("-master_pl_name ").append(HSL_DIR).append(videoId).append("/master.m3u8 ")
//                    .append("-f hls -hls_time 10 -hls_list_size 0 ")
//                    .append("-hls_segment_filename \"").append(HSL_DIR).append(videoId).append("/v%v/fileSequence%d.ts\" ")
//                    .append("\"").append(HSL_DIR).append(videoId).append("/v%v/prog_index.m3u8\"");

            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", ffmpegCmd);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exit = process.waitFor();
            if (exit != 0) {
                throw new RuntimeException("video processing failed!!");
            }

            return videoId;


        } catch (Exception e) {
            throw new RuntimeException("Video processing failed");
        }
    }
}
