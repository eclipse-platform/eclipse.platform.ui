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
package org.eclipse.core.runtime.content;

import java.io.*;
import java.util.Hashtable;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.internal.content.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A content describer for detecting the name of the top-level element or the
 * DTD system identifier in an XML file.
 * <p>
 * This executable extension supports two parameters:
 * "dtd" and "element". 
 * At least one of them <strong>must</strong> be provided.  If the
 * <code>":-"</code> method is used, then the value is treated as
 * "element".
 * </p>  
 * <p>
 * This class is not intended to be subclassed or instantiated by clients, 
 * only to be referenced in the "describer" configuration element of
 * extensions to the <code>org.eclipse.core.runtime.contentTypes</code>
 * extension-pont.
 * </p>
 * 
 * @since 3.0
 */
public final class XMLRootElementContentDescriber extends XMLContentDescriber implements IExecutableExtension {
	private static final String DTD_TO_FIND = "dtd"; //$NON-NLS-1$
	private static final String ELEMENT_TO_FIND = "element"; //$NON-NLS-1$
	/* (Intentionally not included in javadoc)
	 * The system identifier that we wish to find. This value will be
	 * initialized by the <code>setInitializationData</code> method. If no
	 * value is provided, then this means that we don't care what the system
	 * identifier will be.
	 */
	private String dtdToFind = null;
	/* (Intentionally not included in javadoc)
	 * The top-level element we are looking for. This value will be initialized
	 * by the <code>setInitializationData</code> method. If no value is
	 * provided, then this means that we don't care what the top-level element
	 * will be.
	 */
	private String elementToFind = null;
	private String id;

	/* (Intentionally not included in javadoc)
	 * Determines the validation status for the given contents.
	 * 
	 * @param contents the contents to be evaluated
	 * @return one of the following:<ul>
	 * <li><code>VALID</code></li>,
	 * <li><code>INVALID</code></li>,
	 * <li><code>INDETERMINATE</code></li>
	 * </ul>
	 * @throws IOException
	 */
	private int checkCriteria(InputSource contents) throws IOException {
		XMLRootHandler xmlHandler = new XMLRootHandler(elementToFind != null);
		try {
			if (!xmlHandler.parseContents(contents))
				return INVALID;
		} catch (CharConversionException cce) {
			// in some cases (see bug 62443) a CharConversionException may occur
			// we don't want to allow higher-level IOExceptions such as these ones 
			// to cause the current content type detection loop to fail
			if (ContentTypeManager.DEBUGGING) {
				String message = Policy.bind("content.errorReadingContents", id); //$NON-NLS-1$ 
				ContentType.log(message, cce);
			}
			return INVALID;
		} catch (SAXException e) {
			// we may be handed any kind of contents... it is normal we fail to parse
			return INVALID;
		} catch (ParserConfigurationException e) {
			// some bad thing happened - force this describer to be disabled
			String message = Policy.bind("content.parserConfiguration"); //$NON-NLS-1$
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, 0, message, e));
			throw new RuntimeException(message);
		}
		// Check to see if we matched our criteria.
		if ((elementToFind != null) && (!elementToFind.equals(xmlHandler.getRootName())))
			return INVALID;
		if ((dtdToFind != null) && (!dtdToFind.equals(xmlHandler.getDTD())))
			return INVALID;
		// We must be okay then.		
		return VALID;
	}

	/* (Intentionally not included in javadoc)
	 * @see IContentDescriber#describe(InputStream, IContentDescription)
	 */
	public int describe(InputStream contents, IContentDescription description) throws IOException {
		// call the basic XML describer to do basic recognition
		if (super.describe(contents, description) == INVALID)
			return INVALID;
		// super.describe will have consumed some chars, need to rewind		
		contents.reset();
		// Check to see if we matched our criteria.		
		return checkCriteria(new InputSource(contents));
	}

	/* (Intentionally not included in javadoc)
	 * @see IContentDescriber#describe(Reader, IContentDescription)
	 */
	public int describe(Reader contents, IContentDescription description) throws IOException {
		// call the basic XML describer to do basic recognition
		if (super.describe(contents, description) == INVALID)
			return INVALID;
		// super.describe will have consumed some chars, need to rewind
		contents.reset();
		// Check to see if we matched our criteria.
		return checkCriteria(new InputSource(contents));
	}

	/* (Intentionally not included in javadoc)
	 * @see IExecutableExtension#setInitializationData
	 */
	public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data) throws CoreException {
		this.id = config.getDeclaringExtension().getNamespace() + '.' + config.getAttributeAsIs("id"); //$NON-NLS-1$
		if (data instanceof String)
			elementToFind = (String) data;
		else if (data instanceof Hashtable) {
			Hashtable parameters = (Hashtable) data;
			dtdToFind = (String) parameters.get(DTD_TO_FIND);
			elementToFind = (String) parameters.get(ELEMENT_TO_FIND);
		}
		if (dtdToFind == null && elementToFind == null) {
			String message = Policy.bind("content.badInitializationData", XMLRootElementContentDescriber.class.getName()); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, Platform.PI_RUNTIME, 0, message, null));
		}
	}
}