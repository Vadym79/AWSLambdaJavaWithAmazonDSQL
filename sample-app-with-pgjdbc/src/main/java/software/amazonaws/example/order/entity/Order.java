package software.amazonaws.example.order.entity;

import java.util.Set;

public class Order {
	
	private int id; 
	private int userId; 
	private int totalValue;
	private Set<OrderItem> orderItems=Set.of();
	private String status ;
	
	public String getStatus() {
		return this.status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getId() {
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return this.userId;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getTotalValue() {
		return this.totalValue;
	}
	public void setTotalValue(int totalValue) {
		this.totalValue = totalValue;
	}
	
	public Set<OrderItem> getOrderItems() {
		return this.orderItems;
	}
	public void setOrderItems(Set<OrderItem> orderItems) {
		this.orderItems = orderItems;
	}
	@Override
	public String toString() {
		return "Order [id=" + this.id + ", userId=" + this.userId + ", totalValue=" + this.totalValue +
				", orderItems=" + this.orderItems+ ", status=" + this.status
				+ "]";
	}
	
	public enum Status {
		  RECEIVED,
		  SHIPPED
		}

	
}
