/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.dialogs;

/**
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.2
 */
public abstract class AbstractSearchItem {

	private boolean duplicate = false;

	private boolean isHistory = false;

	/**
	 * Check if it is duplicate
	 * 
	 * @return true if it's duplicate, else false
	 */
	public boolean isDuplicate() {
		return this.duplicate;
	}

	/**
	 * 
	 * Mark it as a duplicate
	 */
	public void setDuplicate() {
		this.duplicate = true;
	}

	/**
	 * Check if it is duplicate
	 * 
	 * @return true if it's duplicate, else false
	 */
	public boolean isHistory() {
		return this.isHistory;
	}

	/**
	 * 
	 * Mark it as a duplicate
	 */
	public void markAsHistory() {
		this.isHistory = true;
	}

}
