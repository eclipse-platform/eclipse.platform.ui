package org.eclipse.help.internal.ui;

public interface IDocumentCompleteListener {
	/**
	 * @param url URL of the document that completed loading
	 */
	public void documentComplete(String url);
}

