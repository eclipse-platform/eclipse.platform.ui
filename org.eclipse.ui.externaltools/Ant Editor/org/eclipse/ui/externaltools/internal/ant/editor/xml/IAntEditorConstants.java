/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.ant.editor.xml;

/**
 * Constants used by the Ant editor
 */
public interface IAntEditorConstants {
	
	/**
	 * Attributes stored in XmlElements
	 */
	public static final String ATTR_NAME= "name"; //$NON-NLS-1$
	public static final String ATTR_DEFAULT= "default"; //$NON-NLS-1$
	public static final String ATTR_RESOURCE= "resource"; //$NON-NLS-1$
	public static final String ATTR_ENVIRONMENT= "environment"; //$NON-NLS-1$
	public static final String ATTR_TARGET= "target"; //$NON-NLS-1$
	public static final String ATTR_DIR= "dir"; //$NON-NLS-1$
	public static final String ATTR_FILE= "file"; //$NON-NLS-1$
	public static final String ATTR_DESTFILE= "destfile"; //$NON-NLS-1$
	public static final String ATTR_SRC= "src"; //$NON-NLS-1$
	public static final String ATTR_ZIPFILE= "zipfile"; //$NON-NLS-1$
	public static final String ATTR_COMMAND= "command"; //$NON-NLS-1$
	public static final String ATTR_EXECUTABLE= "executable"; //$NON-NLS-1$
	public static final String ATTR_TYPE= "type"; //$NON-NLS-1$
	public static final String ATTR_DESCRIPTION= "description"; //$NON-NLS-1$
	/**
	 * Values of the type attribute stored in XmlElements
	 */
	public static final String TYPE_EXTERNAL = "external"; //$NON-NLS-1$
	public static final String TYPE_TARGET = "target"; //$NON-NLS-1$
	public static final String TYPE_PROJECT = "project"; //$NON-NLS-1$
	public static final String TYPE_PROPERTY = "property"; //$NON-NLS-1$
	public static final String TYPE_ANTCALL = "antcall"; //$NON-NLS-1$
	public static final String TYPE_MKDIR = "mkdir"; //$NON-NLS-1$
	public static final String TYPE_COPY = "copy"; //$NON-NLS-1$
	public static final String TYPE_ARCHIVE = "archive"; //$NON-NLS-1$
	public static final String TYPE_DECOMPRESS = "decompress"; //$NON-NLS-1$
	public static final String TYPE_COMPRESS = "compress"; //$NON-NLS-1$
	public static final String TYPE_EXEC = "exec"; //$NON-NLS-1$
	public static final String TYPE_DELETE = "delete"; //$NON-NLS-1$
	public static final String TYPE_UNKNOWN = "unknown"; //$NON-NLS-1$

}
