/*******************************************************************************
 * Copyright (c) 2000, 2003 Keith Seitz and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Keith Seitz (keiths@redhat.com) - initial implementation
 *     IBM Corporation - integration and code cleanup
 *******************************************************************************/
package org.eclipse.debug.ui;

/**
 * A key/value set whose data is passed into Runtime.exec(...)
 */
public class EnvironmentVariable
{
	// The name of the environment variable
	private String name;
	
	// The value of the environment variable
	private String value;
	
	public EnvironmentVariable(String name, String value)
	{
		this.name = name;
		this.value = value;
	}

	/**
	 * Returns this variable's name, which serves as the key in the key/value
	 * pair this variable represents
	 * 
	 * @return this variable's name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Returns this variables value.
	 * 
	 * @return this variable's value
	 */
	public String getValue()
	{
		return value;
	}
	
	/**
	 * Sets this variable's name (key)
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Sets this variable's value
	 * @param value
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
}
