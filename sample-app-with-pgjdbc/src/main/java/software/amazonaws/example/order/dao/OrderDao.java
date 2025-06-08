package software.amazonaws.example.order.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import software.amazonaws.example.order.entity.Order;
import software.amazonaws.example.order.entity.OrderItem;

public class OrderDao {

	public Optional<Order> getOrderById(int id) throws Exception {
		try (Connection con = DsqlDataSourceConfig.getPooledConnection();
				PreparedStatement pst = this.createGetOrderByIdPreparedStatement(con, id);
				ResultSet rs = pst.executeQuery()) {
			if (rs.next()) {

				int userId = rs.getInt("user_id");
				int totalValue = rs.getInt("total_value");
				Order order = new Order(id, userId, totalValue);
				return Optional.of(order);
			} else {
				return Optional.empty();
			}
		}
	}
	
	public Optional <OrderItem> getOrderItemById (int id) throws Exception {
		try (Connection con = DsqlDataSourceConfig.getPooledConnection();
				PreparedStatement pst = this.createGetOrderItemByIdPreparedStatement(con, id);
				ResultSet rs = pst.executeQuery()) {
			if (rs.next()) {

				int userId = rs.getInt("user_id");
				int totalValue = rs.getInt("total_value");
				int orderId = rs.getInt("order_id");
				final Order order = new Order(orderId, userId, totalValue);
				
				int orderItemId=rs.getInt("order_item_id");
				int productId=rs.getInt("product_id");
				int value=rs.getInt("value");
				int quantity=rs.getInt("quantity");
				final OrderItem orderItem = new OrderItem(orderItemId, productId, order, value, quantity);
				return Optional.of(orderItem);
			} else {
				return Optional.empty();
			}
		}
		
	}

	private PreparedStatement createGetOrderByIdPreparedStatement(Connection con, int id) throws SQLException {
		PreparedStatement pst = con.prepareStatement("SELECT * FROM orders WHERE id = ?");
		pst.setInt(1, id);
		return pst;
	}
	
	private PreparedStatement createGetOrderItemByIdPreparedStatement(Connection con, int id) throws SQLException {
		PreparedStatement pst = con.prepareStatement("SELECT o.user_id, o.total_value, oi.id as order_item_id, oi.product_id, oi.order_id, oi.value, oi.quantity FROM order_items as oi JOIN orders as o ON o.id = oi.order_id WHERE oi.id = ? ");
		pst.setInt(1, id);
		return pst;
	}
}
