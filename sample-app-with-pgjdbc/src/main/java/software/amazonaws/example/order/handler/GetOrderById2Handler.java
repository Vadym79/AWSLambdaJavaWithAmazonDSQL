
package software.amazonaws.example.order.handler;

import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import software.amazon.awssdk.http.HttpStatusCode;
import software.amazonaws.example.order.dao.OrderDao;
import software.amazonaws.example.order.entity.Order;

public class GetOrderById2Handler
		implements RequestHandler<Map<String, String>, APIGatewayProxyResponseEvent> {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.registerModule(new JavaTimeModule());
	}
	private static final OrderDao orderDao = new OrderDao();


	@Override
	public APIGatewayProxyResponseEvent handleRequest(Map<String, String> params, Context context) {
        
		System.out.println("params: "+params);
	  
        if(context.getClientContext() != null) {
    	 System.out.println("custom map "+context.getClientContext().getCustom());
        }
    	
    	String id = params.get("id");
    	System.out.println("order id to retrieve "+id);
		try {
			Optional<Order> optionalOrder = orderDao.getOrderById(Integer.valueOf(id));
			if (optionalOrder.isEmpty()) {
				context.getLogger().log(" order with id " + id + " not found ");
				return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.NOT_FOUND)
						.withBody("order with id = " + id + " not found");
			}
			context.getLogger().log(" order " + optionalOrder.get() + " found ");
			return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.OK)
					.withBody(objectMapper.writeValueAsString(optionalOrder.get()));
		} catch (Exception je) {
			je.printStackTrace();
			return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
					.withBody("Internal Server Error :: " + je.getMessage());
		}
	}

}