package org.eclipse.jface.text;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Extension interface for <code>IInformationControl</code>.
 */ 
public interface IInformationControlExtension {
	
	/**
	 * Returns whether this information control has contents to be displayed.
	 * @return <code>true</code> if there is contents to be displayed.
	 */
	boolean hasContents();
}