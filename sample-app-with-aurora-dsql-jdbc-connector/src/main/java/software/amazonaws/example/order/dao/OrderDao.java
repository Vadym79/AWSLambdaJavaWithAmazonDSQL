package software.amazonaws.example.order.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import software.amazonaws.example.order.entity.Order;
import software.amazonaws.example.order.entity.Order.Status;
import software.amazonaws.example.order.entity.OrderItem;

public class OrderDao {
	
	
	@SuppressWarnings("unused")
	private static final DsqlDataSourceConfig dsqlDataSourceConfig=new DsqlDataSourceConfig();
	
	/**
	 * create order and return its id
	 * 
	 * @param order order
	 * @return order id
	 */
	public int createOrder(Order order) throws Exception {
		//int randomOrderId = (int) (Math.random() * 100000001);
		int startItemId=34001;
		for(int i=17000 ; i <=  32767 ; i++) {
		order.setId(i);
		order.setDateTime(LocalDateTime.now());
		order.setStatus(Status.RECEIVED.name());
		try (Connection con = getConnection()) {
			con.setAutoCommit(false);
			long startTime=System.currentTimeMillis();
			try (PreparedStatement pst = this.createOrderPreparedStatement(con, order)) {
				long startTimeCreateOrder=System.currentTimeMillis();
				pst.executeUpdate();
				long endTimeCreateOrder=System.currentTimeMillis();
				System.out.println("time to create order with id  " +i+ " in the database in ms "+(endTimeCreateOrder-startTimeCreateOrder)); 
				
				for (OrderItem orderItem : order.getOrderItems()) {
					//int randomOrderItemId = (int) (Math.random() * 1000000001);
					orderItem.setId(startItemId);
					orderItem.setOrderId(i);
					startItemId++;
					try (PreparedStatement psti = this.createOrderItemPreparedStatement(con, orderItem)) {
						long startTimeCreateOrderItem=System.currentTimeMillis();
						psti.executeUpdate();
						long endTimeCreateOrderItem=System.currentTimeMillis();
						System.out.println("time to create order item with id  " +startItemId+ " and order id "+ i + " in the database in ms "+(endTimeCreateOrderItem-startTimeCreateOrderItem)); 
						
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
		
		}
		return 0;
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
		long startTime=System.currentTimeMillis();
		try (Connection con = getConnection();
				PreparedStatement pst = this.getOrderByIdPreparedStatement(con, id);
				ResultSet rs = pst.executeQuery()) {
			long endTimeGetOrder=System.currentTimeMillis();
			System.out.println("time to get an order by id  " +id+ " from the database in ms "+(endTimeGetOrder-startTime)); 
			if (rs.next()) {
				int userId = rs.getInt("user_id");
				int totalValue = rs.getInt("total_value");
				String status = rs.getString("status");
				Timestamp createdTs=rs.getTimestamp("created");
				LocalDateTime created=null;
				if(createdTs != null) {
					created=createdTs.toLocalDateTime();
				}
				final Order order = new Order();
				order.setId(id);
				order.setUserId(userId);
				order.setTotalValue(totalValue);
				order.setStatus(status);
				order.setDateTime(created);
				
				Set<OrderItem> orderItems = new HashSet<>();
				long startTimeGetOrderItem=System.currentTimeMillis();
				try (PreparedStatement psti = this.getOrderItemsByOrderIdPreparedStatement(con, id);
						ResultSet rsi = psti.executeQuery()) {
					long endTimeGetOrderItem=System.currentTimeMillis();
					System.out.println("time to get an order item by order id  " +id+ " from the database in ms "+(endTimeGetOrderItem-startTimeGetOrderItem)); 
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
				long endTime=System.currentTimeMillis();
				System.out.println("time to get an order by id " +id+ " in ms "+(endTime-startTime)); 
				return Optional.of(order);
			} else {
				return Optional.empty();
			}
		}
	}
	
	
	/** return orders created between start and dates
	 * 
	 * @param startDate
	 * @param endDate
	 * @return orders created between start and dates
	 * @throws Exception
	 */
	public Set<Order> getOrdersByCreatedDates(LocalDateTime startDate, LocalDateTime endDate) throws Exception {
		
		Set<Order> orders=new HashSet<Order>();
		try (Connection con = getConnection();
				PreparedStatement pst = this.getOrdersByCreatedDatesPreparedStatement(con, startDate, endDate);
				ResultSet rs = pst.executeQuery()) {
			//long endTimeGetOrder=System.currentTimeMillis();
			while (rs.next()) {
				//long startTime=System.currentTimeMillis();
				int id = rs.getInt("id");
				//System.out.println("time to get an order by id  " +id+ " from the database in ms "+(endTimeGetOrder-startTime)); 
				int userId = rs.getInt("user_id");
				int totalValue = rs.getInt("total_value");
				String status = rs.getString("status");
				Timestamp createdTs=rs.getTimestamp("created");
				LocalDateTime created=null;
				if(createdTs != null) {
					created=createdTs.toLocalDateTime();
				}
				final Order order = new Order();
				order.setId(id);
				order.setUserId(userId);
				order.setTotalValue(totalValue);
				order.setStatus(status);
				order.setDateTime(created);
				
				Set<OrderItem> orderItems = new HashSet<>();
				
				/*
				try (PreparedStatement psti = this.selectSessionID(con);
						ResultSet rsi = psti.executeQuery()) {
					if (rsi.next()) {
						String sessionID=rsi.getString(1);
						System.out.println("sessionID "+sessionID);
				   }
				}
				*/
				//long startTimeGetOrderItem=System.currentTimeMillis();
				try (PreparedStatement psti = this.getOrderItemsByOrderIdPreparedStatement(con, id);
						ResultSet rsi = psti.executeQuery()) {
					//long endTimeGetOrderItem=System.currentTimeMillis();
					//System.out.println("time to get an order item by order id  " +id+ " from the database in ms "+(endTimeGetOrderItem-startTimeGetOrderItem)); 
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
				//long endTime=System.currentTimeMillis();
				//System.out.println("time to get an order by id " +id+ " in ms "+(endTime-startTime)); 
				orders.add(order);
			} 
		}
		return orders;
	}

	
	
	private PreparedStatement updateOrderStatusByOrderIdPreparedStatement(Connection con, int id, String status) throws SQLException {
		PreparedStatement pst = con.prepareStatement("UPDATE orders SET status=? WHERE id=?");
		pst.setString(1, status);
		pst.setInt(2, id);
		return pst;
	}

	
	
	
	private PreparedStatement selectSessionID(Connection con) throws SQLException {
		PreparedStatement pst = con.prepareStatement("SELECT sys.current_session_id();");
		return pst;
	}


	private PreparedStatement createOrderPreparedStatement(Connection con, Order order) throws SQLException {
		PreparedStatement pst = con.prepareStatement("INSERT INTO orders VALUES (?, ?, ?, ?, ?) ");
		pst.setInt(1, order.getId());
		pst.setInt(2, order.getUserId());
		pst.setInt(3, order.getTotalValue());
		pst.setString(4, order.getStatus());
		if(order.getDateTime() !=null) {
		   pst.setTimestamp(5, Timestamp.valueOf(order.getDateTime()));
		} else {
			pst.setTimestamp(5,null);
		}
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
	
	private PreparedStatement getOrdersByCreatedDatesPreparedStatement(Connection con, LocalDateTime startDate,LocalDateTime endDate) throws SQLException {
		PreparedStatement pst = con.prepareStatement("SELECT * FROM orders WHERE created BETWEEN ? and ? LIMIT 10");
		pst.setTimestamp(1, Timestamp.valueOf(startDate));
		pst.setTimestamp(2, Timestamp.valueOf(endDate));
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
