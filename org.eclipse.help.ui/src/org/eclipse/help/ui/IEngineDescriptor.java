/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui;

import java.util.Dictionary;

/**
 * 
 */

public interface IEngineDescriptor {
	/**
	 * 
	 * @return
	 */
	String getId();

	/**
	 * 
	 * @return
	 */
	String getEngineTypeId();

	/**
	 * 
	 * @return
	 */
	String getLabel();

	/**
	 * 
	 * @param label
	 */
	void setLabel(String label);

	/**
	 * 
	 * @return
	 */
	String getDescription();

	/**
	 * 
	 * @param desc
	 */
	void setDescription(String desc);

	/**
	 * 
	 * @return
	 */
	Dictionary getParameters();
	/**
	 * @return 
	 */
	boolean isUserDefined();
}
