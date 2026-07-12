package com.demo.prarice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/entry")
public class DemoController {

    @GetMapping
    public ResponseEntity<Map> getWelcome(){
        Map<String,String> envValues=new HashMap<>();


        envValues.put("HostName",System.getenv("HOSTNAME"));
        envValues.put("Pod Namespace",System.getenv("POD_NAMESPACE"));
        envValues.put("Node Name",System.getenv("NODE_NAME"));
        ZonedDateTime zoneDateTimeNow = ZonedDateTime.now();

        envValues.put("Date",zoneDateTimeNow.toString() );
        return ResponseEntity.ok(envValues);
    }
}
