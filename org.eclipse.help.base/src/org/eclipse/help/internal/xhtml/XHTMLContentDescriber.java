/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.xhtml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.XMLRootElementContentDescriber;

/**
 * A content describer for XHTML.
 */
public class XHTMLContentDescriber implements IContentDescriber {

	private static final String PROPERTY_DTD = "dtd"; //$NON-NLS-1$
	public static final int BUFFER_SIZE = 8192;

	// XHTML has 3 DTDs, so we have to try each one.
	private static final String DTD_STRICT = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"; //$NON-NLS-1$
	private static final String DTD_TRANSITIONAL = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"; //$NON-NLS-1$
	private static final String DTD_FRAMESET = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"; //$NON-NLS-1$

	private XMLRootElementContentDescriber describerStrict = new XMLRootElementContentDescriber();
	private XMLRootElementContentDescriber describerTransitional = new XMLRootElementContentDescriber();
	private XMLRootElementContentDescriber describerFrameset = new XMLRootElementContentDescriber();

	/**
	 * Constructs a new XHTMLContentDescriber. Initializes the three
	 * delegates with their respective DTDs.
	 */
	public XHTMLContentDescriber() {
		try {
			describerStrict.setInitializationData(null, null, getParameter(PROPERTY_DTD, DTD_STRICT));
		}
		catch (CoreException e) {
			// not much we can do here
		}

		try {
			describerTransitional.setInitializationData(null, null, getParameter(PROPERTY_DTD, DTD_TRANSITIONAL));
		}
		catch (CoreException e) {
			// not much we can do here
		}

		try {
			describerFrameset.setInitializationData(null, null, getParameter(PROPERTY_DTD, DTD_FRAMESET));
		}
		catch (CoreException e) {
			// not much we can do here
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.content.IContentDescriber#describe(java.io.InputStream, org.eclipse.core.runtime.content.IContentDescription)
	 */
	public int describe(InputStream contents, IContentDescription description) throws IOException {
		/*
		 * Load the first BUFFER_SIZE bytes, then pass that to each delegate.
		 * If any one recognizes their DTD, return VALID.
		 */
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			contents.read(buffer);
		
			ByteArrayInputStream in = new ByteArrayInputStream(buffer);
			if (describerTransitional.describe(in, description) == VALID) {
				return VALID;
			}
			in = new ByteArrayInputStream(buffer);
			if (describerStrict.describe(in, description) == VALID) {
				return VALID;
			}
			in = new ByteArrayInputStream(buffer);
			return describerFrameset.describe(in, description);
		}
		catch (Exception e) {
			return INDETERMINATE;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.content.IContentDescriber#getSupportedOptions()
	 */
	public QualifiedName[] getSupportedOptions() {
		return new QualifiedName[0];
	}
	
	/**
	 * Creates a new parameter suitable for passing into the
	 * XMLRootElementContentDescriber. The parameters have to be
	 * in the form of a Hashtable.
	 * 
	 * @param name parameter name
	 * @param value parameter value
	 * @return the parameter, in Hashtable form
	 */
	private static Hashtable getParameter(String name, String value) {
		Hashtable hash = new Hashtable();
		hash.put(name, value);
		return hash;
	}
}
