package com.vaadin.tests.integration.server;

import java.io.File;
import java.io.InputStream;

import org.junit.Test;

import com.vaadin.testbench.parallel.TestNameSuffix;
import com.vaadin.tests.integration.util.TestServerConnection;

@TestNameSuffix(property = "server-name")
public class ITJetty9 {

	// public List<String> getServersToTest() {
	// return Collections.singletonList("jetty9");
	// }

	@Test
	public void startServerAndRunTest() throws Exception {
		TestServerConnection conn = new TestServerConnection("jetty9");
		conn.lock();
		try {
			// TODO 1. Send base files
			
			// 2. Send demo.war
			conn.sendFile(new File(System.getProperty("war")), "demo.war");
			// 3. Start server and wait for demo to be deployed
			conn.startServerAndDeploy();
			
			// 4. TODO Server running at jetty9:8080, execute TB test suite
			
			// 5. Stop server and clean everything up (also releases lock) 
			conn.stopServerAndCleanup();
		} catch (Exception e) {
			conn.unlock();
		}
		conn.disconnect();
	}

	private InputStream findWar() {
		System.out.println("Prop: " + System.getProperty("war"));
		return null;
	}
}
