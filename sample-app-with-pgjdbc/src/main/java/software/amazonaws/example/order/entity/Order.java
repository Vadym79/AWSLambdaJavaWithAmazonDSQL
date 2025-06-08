package software.amazonaws.example.order.entity;

import java.util.Set;

public record Order (int id, int userId, int totalValue) {}
