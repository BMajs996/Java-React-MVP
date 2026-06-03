package com.example.vezba.repository;

import com.example.vezba.model.AppUser;
import com.example.vezba.model.Photo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByOwnerOrderByCreatedAtDesc(AppUser owner);
}
