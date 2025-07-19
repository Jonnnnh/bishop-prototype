package com.example.bishopprototype.controller;

import com.example.synthetichumancore.aspect.AuditLevel;
import com.example.synthetichumancore.aspect.WeylandWatchingYou;
import com.example.synthetichumancore.dispatcher.CommandDispatcher;
import com.example.synthetichumancore.dto.CommandDto;
import com.example.synthetichumancore.model.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/bishop")
@RequiredArgsConstructor
public class CommandController {

    private final CommandDispatcher dispatcher;

    @PostMapping("/cmd")
    @WeylandWatchingYou(level = AuditLevel.INFO)
    public ResponseEntity<Void> send(@RequestBody CommandDto dto) {
        Command cmd = Command.builder()
                .description(dto.getDescription())
                .priority(dto.getPriority())
                .author(dto.getAuthor())
                .time(Instant.parse(dto.getTime()))
                .build();

        cmd.setStatus("PENDING");
        dispatcher.dispatch(cmd);
        return ResponseEntity.accepted().build();
    }
}
