package com.colombianpayments.infrastructure.web;

/** Error body shared by every failing endpoint: a short code and a message for the user. */
public record ApiError(String error, String message) {
}
