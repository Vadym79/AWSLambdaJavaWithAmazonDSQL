package software.amazonaws.example.order.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import software.amazonaws.example.order.entity.Order;
import software.amazonaws.example.order.entity.Order.Status;
import software.amazonaws.example.order.entity.OrderItem;

public class OrderDao {
	
	//private DsqlDataSourceConfig config = new DsqlDataSourceConfig();

	/**
	 * create order and return its id
	 * 
	 * @param order order
	 * @return order id
	 */
	public int createOrder(Order order) throws Exception {
		int randomOrderId = (int) (Math.random() * 10000001);
		order.setId(randomOrderId);
		order.setStatus(Status.RECEIVED.name());
		try (Connection con = getConnection()) {
			con.setAutoCommit(false);
			long startTime=System.currentTimeMillis();
			try (PreparedStatement pst = this.createOrderPreparedStatement(con, order)) {
				pst.executeUpdate();

				for (OrderItem orderItem : order.getOrderItems()) {
					int randomOrderItemId = (int) (Math.random() * 100000001);
					orderItem.setId(randomOrderItemId);
					orderItem.setOrderId(randomOrderId);
					try (PreparedStatement psti = this.createOrderItemPreparedStatement(con, orderItem)) {
						psti.executeUpdate();
					}
				}
				con.commit();
			} catch (SQLException ex) {
				con.rollback();
				throw ex;
			} finally {
				con.setAutoCommit(true);
			}
			long endTime=System.currentTimeMillis();
			System.out.println("time to create an order in ms "+(endTime-startTime)); 
		}
		
		return randomOrderId;
	}
	
	
	/**
	 * updates order status by order id
	 * 
	 * @param id - order id
	 * @param status order status to set for order
	 * @return order id
	 */
	public int updateOrderStatusByOrderId(int id, String status) throws Exception {
		try (Connection con = getConnection()) {
			try (PreparedStatement pst = this.updateOrderStatusByOrderIdPreparedStatement(con, id, status)) {
				pst.executeUpdate();
			}
		}
		return id;
	}


	/**
	 * returns order by its id with order items
	 * 
	 * @param id -order id
	 * @return
	 * @throws Exception
	 */
	public Optional<Order> getOrderById(int id) throws Exception {
		try (Connection con = getConnection();
				PreparedStatement pst = this.getOrderByIdPreparedStatement(con, id);
				ResultSet rs = pst.executeQuery()) {
			if (rs.next()) {

				int userId = rs.getInt("user_id");
				int totalValue = rs.getInt("total_value");
				String status = rs.getString("status");
				final Order order = new Order();
				order.setId(id);
				order.setUserId(userId);
				order.setTotalValue(totalValue);
				order.setStatus(status);

				Set<OrderItem> orderItems = new HashSet<>();

				try (PreparedStatement psti = this.getOrderItemsByOrderIdPreparedStatement(con, id);
						ResultSet rsi = psti.executeQuery()) {
					while (rsi.next()) {
						int itemId = rsi.getInt("id");
						int productId = rsi.getInt("product_id");
						int value = rsi.getInt("value");
						int quantity = rsi.getInt("quantity");

						final OrderItem orderItem = new OrderItem();
						orderItem.setId(itemId);
						orderItem.setProductId(productId);
						orderItem.setOrderId(id);
						orderItem.setQuantity(quantity);
						orderItem.setValue(value);
						orderItems.add(orderItem);
					}
				}
				order.setOrderItems(orderItems);
				return Optional.of(order);
			} else {
				return Optional.empty();
			}
		}
	}

	
	
	private PreparedStatement updateOrderStatusByOrderIdPreparedStatement(Connection con, int id, String status) throws SQLException {
		PreparedStatement pst = con.prepareStatement("UPDATE orders SET status=? WHERE id=?");
		pst.setString(1, status);
		pst.setInt(2, id);
		return pst;
	}


	private PreparedStatement createOrderPreparedStatement(Connection con, Order order) throws SQLException {
		PreparedStatement pst = con.prepareStatement("INSERT INTO orders VALUES (?, ?, ?, ?) ");
		pst.setInt(1, order.getId());
		pst.setInt(2, order.getUserId());
		pst.setInt(3, order.getTotalValue());
		pst.setString(4, order.getStatus());
		return pst;
	}

	private PreparedStatement createOrderItemPreparedStatement(Connection con, OrderItem orderItem)
			throws SQLException {
		PreparedStatement pst = con.prepareStatement("INSERT INTO order_items VALUES (?, ?, ?, ?, ?)");
		pst.setInt(1, orderItem.getId());
		pst.setInt(2, orderItem.getProductId());
		pst.setInt(3, orderItem.getOrderId());
		pst.setInt(4, orderItem.getValue());
		pst.setInt(5, orderItem.getQuantity());
		return pst;
	}

	private PreparedStatement getOrderByIdPreparedStatement(Connection con, int id) throws SQLException {
		PreparedStatement pst = con.prepareStatement("SELECT * FROM orders WHERE id = ?");
		pst.setInt(1, id);
		return pst;
	}

	private PreparedStatement getOrderItemsByOrderIdPreparedStatement(Connection con, int orderId) throws SQLException {
		PreparedStatement pst = con.prepareStatement("SELECT * FROM order_items WHERE order_id = ?");
		pst.setInt(1, orderId);
		return pst;
	}
	
	private static final Connection getConnection() throws SQLException {
		 return DsqlDataSourceConfig.getPooledConnection();
		//return DsqlDataSourceConfig.getJDBCConnection();
	}
}
