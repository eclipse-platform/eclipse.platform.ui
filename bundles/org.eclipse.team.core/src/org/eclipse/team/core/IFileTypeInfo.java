/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.core;

/**
 * A file type info specifies both the file extension and the 
 * corresponding file type.
 * 
 * @since 2.0
 */
public interface IFileTypeInfo {
	/**
	 * Returns the string specifying the file extension
	 * 
	 * @return the file extension
	 */
	public String getExtension();
	
	/**
	 * Returns the file type for files ending with the corresponding
	 * extension.
	 * 
	 * Valid values are:
	 * Team.UNKNOWN
	 * Team.TEXT
	 * Team.BINARY
	 * 
	 * @return the file type
	 */
	public int getType();
}
