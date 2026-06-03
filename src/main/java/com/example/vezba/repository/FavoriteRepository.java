package com.example.vezba.repository;

import com.example.vezba.model.AppUser;
import com.example.vezba.model.Favorite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByOwner(AppUser owner);
}
