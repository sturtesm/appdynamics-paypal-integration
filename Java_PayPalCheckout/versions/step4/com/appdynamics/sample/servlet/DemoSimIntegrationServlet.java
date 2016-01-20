package com.appdynamics.sample.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.appdynamics.sample.util.ResultPrinter;

@SuppressWarnings("serial")
@WebServlet("/demosim")
public  class DemoSimIntegrationServlet extends PaypalDemoServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	Logger logger = Logger.getLogger(DemoSimIntegrationServlet.class);


	public DemoSimIntegrationServlet() {

	}

	private String getWebTierPort() {
		String response = "";

		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://127.0.0.1:8000/getservice?service=WebTier");

		try {
			HttpResponse execute = client.execute(httpGet);
			InputStream content = execute.getEntity().getContent();

			BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
			String s = "";
			while ((s = buffer.readLine()) != null) {
				response += s;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String ports = getWebTierPort();

		String pattern = "(\\d+)";

		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);

		// Now create matcher object.
		Matcher m = r.matcher(ports);
		
		
		System.out.println("ports = " + ports);
		
		if (m.find( )) {
			String port = m.group(0);
			
			String response = callWebTier(new Integer(port));
			
			if (response == null || response.trim().length() <= 0) {
				response = "FAIL";
			}
			
			ResultPrinter.addResult(req, resp, "Web Tier Service Ports", 
					"Response Calling http://127.0.0.1:" + port + "/WebTier/Login/0", response, null);
		}
		else {
			ResultPrinter.addResult(req, resp, "Web Tier Service Ports", 
					"Web Tier Service Ports", "No response Found", null);
		}

		req.getRequestDispatcher("jsp/response.jsp").forward(req, resp);

	}

	private String callWebTier(Integer port) {
		String response = "";
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://127.0.0.1:" + port + "/WebTier/Login/0");

		try {
			HttpResponse execute = client.execute(httpGet);
			InputStream content = execute.getEntity().getContent();

			BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
			String s = "";
			while ((s = buffer.readLine()) != null) {
				response += s;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return response;

	}

}
