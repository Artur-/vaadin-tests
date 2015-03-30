package com.vaadin.tests.integration.util;

import com.jcraft.jsch.UserInfo;

public class DummyUserInfo implements UserInfo {

	@Override
	public String getPassphrase() {
		System.out.println("getPassphrase");
		return "";
	}

	@Override
	public String getPassword() {
		System.out.println("getPassword");
		return "5sivi";
	}

	@Override
	public boolean promptPassword(String message) {
		System.out.println("promptPassword");
		return true;
	}

	@Override
	public boolean promptPassphrase(String message) {
		System.out.println("promptPasshrase");
		return true;
	}

	@Override
	public boolean promptYesNo(String message) {
		System.out.println("yesno?");
		return false;
	}

	@Override
	public void showMessage(String message) {
		// TODO Auto-generated method stub

	}

}
