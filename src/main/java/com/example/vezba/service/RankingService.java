package com.example.vezba.service;

import com.example.vezba.model.AccountType;
import com.example.vezba.model.AppUser;
import com.example.vezba.repository.AppUserRepository;
import com.example.vezba.repository.GameMatchRepository;
import com.example.vezba.web.ApiDtos;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RankingService {
    private final AppUserRepository users;
    private final GameMatchRepository matches;

    public RankingService(AppUserRepository users, GameMatchRepository matches) {
        this.users = users;
        this.matches = matches;
    }

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
