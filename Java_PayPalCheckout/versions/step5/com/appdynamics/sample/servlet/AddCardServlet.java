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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;

import com.appdynamics.sample.util.ResultPrinter;
import com.appdynamics.sample.servlet.PaypalDemoServlet;

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
@WebServlet("/addcard")
public class AddCardServlet extends PaypalDemoServlet {

	static String PAGE_HEADER = "<html><head><title>Welcome to Our Online PayPal Store</title></head><body>";

	static String PAGE_FOOTER = "</body></html>";

	Logger logger = Logger.getLogger(AddCardServlet.class);

	public AddCardServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String authToken = null;
		String cardInfo = null;

		String doAbortParam = req.getParameter("abort");
		String resetPool = req.getParameter("reset");

		boolean reset = (resetPool != null && resetPool.trim().length() > 0);
		boolean abort = (doAbortParam == null || doAbortParam.trim().length() == 0);

		logger.info("Processing payment request, abort == " + abort);

		try {
			
			if (reset) {
				resetAuthWebClientPool();
			}
			else {
				String userID = (abort) ? null : "Mr. Rogers";
				authToken = callAuthService((doAbortParam != null), userID);

				cardInfo = addCard(authToken);
			}
		} catch (InvalidCardException e) {
			logger.fatal("Handling invalid card format exception");
			e.printStackTrace();

			throw new ServletException(e);
		} catch (Exception e) {
			e.printStackTrace();

			throw new ServletException(e);
		}

		logger.info("Successfully processed payment request");

		ResultPrinter.addResult(req, resp, "Added Credit Card", 
				"Visa - Auth Token (" + authToken + ")", cardInfo, null);
		
		req.getRequestDispatcher("jsp/response.jsp").forward(req, resp);
	}


	/**
	 * adds a new credit card, actually calls a mid-tier service which then calls to paypal.
	 * 
	 * If authToken == null then simulates an error by throwing an InvalidCardFormatException
	 * 
	 * @param authToken
	 * @return
	 * @throws InvalidCardException 
	 */
	private String addCard(String authToken) throws InvalidCardException {
		String host = "http://localhost:7090";
		String service = "/service/v1/paypal/card/credit/create/" + 
				authToken + "/4111111111111111/visa";

		/**
    	WebClient client = 
    			clientHelper.getWebClient(host, service, true, 10);
		 */
		WebClient client = 	
				WebClient.create(host).path(service);

		if (client == null) {
			logger.fatal("Failed to create web client to invoke payment service");

			return null;
		}
		else {
			logger.info("Successfully got web client from pool for [ " + host + " : " + service + " ]");
		}

		client.type(MediaType.TEXT_PLAIN);
		client.accept("text/plain", "text/html");

		return client.get(String.class);
	}
}
