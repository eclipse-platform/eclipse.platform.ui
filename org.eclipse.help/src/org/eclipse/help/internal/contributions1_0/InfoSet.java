package org.eclipse.help.internal.contributions1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
