package at.fhtw.usageworker.api;

import at.fhtw.usageworker.dto.UsageHourlyDto;
import at.fhtw.usageworker.model.UsageHourly;
import at.fhtw.usageworker.repo.UsageHourlyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usage/hourly")
public class UsageQueryController {

    private final UsageHourlyRepository repo;

    @GetMapping
    public List<UsageHourlyDto> byRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        return repo.findByHourBetweenOrderByHourAsc(from, to)
                .stream().map(UsageHourlyDto::from).toList();
    }

    @GetMapping("/latest")
    public List<UsageHourlyDto> latest(@RequestParam(defaultValue = "12") int limit) {
        var page = PageRequest.of(0, Math.max(1, limit), Sort.by("hour").descending());
        return repo.findAllByOrderByHourDesc(page)
                .stream().map(UsageHourlyDto::from).toList();
    }
}
