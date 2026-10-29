package io.honeymon.example.apidocs.product;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final AtomicLong sequence = new AtomicLong();
    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final Clock clock;

    public ProductService() {
        this(Clock.systemUTC());
    }

    ProductService(Clock clock) {
        this.clock = clock;
    }

    public Product create(CreateProductRequest request) {
        long id = sequence.incrementAndGet();
        Product product = new Product(id, request.name(), request.category(), request.price(), Instant.now(clock));
        products.put(id, product);
        return product;
    }

    public Product findById(long id) {
        Product product = products.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    public List<Product> findAll(String category) {
        return products.values().stream()
                .filter(product -> category == null || product.category().equalsIgnoreCase(category))
                .sorted(Comparator.comparingLong(Product::id))
                .toList();
    }
}
