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
package org.eclipse.ant.core;


import org.eclipse.ant.internal.core.AntObject;

/**
 * Represents an Ant task.
 * @since 2.1
 */
public class Task extends AntObject {

	/**
	 * Returns the name of the task
	 * @return the name of the task
	 */
	public String getTaskName() {
		return fName;
	}

	/**
	 * Sets the name of the task
	 * @param taskName The taskName to set
	 */
	public void setTaskName(String taskName) {
		fName= taskName;
	}
}
