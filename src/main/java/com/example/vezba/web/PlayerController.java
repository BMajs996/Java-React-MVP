package com.example.vezba.web;

import com.example.vezba.model.AccountType;
import com.example.vezba.repository.AppUserRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final AppUserRepository users;

    public PlayerController(AppUserRepository users) {
        this.users = users;
    }

    @GetMapping
    public List<ApiDtos.PlayerDto> list() {
        return users.findByAccountTypeAndBannedFalseOrderByDisplayNameAsc(AccountType.PLAYER).stream()
            .map(ApiDtos.PlayerDto::from)
            .toList();
    }
}
