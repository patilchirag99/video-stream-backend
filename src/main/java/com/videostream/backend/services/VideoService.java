package com.videostream.backend.services;

import com.videostream.backend.entities.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {

    //Save video
    Video save(Video video, MultipartFile file);

    //Get video by id
    Video get(String id);

    //Get video by title
    Video getByTitle(String title);

    List<Video> getALl();

    //Video processing
    public String processVideo(String videoId, MultipartFile file);
}
