package com.ivanfranchin.bookservice.dto;

import java.math.BigDecimal;

public record BookResponse(String id, String authorName, String title, BigDecimal price) {
}
