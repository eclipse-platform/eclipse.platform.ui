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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.help.internal.search.ASCIIReader;

/**
 * A content describer for XHTML.
 */
public class XHTMLContentDescriber implements IContentDescriber {

	private static final String XHTML_DTD_PREFIX = "http://www.w3.org/TR/xhtml"; //$NON-NLS-1$

	public static final int BUFFER_SIZE = 4096;

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.content.IContentDescriber#describe(java.io.InputStream, org.eclipse.core.runtime.content.IContentDescription)
	 */
	public int describe(InputStream contents, IContentDescription description) throws IOException {
		Reader reader = null;
		try {
			reader = new ASCIIReader(contents, BUFFER_SIZE);
			char[] chars = new char[BUFFER_SIZE];
			reader.read(chars);
			String str = new String(chars);
			return (str.indexOf(XHTML_DTD_PREFIX) != -1) ? VALID : INVALID;
		}
		catch (Exception e) {
			return INDETERMINATE;
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException e) {
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.content.IContentDescriber#getSupportedOptions()
	 */
	public QualifiedName[] getSupportedOptions() {
		return new QualifiedName[0];
	}
}
