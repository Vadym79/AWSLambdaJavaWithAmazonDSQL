// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.order.handler;

import java.util.Map;
import java.util.Optional;

import org.crac.Core;
import org.crac.Resource;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import software.amazon.awssdk.http.HttpStatusCode;
import software.amazonaws.example.order.dao.OrderDao;
import software.amazonaws.example.order.entity.Order;

public class GetOrderByIdWithPrimingHandler
		implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>, Resource {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final OrderDao orderDao = new OrderDao();
	static {
		objectMapper.registerModule(new JavaTimeModule());
	}
	
	public GetOrderByIdWithPrimingHandler() {
		Core.getGlobalContext().register(this);
	}
	
	
	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
		String id = requestEvent.getPathParameters().get("id");
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

	
	@Override
	public void beforeCheckpoint(org.crac.Context<? extends Resource> context) throws Exception {
		long startTime = System.currentTimeMillis();
	    this.beforeCheckpointFullPriming(context);
	    long endTime = System.currentTimeMillis();
		System.out.println("time to prime the order in ms " + (endTime - startTime));
	}
	

	
	private void beforeCheckpointFullPriming(org.crac.Context<? extends Resource> context) throws Exception {
		APIGatewayProxyRequestEvent requestEvent = LambdaEventSerializers.serializerFor(APIGatewayProxyRequestEvent.class, ClassLoader.getSystemClassLoader())
				.fromJson(getAPIGatewayProxyRequestEventAsJson());
		this.handleRequest(requestEvent, new MockLambdaContext());
	}
	

	@SuppressWarnings("unused")
	private void beforeCheckpointDynamoDBRequestPriming(org.crac.Context<? extends Resource> context) throws Exception {
		orderDao.getOrderById(0);
	}

	private static String getAPIGatewayProxyRequestEventAsJson() throws Exception {
		final APIGatewayProxyRequestEvent proxyRequestEvent = new APIGatewayProxyRequestEvent();
		proxyRequestEvent.setHttpMethod("GET");
		proxyRequestEvent.setPathParameters(Map.of("id", "0"));
		return objectMapper.writeValueAsString(proxyRequestEvent);
	}

	@Override
	public void afterRestore(org.crac.Context<? extends Resource> context) throws Exception {
	}

}