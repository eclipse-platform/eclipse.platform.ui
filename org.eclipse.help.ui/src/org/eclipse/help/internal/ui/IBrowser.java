package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.Control;
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
	/**
	 * @param showPrintDialog
	 * set to true to cause print dialog to be displayed
	 */
	public int print(boolean showPrintDialog);
	/**
	 * Adds listener for DocumentComplete events
	 */
	public void addDocumentCompleteListener(IDocumentCompleteListener listener);
	/**
	 * Adds listener for DocumentComplete events
	 */
	public void removeDocumentCompleteListener(IDocumentCompleteListener listener);
	/**
	 * Disposes of control
	 */
	public void dispose();
}