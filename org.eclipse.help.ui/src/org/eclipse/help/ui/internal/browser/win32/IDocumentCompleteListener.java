/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.win32;
public interface IDocumentCompleteListener {
	/**
	 * @param url URL of the document that completed loading
	 */
	public void documentComplete(String url);
}