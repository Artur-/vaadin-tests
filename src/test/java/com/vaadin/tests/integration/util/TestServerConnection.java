package com.vaadin.tests.integration.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Logger;
import com.jcraft.jsch.Session;

public class TestServerConnection {

	private static final String DEFAULT_USERNAME = "integration";

	private String serverName;

	private Session session;

	private JSch jsch;

	private static String sshDir = System.getProperty("user.home") + "/.ssh/";
	private static String[] publicKeys = new String[] {
			System.getProperty("sshkey.file"), sshDir + "id_rsa2",
			sshDir + "id_dsa", sshDir + "id_rsa" };

	public TestServerConnection(String serverName) throws JSchException {
		this.serverName = serverName;
		connectIfNeeded();
	}

	public void lock() throws IOException, JSchException {
		System.out.println("Locking machine " + serverName);
		executeAntCommand("get-lock");
	}

	public void unlock() throws IOException, JSchException {
		System.out.println("Unlocking machine " + serverName);
		executeAntCommand("release-lock");
	}

	public void sendFile(File source, String destination) throws IOException,
			JSchException {
		System.out.println("Sending " + source.getAbsolutePath() + " ("
				+ (source.length() / 1024 / 1024) + "MB) to " + destination);
		if (!source.exists() || !source.isFile())
			throw new IOException("File " + source.getName()
					+ " does not exist");

		String command = "scp -t " + destination;
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);

		OutputStream out = channel.getOutputStream();
		InputStream in = channel.getInputStream();

		channel.connect();

		if (checkAck(in) != 0) {
			throw new IOException("Expected ack");
		}

		// send "C0644 filesize filename", where filename should not include '/'
		long filesize = source.length();
		command = "C0644 " + filesize + " ";
		command += source.getName();
		command += "\n";
		out.write(command.getBytes());
		out.flush();
		if (checkAck(in) != 0) {
			throw new IOException("Expected ack");
		}

		// send content of file
		FileInputStream fis = new FileInputStream(source);
		BufferedOutputStream bout = new BufferedOutputStream(out, 1024 * 1024);
		IOUtils.copy(fis, bout);
		fis.close();
		bout.flush();

		// send '\0'
		IOUtils.write("\0", out);
		out.flush();
		if (checkAck(in) != 0) {
			System.exit(0);
		}
		bout.close();
		channel.disconnect();

	}

	private static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				System.out.print(sb.toString());
			}
			if (b == 2) { // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}

	public String executeAntCommand(String cmd) throws JSchException,
			IOException {

		ChannelExec execChannel = (ChannelExec) session.openChannel("exec");
		BufferedReader in = new BufferedReader(new InputStreamReader(
				execChannel.getInputStream()));
		execChannel.setCommand("ant -f deploy.xml " + cmd);
		execChannel.connect();
		try {
			String result = IOUtils.toString(in);
			if (!result.contains("BUILD SUCCESSFUL")) {
				throw new IOException("Unexpected output from ant command '"
						+ cmd + "':\n" + result);
			}

			return result;
		} finally {
			execChannel.disconnect();
		}
	}

	private void connectIfNeeded() throws JSchException {
		if (jsch != null) {
			return;
		}
		jsch = new JSch();

		for (String publicKey : publicKeys) {
			if (publicKey != null && new File(publicKey).exists()) {
				System.out.println("Using " + publicKey);
				jsch.addIdentity(publicKey);
				// break;
			}
		}
		JSch.setLogger(new DebugLogger());
		session = jsch.getSession(DEFAULT_USERNAME, getHostName());
		session.setUserInfo(new DummyUserInfo());
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect();

	}

	protected String getHostName() {
		return serverName + ".devnet.vaadin.com";
	}

	public void disconnect() {
		session.disconnect();
	}

	public static class DebugLogger implements Logger {

		@Override
		public boolean isEnabled(int level) {
			return true;
		}

		@Override
		public void log(int level, String message) {
			System.out.println(level + " " + message);

		}

	}

	public void startServerAndDeploy() throws JSchException, IOException {
		String result = executeAntCommand("startup-and-deploy");
		if (!result.contains("Demo deployed successfully"))
			throw new IOException("Demo failed to deploy: " + result);

	}

	public void stopServerAndCleanup() throws JSchException, IOException {
		executeAntCommand("shutdown-and-cleanup");
	}
}
