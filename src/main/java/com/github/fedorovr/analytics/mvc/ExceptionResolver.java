package com.github.fedorovr.analytics.mvc;

import com.github.fedorovr.analytics.mvc.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionResolver {
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public Map<String, String> handleNoHandlerFound(UserNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "user not found");
        return response;
    }
}
