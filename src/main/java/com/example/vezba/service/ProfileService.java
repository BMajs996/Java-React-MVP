package com.example.vezba.service;

import com.example.vezba.model.AppUser;
import com.example.vezba.repository.AppUserRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
    private final AppUserRepository users;

    public ProfileService(AppUserRepository users) {
        this.users = users;
    }

    public AppUser update(AppUser user, String displayName, String phone, String city, String avatarUrl, String bio) {
        user.setDisplayName(displayName);
        user.setPhone(phone);
        user.setCity(city);
        user.setAvatarUrl(avatarUrl);
        user.setBio(bio);
        return users.save(user);
    }
}
