package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.action.Action;

public abstract class TestBrowserAction extends Action {
	private TestBrowser browser;
	public TestBrowserAction(String label, TestBrowser browser) {
		super(label);
		this.browser = browser;
	}
	/**
	 * Returns the test browser.
	 */
	public TestBrowser getBrowser() {
		return browser;
	}
}
