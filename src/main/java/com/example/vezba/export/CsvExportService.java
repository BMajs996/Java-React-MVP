package com.example.vezba.export;

import com.example.vezba.web.ApiDtos;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CsvExportService {
    public String matches(List<ApiDtos.MatchDto> matches) {
        StringBuilder csv = new StringBuilder("id,title,startTime,court,playerA,playerB,status,score,winner\n");
        for (ApiDtos.MatchDto match : matches) {
            csv.append(match.id()).append(',')
                .append(cell(match.title())).append(',')
                .append(cell(String.valueOf(match.startTime()))).append(',')
                .append(cell(match.court() == null ? "" : match.court().name())).append(',')
                .append(cell(match.playerA() == null ? "" : match.playerA().displayName())).append(',')
                .append(cell(match.playerB() == null ? "" : match.playerB().displayName())).append(',')
                .append(cell(match.status())).append(',')
                .append(cell(match.score())).append(',')
                .append(cell(match.winner() == null ? "" : match.winner().displayName()))
                .append('\n');
        }
        return csv.toString();
    }

    public String rankings(List<ApiDtos.RankingDto> rankings) {
        StringBuilder csv = new StringBuilder("playerId,displayName,wins,played,losses,winRate\n");
        for (ApiDtos.RankingDto ranking : rankings) {
            csv.append(ranking.playerId()).append(',')
                .append(cell(ranking.displayName())).append(',')
                .append(ranking.wins()).append(',')
                .append(ranking.played()).append(',')
                .append(ranking.losses()).append(',')
                .append(ranking.winRate())
                .append('\n');
        }
        return csv.toString();
    }

    private String cell(String value) {
        if (value == null) {
            return "";
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
