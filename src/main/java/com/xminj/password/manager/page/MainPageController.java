package com.xminj.password.manager.page;


import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

@Singleton
public class MainPageController {
    private static final Logger log = LoggerFactory.getLogger(MainPageController.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


}
