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
import com.appdynamics.sample.servlet.PaymentCardInfo;
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
		
		getPaymentDetails();
		
		ResultPrinter.addResult(req, resp, "Credit Card Payment", 
				"Stub", "Credit Card Payment Stub", null);
		
		req.getRequestDispatcher("jsp/response.jsp").forward(req, resp);
	}
	
	private PaymentCard getPaymentDetails() {
		return PaymentCardInfo.instance().getCard();
	}
}
