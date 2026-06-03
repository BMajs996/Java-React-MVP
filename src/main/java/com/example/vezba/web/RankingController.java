package com.example.vezba.web;

import com.example.vezba.model.AccountType;
import com.example.vezba.model.AppUser;
import com.example.vezba.repository.AppUserRepository;
import com.example.vezba.repository.GameMatchRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rankings")
public class RankingController {
    private final AppUserRepository users;
    private final GameMatchRepository matches;

    public RankingController(AppUserRepository users, GameMatchRepository matches) {
        this.users = users;
        this.matches = matches;
    }

    @GetMapping
    public List<ApiDtos.RankingDto> ranking() {
        return users.findByAccountType(AccountType.PLAYER).stream()
            .map(this::rankingFor)
            .sorted(Comparator.comparingLong(ApiDtos.RankingDto::wins).reversed()
                .thenComparing(Comparator.comparingDouble(ApiDtos.RankingDto::winRate).reversed()))
            .toList();
    }

    private ApiDtos.RankingDto rankingFor(AppUser user) {
        long played = matches.countByPlayerAOrPlayerB(user, user);
        long wins = matches.countByWinner(user);
        long losses = Math.max(0, played - wins);
        double winRate = played == 0 ? 0 : (double) wins / played;
        return new ApiDtos.RankingDto(user.getId(), user.getDisplayName(), wins, played, losses, winRate);
    }
}
