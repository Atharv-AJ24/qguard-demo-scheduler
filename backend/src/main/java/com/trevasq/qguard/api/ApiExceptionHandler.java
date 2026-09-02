package com.trevasq.qguard.api;

import com.trevasq.qguard.api.ApiDtos.ErrorResponse;
import com.trevasq.qguard.service.BookingService;
import com.trevasq.qguard.service.SchedulingService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(SchedulingService.SlotTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse conflict() {
        return new ErrorResponse(
                "SLOT_UNAVAILABLE",
                "That slot is no longer available."
        );
    }

    @ExceptionHandler(BookingService.UnknownManagementTokenException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse absent() {
        return new ErrorResponse(
                "NOT_FOUND",
                "Booking not found."
        );
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentNotValidException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse invalid(Exception exception) {
        return new ErrorResponse(
                "VALIDATION_ERROR",
                exception instanceof IllegalArgumentException
                        ? exception.getMessage()
                        : "Invalid request."
        );
    }
}
