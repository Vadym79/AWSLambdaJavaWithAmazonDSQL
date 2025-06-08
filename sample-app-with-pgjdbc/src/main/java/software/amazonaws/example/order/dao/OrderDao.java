package software.amazonaws.example.order.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import software.amazonaws.example.order.entity.Order;

public class OrderDao {

	public Optional<Order> getOrderById(int id) throws Exception {
		try (Connection con = DsqlDataSourceConfig.getPooledConnection();
				PreparedStatement pst = this.createPreparedStatement(con, id);
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

	private PreparedStatement createPreparedStatement(Connection con, int id) throws SQLException {
		PreparedStatement pst = con.prepareStatement("select * from orders where id = ?");
		pst.setInt(1, id);
		return pst;
	}
}
