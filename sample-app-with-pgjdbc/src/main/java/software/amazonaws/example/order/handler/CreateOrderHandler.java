// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.order.handler;

import org.crac.Core;
import org.crac.Resource;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.http.HttpStatusCode;
import software.amazonaws.example.order.dao.OrderDao;
import software.amazonaws.example.order.entity.Order;


public class CreateOrderHandler
		implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>, Resource  {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final OrderDao orderDao= new OrderDao();
	
	public CreateOrderHandler () {
		Core.getGlobalContext().register(this);
	}

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
		try {

			String requestBody = requestEvent.getBody();
			Order order = objectMapper.readValue(requestBody, Order.class);	
			int orderId=orderDao.createOrder(order);
			context.getLogger().log(" order with id "+orderId+" created");	
			return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.CREATED)
					.withBody("order with id = " + orderId + " created");
		} catch (Exception e) {
			e.printStackTrace();
			return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
					.withBody("Internal Server Error :: " + e.getMessage());
		}
	}

	@Override
	public void beforeCheckpoint(org.crac.Context<? extends Resource> context) throws Exception {
		long startTime=System.currentTimeMillis();
		orderDao.getOrderById(0);
		long endTime=System.currentTimeMillis();
		System.out.println("time to prime the order in ms "+(endTime-startTime));
	}

	@Override
	public void afterRestore(org.crac.Context<? extends Resource> context) throws Exception {
	}	
	
}