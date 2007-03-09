/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

/**
 * Represents a link with text that invokes a specific command with
 * parameters.
 * 
 * @since 3.3
 */
public interface ICommandLink extends IUAElement {

	/**
	 * Returns the label text to display for this link.
	 * 
	 * @return the link text
	 */
	public String getLabel();
	
	/**
	 * Returns the serialized form of the command to invoke, along with
	 * its parameters.
	 * 
	 * @return the serialized command
	 */
	public String getSerialization();
}
