package io.honeymon.example.apidocs.product;

import java.time.Instant;

public record Product(
        long id,
        String name,
        String category,
        int price,
        Instant createdAt
) {
}
