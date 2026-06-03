package com.example.vezba.web;

import com.example.vezba.auth.AuthService;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.Favorite;
import com.example.vezba.model.FavoriteType;
import com.example.vezba.model.Notification;
import com.example.vezba.model.Photo;
import com.example.vezba.repository.AppUserRepository;
import com.example.vezba.repository.FavoriteRepository;
import com.example.vezba.repository.NotificationRepository;
import com.example.vezba.repository.PhotoRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final AuthService authService;
    private final AppUserRepository users;
    private final FavoriteRepository favorites;
    private final NotificationRepository notifications;
    private final PhotoRepository photos;

    public ProfileController(AuthService authService, AppUserRepository users, FavoriteRepository favorites,
                             NotificationRepository notifications, PhotoRepository photos) {
        this.authService = authService;
        this.users = users;
        this.favorites = favorites;
        this.notifications = notifications;
        this.photos = photos;
    }

    @PatchMapping
    public ApiDtos.UserDto update(@RequestHeader("X-Auth-Token") String token, @RequestBody ProfileRequest request) {
        AppUser user = authService.requireUser(token);
        user.setDisplayName(request.displayName());
        user.setPhone(request.phone());
        user.setCity(request.city());
        user.setAvatarUrl(request.avatarUrl());
        user.setBio(request.bio());
        return ApiDtos.UserDto.from(users.save(user));
    }

    @GetMapping("/favorites")
    public List<ApiDtos.UserDto> favorites(@RequestHeader("X-Auth-Token") String token) {
        AppUser user = authService.requireUser(token);
        return favorites.findByOwner(user).stream().map(Favorite::getTarget).map(ApiDtos.UserDto::from).toList();
    }

    @PostMapping("/favorites")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.UserDto addFavorite(@RequestHeader("X-Auth-Token") String token, @RequestBody FavoriteRequest request) {
        AppUser owner = authService.requireUser(token);
        AppUser target = users.findById(request.targetId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        favorites.save(new Favorite(owner, target, request.type()));
        return ApiDtos.UserDto.from(target);
    }

    @GetMapping("/notifications")
    public List<ApiDtos.NotificationDto> notifications(@RequestHeader("X-Auth-Token") String token) {
        AppUser user = authService.requireUser(token);
        return notifications.findByRecipientOrderByCreatedAtDesc(user).stream().map(ApiDtos.NotificationDto::from).toList();
    }

    @GetMapping("/photos")
    public List<ApiDtos.PhotoDto> photos(@RequestHeader("X-Auth-Token") String token) {
        AppUser user = authService.requireUser(token);
        return photos.findByOwnerOrderByCreatedAtDesc(user).stream().map(ApiDtos.PhotoDto::from).toList();
    }

    @PostMapping("/photos")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.PhotoDto addPhoto(@RequestHeader("X-Auth-Token") String token, @RequestBody PhotoRequest request) {
        AppUser user = authService.requireUser(token);
        return ApiDtos.PhotoDto.from(photos.save(new Photo(user, request.url(), request.caption())));
    }

    public record ProfileRequest(String displayName, String phone, String city, String avatarUrl, String bio) {
    }

    public record FavoriteRequest(Long targetId, FavoriteType type) {
    }

    public record PhotoRequest(String url, String caption) {
    }
}
