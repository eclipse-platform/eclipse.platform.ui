package org.eclipse.ui.editors.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Document provider for <code>IStorage</code> based domain elements.
 */ 
public interface IStorageDocumentProvider {
	
	/**
	 * Returns the default encoding used by this provider.
	 * 
	 * @return the default encoding used  by this provider
	 */
	String getDefaultEncoding();
	
	/**
	 * Returns the encoding for the given element, or 
	 * <code>null</code> if the element is not managed by this provider.
	 * 
	 * @param element the element
	 * @return the encoding for the given element
	 */
	String getEncoding(Object element);
	
	/**
	 * Sets the encoding for the given element. If <code>encoding</code>
	 * is <code>null</code> the workbench encoding should be used.
	 * 
	 * @param element the element
	 * @param encoding the encoding to be used
	 */
	void setEncoding(Object element, String encoding);
}
