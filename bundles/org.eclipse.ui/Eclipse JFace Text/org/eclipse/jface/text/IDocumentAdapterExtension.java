package org.eclipse.jface.text;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Extension to <code>IDocumentAdapter</code>.
 */
public interface IDocumentAdapterExtension {
	
	/**
	 * Stops forwarding document changes to the styled text.
	 */
	void stopForwardingDocumentChanges();
	
	/**
	 * Resumes forwarding document changes to the styled text.
	 * Also forces the styled text to catch up with all the changes
	 * that have been applied since <code>stopTranslatingDocumentChanges</code>
	 * has been called.
	 */
	void resumeForwardingDocumentChanges();
}
