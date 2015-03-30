package com.vaadin.tests.integration;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

public class Servlets {

	@WebServlet(urlPatterns = "/VAADIN/*", name = "VaadinStaticFileServlet")
	public static class StaticFileServlet extends VaadinServlet {
	}

	@WebServlet(urlPatterns = "/servlet/*", name = "Servlet without push")
	@VaadinServletConfiguration(ui = ServletIntegrationUI.class, productionMode = false)
	public static class ServletServlet extends VaadinServlet {
	}

	//
	// @WebServlet(urlPatterns = "/push/streaming/*", name =
	// "Servlet with streaming", asyncSupported = true)
	// @VaadinServletConfiguration(ui = ServletIntegrationStreamingUI.class,
	// productionMode = false)
	// public static class StreamingServlet extends VaadinServlet {
	// }

	@WebServlet(urlPatterns = "/push/long-polling/*", name = "Servlet with long-polling", asyncSupported = true)
	@VaadinServletConfiguration(ui = ServletIntegrationLongPollingUI.class, productionMode = false)
	public static class LongPollingServlet extends VaadinServlet {
	}

	@WebServlet(urlPatterns = "/push/websocket/*", name = "Servlet with default websocket", asyncSupported = true)
	@VaadinServletConfiguration(ui = ServletIntegrationWebsocketUI.class, productionMode = false)
	public static class WebsocketServlet extends VaadinServlet {
	}

	@WebServlet(urlPatterns = "/push/websocket-jsr356/*", name = "Servlet with JSR356 websocket", asyncSupported = true)
	@VaadinServletConfiguration(ui = ServletIntegrationWebsocketJSR356UI.class, productionMode = false)
	public static class WebsocketJSR356Servlet extends VaadinServlet {
	}

}