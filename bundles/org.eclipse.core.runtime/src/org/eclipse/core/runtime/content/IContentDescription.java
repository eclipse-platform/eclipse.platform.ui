/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.content;

import org.eclipse.core.runtime.IPlatform;
import org.eclipse.core.runtime.QualifiedName;

/**
 * A content description object contains information about the nature of an 
 * arbitrary set of bytes.
 * <p>
 * A content description object will always include the content type for the 
 * examined contents, and may also include information on:
 * <ol>
 * <li>charset;</li>
 * <li>byte order mark;</li>
 * <li>other custom properties provided by third-party plug-ins.</li>
 * </ol>
 * </p>
 * <p>
 * <cite>Content describers</cite> provided by plug-ins will fill most of the
 * properties in a content description object, except for the content type, 
 * which is defined by the platform. After a content 
 * description is filled by a content interpreter, it is marked as immutable
 * by the platform, so calling any of the mutator methods defined in this 
 * interface will cause an IllegalStateException to be thrown.  
 * </p>  
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see IContentDescriber  
 * @since 3.0 
 */
public interface IContentDescription {
	/**
	 * Key for the byte order mark property.
	 */
	public final static QualifiedName CHARSET = new QualifiedName(IPlatform.PI_RUNTIME, "charset");  //$NON-NLS-1$	

	/**
	 * Key for the byte order mark property.
	 */
	public final static QualifiedName BYTE_ORDER_MARK = new QualifiedName(IPlatform.PI_RUNTIME, "bom");  //$NON-NLS-1$
	
	/**
	 * Options constant meaning that all properties should be described. 
	 */
	public final static QualifiedName[] ALL = null;
	
	/**
	 * Returns whether the given property is requested to be described. This 
	 * method is intended to allow content describers to determine  which
	 * properties should be described.
	 *  
	 * @param propertyKey a key for the property to be verified 
	 * @return <code>true</code> if the property is to be described, 
	 * <code>false</code> otherwise
	 */
	public boolean isRequested(QualifiedName propertyKey);

	/**
	 * Returns the content type detected. Returns <code>null</code> if the 
	 * content type could not be determined.
	 *   
	 * @return the corresponding content type, or <code>null</code>
	 */
	public IContentType getContentType();

	/**
	 * Returns the value of custom property set by the content interpreter used.  
	 * <p>
	 * The qualifier part of the property name must be the unique identifier
	 * of the declaring plug-in (e.g. <code>"com.example.plugin"</code>).
	 * </p>
	 * 
	 * @param key the property key
	 * @return the property value, or <code>null</code>, if the property is not
	 * found   
	 */
	public Object getProperty(QualifiedName key);

	/**
	 * Sets the given property to the given value. 
	 * <p>
	 * The qualifier part of the property name must be the unique identifier
	 * of the declaring plug-in (e.g. <code>"com.example.plugin"</code>).
	 * </p>
	 * 
	 * @param key the qualified name of the property 
	 * @param value the property value, or <code>null</code>,
	 * if the property is to be removed
	 * @throws IllegalStateException if called after this description has been
	 * filled in
	 */
	public void setProperty(QualifiedName key, Object value);
}