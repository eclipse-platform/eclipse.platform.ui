package org.eclipse.help.internal.contributions;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.lang.*;

/**
 * Views contribution
 */
public interface InfoSet extends Contribution {
	/**
	 */
	String getHref();
	/**
	 */
	InfoView getView(String name);
	/**
	 */
	String[] getViewNames();
	/**
	 */
	InfoView[] getViews();
	/**
	 * Returns true when this actions are meant for a stand-alone component
	 * installation.
	 */
	public boolean isStandalone();
}
