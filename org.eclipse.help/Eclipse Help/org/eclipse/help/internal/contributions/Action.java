package org.eclipse.help.internal.contributions;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
