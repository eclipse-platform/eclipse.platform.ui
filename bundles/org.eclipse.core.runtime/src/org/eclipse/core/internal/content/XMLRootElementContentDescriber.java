/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentDescription;

/**
 * A content describer for detecting the name of the top-level element of the
 * DTD system identifier in an XML file. This supports two parameters:
 * <code>DTD_TO_FIND</code> and <code>ELEMENT_TO_FIND</code>. This is done
 * using the <code>IExecutableExtension</code> mechanism. If the
 * <code>":-"</code> method is used, then the value is treated as the
 * <code>ELEMENT_TO_FIND</code>.
 * 
 * @since 3.0
 */
public class XMLRootElementContentDescriber extends XMLContentDescriber implements IExecutableExtension {

	/**
	 * The name of the executable extension parameter containing the value of
	 * <code>dtdToFind</code>.
	 */

	public static final String DTD_TO_FIND = "dtd"; //$NON-NLS-1$

	/**
	 * The name of the executable extension parameter containing the value of
	 * <code>elementToFind</code>.
	 */
	public static final String ELEMENT_TO_FIND = "element"; //$NON-NLS-1$

	/**
	 * The system identifier that we wish to find. This value will be
	 * initialized by the <code>setInitializationData</code> method. If no
	 * value is provided, then this means that we don't care what the system
	 * identifier will be.
	 */
	private String dtdToFind = null;

	/**
	 * The top-level element we are looking for. This value will be initialized
	 * by the <code>setInitializationData</code> method. If no value is
	 * provided, then this means that we don't care what the top-level element
	 * will be.
	 */
	private String elementToFind = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.content.IContentDescriber#describe(java.io.InputStream,
	 *      org.eclipse.core.runtime.content.IContentDescription, int)
	 */
	public int describe(InputStream contents, IContentDescription description, int optionsMask) throws IOException {
		// call the basic XML describer to do basic recognition
		if (super.describe(contents, description, optionsMask) == INVALID)
			return INVALID;
		contents.reset();
		XMLRootHandler xmlHandler = new XMLRootHandler(elementToFind != null);
		if (!xmlHandler.parseContents(contents))
			return INVALID;
		// Check to see if we matched our criteria.
		if ((elementToFind != null) && (!elementToFind.equals(xmlHandler.getRootName())))
			return INVALID;
		if ((dtdToFind != null) && (!dtdToFind.equals(xmlHandler.getDTD())))
			return INVALID;
		// We must be okay then.		
		return VALID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
	 *      java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data) throws CoreException {
		if (data instanceof String)
			elementToFind = (String) data;
		else if (data instanceof Hashtable) {
			Hashtable parameters = (Hashtable) data;
			dtdToFind = (String) parameters.get(DTD_TO_FIND);
			elementToFind = (String) parameters.get(ELEMENT_TO_FIND);
		}
		if (dtdToFind == null && elementToFind == null) {
			String message = Policy.bind("content.badInitializationData", XMLRootElementContentDescriber.class.getName()); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, IPlatform.PI_RUNTIME, 0, message, null));
		}
	}
}