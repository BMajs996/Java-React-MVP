package com.example.vezba.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.vezba.model.AccountType;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.Favorite;
import com.example.vezba.model.FavoriteType;
import com.example.vezba.model.UserRole;
import com.example.vezba.repository.AppUserRepository;
import com.example.vezba.repository.FavoriteRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {
    @Mock
    private FavoriteRepository favorites;

    @Mock
    private AppUserRepository users;

    @InjectMocks
    private FavoriteService service;

    @Test
    void addRejectsDuplicateFavorite() {
        AppUser owner = user(1L, AccountType.PLAYER);
        AppUser target = user(2L, AccountType.CLUB);
        when(users.findById(2L)).thenReturn(Optional.of(target));
        when(favorites.findByOwner(owner)).thenReturn(List.of(new Favorite(owner, target, FavoriteType.CLUB)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.add(owner, 2L, FavoriteType.CLUB));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(favorites, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void addRejectsFavoriteTypeMismatch() {
        AppUser owner = user(1L, AccountType.PLAYER);
        AppUser target = user(2L, AccountType.CLUB);
        when(users.findById(2L)).thenReturn(Optional.of(target));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.add(owner, 2L, FavoriteType.PLAYER));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(favorites, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void addRejectsSelfFavorite() {
        AppUser owner = user(1L, AccountType.PLAYER);
        when(users.findById(1L)).thenReturn(Optional.of(owner));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.add(owner, 1L, FavoriteType.PLAYER));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(favorites, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private AppUser user(Long id, AccountType accountType) {
        AppUser user = new AppUser("user-" + id + "@demo.rs", "User " + id, "hash", accountType, UserRole.USER);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
