/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jface.text.contentassist;


/**
 * For internal use only. Not API. <p>
 * A context information extension is for extending
 * <code>IContextInformationExtension</code> instances with new functionality.
*/
public interface IContextInformationExtension {

	/**
	 * Returns the position to which the computed context information refers to.
	 * 
	 * @return the position to which the context information refers to
	 */
	int getContextInformationPosition();
}
