package com.example.vezba.service;

import com.example.vezba.model.AccountType;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.Favorite;
import com.example.vezba.model.FavoriteType;
import com.example.vezba.repository.AppUserRepository;
import com.example.vezba.repository.FavoriteRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FavoriteService {
    private final FavoriteRepository favorites;
    private final AppUserRepository users;

    public FavoriteService(FavoriteRepository favorites, AppUserRepository users) {
        this.favorites = favorites;
        this.users = users;
    }

    public List<AppUser> list(AppUser owner) {
        return favorites.findByOwner(owner).stream().map(Favorite::getTarget).toList();
    }

    public AppUser add(AppUser owner, Long targetId, FavoriteType type) {
        AppUser target = users.findById(targetId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        validateFavorite(owner, target, type);
        favorites.save(new Favorite(owner, target, type));
        return target;
    }

    private void validateFavorite(AppUser owner, AppUser target, FavoriteType type) {
        if (owner.getId().equals(target.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add yourself to favorites");
        }
        if (target.isBanned()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add banned users to favorites");
        }
        if (type == null || !matchesTargetType(type, target.getAccountType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Favorite type does not match target account type");
        }
        boolean duplicate = favorites.findByOwner(owner).stream()
            .anyMatch(favorite -> favorite.getTarget().getId().equals(target.getId()));
        if (duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Favorite already exists");
        }
    }

    private boolean matchesTargetType(FavoriteType type, AccountType accountType) {
        return (type == FavoriteType.PLAYER && accountType == AccountType.PLAYER)
            || (type == FavoriteType.CLUB && accountType == AccountType.CLUB);
    }
}
