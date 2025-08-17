
package software.amazonaws.example.order.handler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import software.amazon.awssdk.http.HttpStatusCode;
import software.amazonaws.example.order.dao.OrderDao;
import software.amazonaws.example.order.entity.Order;

public class GetOrdersByCreatedDates2Handler
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
    	
    	String startDate = params.get("startDate");
    	String endDate = params.get("endDate");
    	
    	System.out.println("orders to retrieve between "+startDate+ " end "+endDate);
    	
    	try {
    	startDate=decode(startDate);
    	endDate=decode(endDate);
    	System.out.println("orders to retrieve between decoded "+startDate+ " end "+endDate);
		
			// LocalDateTime.parse("2025-08-02T19:50:55");
			Set<Order> orders = orderDao.getOrdersByCreatedDates(LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
			if (orders.isEmpty()) {
				context.getLogger().log("No orders found created between "+startDate+ " and "+endDate);
				return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.NOT_FOUND)
						.withBody("No orders found created between "+startDate+ " and "+endDate);
			}
			context.getLogger().log(" Orders size " + orders.size() + " found ");
			return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.OK)
					.withBody(objectMapper.writeValueAsString(orders));
		} catch (Exception je) {
			je.printStackTrace();
			return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
					.withBody("Internal Server Error :: " + je.getMessage());
		}
	}

	private static String decode(String value) throws UnsupportedEncodingException {
	    return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
	}
}