package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.widgets.Control;
import org.eclipse.help.internal.contributions.Topic;

/**
 * Help browser
 */
public interface IBrowser {

	public int back();
	public int copy();
	public int forward();
	/**
	 * Returns the actual control object
	 */
	public Control getControl();
	public String getLocationURL();
	public int home();
	/**
	 * Navigate to the specified URL
	 */
	public int navigate(String url);
	public int print();
	/**
	 * Print a Topic and all it's children.
	 */
	public void printFullTopic(Topic rootTopic);
}
