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
 * <p>
 * <b>Note</b>: This interface is part of early access API that may well 
 * change in incompatible ways until it reaches its finished form. 
 * </p>
 * 
 * @see IContentDescriber  
 * @since 3.0 
 */
public interface IContentDescription {
	/**
	 * Flag indicating that the charset for the contents should be described. 
	 */
	public final static int CHARSET = 0x01;
	/**
	 * Flag indicating that the bye order mark for the contents should be described. 
	 */	
	public final static int BYTE_ORDER_MARK = 0x02;	
	/**
	 * Flag indicating that custom properties for the contents should be described. 
	 */	
	public final static int CUSTOM_PROPERTIES = 0x7f;
	/**
	 * Flag indicating that all available information should be described.
	 */
	public final static int ALL = 0xffff;		
	
	/**
	 * Returns the byte order mark (BOM) for the contents. Returns 0 (zero) if a 
	 * BOM was not found or was not determined.
	 * 
	 * @return the byte order mark, or zero
	 */
	public int getMark();
	/**
	 * Returns the charset for the contents. Returns <code>null</code> if the 
	 * charset could not be/was not determined.
	 *  
	 * @return a charset, or <code>null</code>
	 */
	public String getCharset();
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
	 * Sets the charset for this description.
	 * 
	 * @param charset the new charset, or <code>null</code>
	 */
	public void setCharset(String charset);
	/**
	 * Sets the Byte Order Mark for this description.
	 *  
	 * @param mark the new BOM, or zero 
	 */
	public void setMark(int mark);
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
	 */
	public void setProperty(QualifiedName key, Object value);

}
