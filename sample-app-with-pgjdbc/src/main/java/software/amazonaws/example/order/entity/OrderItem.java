package software.amazonaws.example.order.entity;

public record OrderItem (int id, int productId, Order order, int value, int quantity) {}
