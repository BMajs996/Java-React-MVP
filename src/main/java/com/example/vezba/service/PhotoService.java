package com.example.vezba.service;

import com.example.vezba.model.AppUser;
import com.example.vezba.model.Photo;
import com.example.vezba.repository.PhotoRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PhotoService {
    private final PhotoRepository photos;

    public PhotoService(PhotoRepository photos) {
        this.photos = photos;
    }

    public List<Photo> list(AppUser owner) {
        return photos.findByOwnerOrderByCreatedAtDesc(owner);
    }

    public Photo add(AppUser owner, String url, String caption) {
        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo URL is required");
        }
        return photos.save(new Photo(owner, url, caption));
    }
}
