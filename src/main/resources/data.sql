-- Insert sample admin user (password: admin123)
INSERT INTO users (username, email, password, role, created_at, updated_at)
VALUES ('admin', 'admin@store.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample products
INSERT INTO products (name, description, category, price, quantity, available, image_url, created_at, updated_at)
VALUES
    ('iPhone 14', 'Latest Apple smartphone with advanced features', 'Electronics', 999.99, 50, true, '/images/iphone14.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('Samsung Galaxy S23', 'High-performance Android smartphone', 'Electronics', 899.99, 35, true, '/images/galaxy-s23.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('Nike Air Max', 'Comfortable running shoes', 'Clothing', 129.99, 100, true, '/images/nike-airmax.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('MacBook Pro', 'Professional laptop for developers', 'Electronics', 1999.99, 20, true, '/images/macbook-pro.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('Organic Bananas', 'Fresh organic bananas per kg', 'Grocery', 2.99, 200, true, '/images/bananas.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('Levi''s Jeans', 'Classic blue denim jeans', 'Clothing', 79.99, 75, true, '/images/levis-jeans.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('Sony Headphones', 'Wireless noise-canceling headphones', 'Electronics', 299.99, 30, true, '/images/sony-headphones.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('Fresh Milk', 'Organic whole milk 1L', 'Grocery', 3.49, 150, true, '/images/milk.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);