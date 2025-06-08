package software.amazonaws.example.order.entity;

public class OrderItem  {
	
	private int id; 
	private int productId; 
	private int orderId; 
	private int value; 
	private int quantity;
	
	public int getId() {
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getProductId() {
		return this.productId;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public int getOrderId() {
		return this.orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public int getValue() {
		return this.value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public int getQuantity() {
		return this.quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	@Override
	public String toString() {
		return "OrderItem [id=" + this.id + ", productId=" + this.productId + ", orderId=" + this.orderId + ", value=" + this.value
				+ ", quantity=" + this.quantity + "]";
	}
	
	
}
