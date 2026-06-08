package com.example.vezba.web;

import com.example.vezba.auth.AuthService;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.FavoriteType;
import com.example.vezba.service.FavoriteService;
import com.example.vezba.service.NotificationService;
import com.example.vezba.service.PhotoService;
import com.example.vezba.service.ProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final AuthService authService;
    private final ProfileService profileService;
    private final FavoriteService favoriteService;
    private final NotificationService notificationService;
    private final PhotoService photoService;

    public ProfileController(AuthService authService, ProfileService profileService, FavoriteService favoriteService,
                             NotificationService notificationService, PhotoService photoService) {
        this.authService = authService;
        this.profileService = profileService;
        this.favoriteService = favoriteService;
        this.notificationService = notificationService;
        this.photoService = photoService;
    }

    @PatchMapping
    public ApiDtos.UserDto update(@RequestHeader("X-Auth-Token") String token, @Valid @RequestBody ProfileRequest request) {
        AppUser user = authService.requireUser(token);
        return ApiDtos.UserDto.from(profileService.update(user, request.displayName(), request.phone(), request.city(),
            request.avatarUrl(), request.bio()));
    }

    @GetMapping("/favorites")
    public List<ApiDtos.UserDto> favorites(@RequestHeader("X-Auth-Token") String token) {
        AppUser user = authService.requireUser(token);
        return favoriteService.list(user).stream().map(ApiDtos.UserDto::from).toList();
    }

    @PostMapping("/favorites")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.UserDto addFavorite(@RequestHeader("X-Auth-Token") String token, @Valid @RequestBody FavoriteRequest request) {
        AppUser owner = authService.requireUser(token);
        return ApiDtos.UserDto.from(favoriteService.add(owner, request.targetId(), request.type()));
    }

    @GetMapping("/notifications")
    public List<ApiDtos.NotificationDto> notifications(@RequestHeader("X-Auth-Token") String token) {
        AppUser user = authService.requireUser(token);
        return notificationService.list(user).stream().map(ApiDtos.NotificationDto::from).toList();
    }

    @PatchMapping("/notifications/{id}/read")
    public ApiDtos.NotificationDto markNotificationAsRead(@RequestHeader("X-Auth-Token") String token, @PathVariable Long id) {
        AppUser user = authService.requireUser(token);
        return ApiDtos.NotificationDto.from(notificationService.markAsRead(user, id));
    }

    @GetMapping("/photos")
    public List<ApiDtos.PhotoDto> photos(@RequestHeader("X-Auth-Token") String token) {
        AppUser user = authService.requireUser(token);
        return photoService.list(user).stream().map(ApiDtos.PhotoDto::from).toList();
    }

    @PostMapping("/photos")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.PhotoDto addPhoto(@RequestHeader("X-Auth-Token") String token, @Valid @RequestBody PhotoRequest request) {
        AppUser user = authService.requireUser(token);
        return ApiDtos.PhotoDto.from(photoService.add(user, request.url(), request.caption()));
    }

    public record ProfileRequest(@NotBlank String displayName, String phone, String city, String avatarUrl, String bio) {
    }

    public record FavoriteRequest(@NotNull Long targetId, @NotNull FavoriteType type) {
    }

    public record PhotoRequest(@NotBlank String url, String caption) {
    }
}
