package org.eclipse.help.internal.contributions1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.lang.*;

/**
 * Action contribution
 */
public interface Action extends Contribution {
	public String getView();
	/**
	 * Returns true when this actions are meant for a stand-alone component
	 * installation.
	 */
	public boolean isStandalone();
}
