package com.example.matchnormalizer;

import com.example.csvwatcher.watcher.FileEventValue;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Service
public class MatchNormalizer {
    private static final String[] SUMMARY_HEADER = {"date", "homeName", "homeImage", "awayName", "awayImage", "resultHome", "resultAway", "totalLocal", "firstLocal", "secondLocal", "thirdLocal", "fourthLocal", "extraLocal", "totalAway", "firstAway", "secondAway", "thirdAway", "fourthAway", "extraAway"};
    private final Path inputRoot;
    private final Path outputRoot;

    public MatchNormalizer(@Value("${app.csv-root}") Path inputRoot, @Value("${app.output-root}") Path outputRoot) {
        this.inputRoot = inputRoot.toAbsolutePath().normalize(); this.outputRoot = outputRoot;
    }

    @KafkaListener(topics = "${app.input-topic}")
    public void normalize(FileEventValue event) throws Exception {
        if (event == null || event.getFilePath() == null) return;
        Path file = Path.of(event.getFilePath()).toAbsolutePath().normalize();
        String name = file.getFileName().toString().toLowerCase();
        if (!name.startsWith("match_details_") && !name.startsWith("score_summary_")) return;
        if (!file.startsWith(inputRoot) || !Files.isRegularFile(file)) return;
        Path directory = file.getParent();
        String suffix = name.startsWith("match_details_")
                ? name.substring("match_details_".length())
                : name.substring("score_summary_".length());
        Path details = first(directory, "match_details_" + suffix);
        Path score = first(directory, "score_summary_" + suffix);
        if (details == null || score == null) return;
        Map<String, String> detail = details(details);
        Map<String, String> home = row(score, "home");
        Map<String, String> away = row(score, "away");
        String matchId = home.get("match_id");
        if (matchId == null || matchId.isBlank() || away.isEmpty()) return;
        Path out = outputRoot.resolve(matchId); Files.createDirectories(out);
        writeSummary(out.resolve("MATCH_SUMMARY_" + matchId + ".csv"), detail, home, away);
        writeResults(out.resolve("RESULTS_normalized_spain_acb_" + matchId + ".csv"), home, away);
    }

    private Path first(Path directory, String prefix) throws Exception {
        try (var files = Files.list(directory)) { return files.filter(p -> p.getFileName().toString().toLowerCase().startsWith(prefix)).findFirst().orElse(null); }
    }
    private Map<String, String> details(Path path) throws Exception {
        Map<String, String> result = new HashMap<>();
        try (Reader r = Files.newBufferedReader(path); CSVParser p = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(r)) { for (CSVRecord x : p) result.put(x.get("label").toLowerCase(), x.get("value")); }
        return result;
    }
    private Map<String, String> row(Path path, String side) throws Exception {
        try (Reader r = Files.newBufferedReader(path); CSVParser p = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(r)) { for (CSVRecord x : p) if (side.equalsIgnoreCase(x.get("side"))) { Map<String,String> m = new HashMap<>(); x.toMap().forEach(m::put); return m; } }
        return Map.of();
    }
    private void writeSummary(Path path, Map<String,String> d, Map<String,String> h, Map<String,String> a) throws Exception {
        try (Writer w = Files.newBufferedWriter(path); CSVPrinter p = new CSVPrinter(w, CSVFormat.DEFAULT.builder().setHeader(SUMMARY_HEADER).build())) { p.printRecord(d.getOrDefault("date", ""), h.get("team"), "", a.get("team"), "", h.get("total"), a.get("total"), h.get("total"), h.get("period_1"), h.get("period_2"), h.get("period_3"), h.get("period_4"), h.get("period_5"), a.get("total"), a.get("period_1"), a.get("period_2"), a.get("period_3"), a.get("period_4"), a.get("period_5")); }
    }
    private void writeResults(Path path, Map<String,String> h, Map<String,String> a) throws Exception {
        String[] header = {"matchId","eventTime","homeTeam","awayTeam","homeScore","awayScore","homeScore1","homeScore2","homeScore3","homeScore4","homeScore5","awayScore1","awayScore2","awayScore3","awayScore4","awayScore5"};
        try (Writer w = Files.newBufferedWriter(path); CSVPrinter p = new CSVPrinter(w, CSVFormat.DEFAULT.builder().setHeader(header).build())) { p.printRecord(h.get("match_id"), "", h.get("team"), a.get("team"), h.get("total"), a.get("total"), h.get("period_1"),h.get("period_2"),h.get("period_3"),h.get("period_4"),h.get("period_5"),a.get("period_1"),a.get("period_2"),a.get("period_3"),a.get("period_4"),a.get("period_5")); }
    }
}
