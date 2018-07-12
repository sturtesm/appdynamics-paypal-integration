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
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;

import com.appdynamics.sample.rest.client.WebClientPoolHelper;
import com.appdynamics.sample.util.ResultPrinter;

/**
 * <p>
 * A simple servlet taking advantage of features added in 3.0.
 * </p>
 * 
 * <p>
 * The servlet is registered and mapped to /accountBalance
 * </p>
 * 
 */
@SuppressWarnings("serial")
@WebServlet("/accountHistory")
public class AccountServlet extends PaypalDemoServlet {

	static String PAGE_HEADER = "<html><head><title>Welcome to Our Online PayPal Store</title></head><body>";

	static String PAGE_FOOTER = "</body></html>";

	Logger logger = Logger.getLogger(AccountServlet.class);

	public AccountServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String accountDetails = null;
		
		String doAbortParam = req.getParameter("abort");
		String resetPool = req.getParameter("reset");

		boolean reset = (resetPool != null && resetPool.trim().length() > 0);
		boolean abort = (doAbortParam == null || doAbortParam.trim().length() == 0);

		logger.info("Processing request for account history, abort == " + abort);

		try {
			
			if (reset) {
				resetAuthWebClientPool();
			}
			else {
				String userId = (abort) ? null : "Steve S";
				String authorization = callAuthService((doAbortParam != null), null);
				
				/** process the payment request */
				accountDetails = getAccountHistory(authorization);
				
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

		ResultPrinter.addResult(req, resp, "Account History", 
				"History Length - Last 10 Payments", accountDetails, null);
		
		req.getRequestDispatcher("jsp/response.jsp").forward(req, resp);
		
	}


	/**
	 * Get the account history
	 * 
	 * If authToken == null then simulates an error by throwing an InvalidCardFormatException
	 * @param authorization 
	 * 
	 * @param authToken
	 * @return
	 * @throws InvalidCardException 
	 */
	private String getAccountHistory(String authorization) throws InvalidCardException {
		String host = "http://localhost:7090";
		String service = "/service/v1/paypal/payment/history/" + authorization;

		WebClient client = WebClient.create(host).path(service);

		if (client == null) {
			logger.fatal("Failed to create web client to invoke payment history service");

			return null;
		}
		else {
			logger.info("Successfully got web client from pool for [ " + host + " : " + service + " ]");
		}

		/** when we throw the exception we won't put the client back into the pool
		if (authToken == null) {
			throw new InvalidCardException ("Invalid Auth Token Exception, Payment Auth Token != null");
		}
		*/

		client.type(MediaType.TEXT_PLAIN);
		client.accept("text/plain", "text/html");

		return client.get(String.class);
	}

}
