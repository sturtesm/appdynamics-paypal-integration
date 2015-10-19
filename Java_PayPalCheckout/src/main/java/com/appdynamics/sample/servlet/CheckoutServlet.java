/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.appdynamics.sample.servlet;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;

import com.appdynamics.sample.servlet.PaymentCardInfo.PaymentCard;
import com.appdynamics.sample.util.ResultPrinter;

/**
 * <p>
 * A simple servlet taking advantage of features added in 3.0.
 * </p>
 * 
 * <p>
 * The servlet is registered and mapped to /login
 * </p>
 * 
 */
@SuppressWarnings("serial")
@WebServlet("/checkout")
public class CheckoutServlet extends PaypalDemoServlet {

	static String PAGE_HEADER = "<html><head><title>Welcome to Our Online PayPal Store</title></head><body>";

	static String PAGE_FOOTER = "</body></html>";

	Logger logger = Logger.getLogger(CheckoutServlet.class);

	public CheckoutServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String authToken = null;
		String paymentInfo = null;

		String doAbortParam = req.getParameter("abort");
		String resetPool = req.getParameter("reset");

		boolean reset = (resetPool != null && resetPool.trim().length() > 0);
		boolean abort = (doAbortParam == null || doAbortParam.trim().length() == 0);

		logger.info("Processing payment request, abort == " + abort);

		try {
			
			getPaymentDetails();
			
			if (reset) {
				resetAuthWebClientPool();
			}
			else {
				String userID = (abort) ? null : "Mr. Rogers";
				authToken = callAuthService((doAbortParam != null), userID);

				paymentInfo = initiatePayment(authToken);
			}
		} catch (Exception e) {
			e.printStackTrace();

			throw new ServletException(e);
		}

		logger.info("Successfully processed payment request");

		ResultPrinter.addResult(req, resp, "Credit Card Payment", 
				"Auth Token (paymentInfo)", paymentInfo, null);
		
		req.getRequestDispatcher("jsp/response.jsp").forward(req, resp);
	}


	/**
	 * initiate a payment, actually calls a mid-tier service which then calls to paypal.
	 * 
	 * If authToken == null then simulates an error by throwing an InvalidCardFormatException
	 * 
	 * @param authToken
	 * @return
	 * @throws InvalidCardException 
	 */
	private String initiatePayment(String authToken) throws InvalidCardException {
		
		PaymentCard paymentDetails = getPaymentDetails();
		
		if (paymentDetails.getCardType().equalsIgnoreCase("discover")) {
			throw new InvalidCardException("Error processing payment request, we don't take Discover yet!");
		}
		else {
			logger.info("Successfully initiating payment for " + 
					paymentDetails.getCardType() + " Card");
		}
		
		String host = "http://localhost:7090";
		String service = "/service/v1/paypal/payment/";

		WebClient client = 
				WebClient.create("http://localhost:7090").path(
						"/service/v1/paypal/payment/credit/create/" + authToken);

		if (client == null) {
			logger.fatal("Failed to create web client to invoke payment service");

			return null;
		}
		else {
			logger.info("Successfully got web client from pool for [ " + host + " : " + service + " ]");
		}

		/** when we throw the exception we won't put the client back into the pool */
		if (authToken == null) {
			throw new InvalidCardException ("Invalid Auth Token Exception, Payment Auth Token != null");
		}

		client.type(MediaType.TEXT_PLAIN);
		client.accept("text/plain", "text/html");

		return client.get(String.class);
	}

	private PaymentCard getPaymentDetails() {
		return PaymentCardInfo.instance().getCard();
	}
}
