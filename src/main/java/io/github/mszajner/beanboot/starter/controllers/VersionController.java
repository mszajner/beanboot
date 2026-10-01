package io.github.mszajner.beanboot.starter.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Controller
public class VersionController {

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @GetMapping("/api/version")
    @ResponseBody
    public ResponseEntity<String> version() {
        if (Objects.isNull(buildProperties)) {
            return ResponseEntity.ok("---");
        }
        String time = Optional.ofNullable(buildProperties.getTime())
                .map(t -> DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm").format(t.atZone(ZoneId.systemDefault())))
                .orElse("");
        String version = Optional.ofNullable(buildProperties.getVersion())
                .map(v -> v.replace("SNAPSHOT", time))
                .orElse(time);
        return ResponseEntity.ok(version);
    }
}
