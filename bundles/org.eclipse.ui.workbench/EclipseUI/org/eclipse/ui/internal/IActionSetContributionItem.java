/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

/**
 * This interface should be implemented by all contribution items defined by an
 * action set.
 */
public interface IActionSetContributionItem {

	/**
	 * Returns the action set id.
	 */
	String getActionSetId();

	/**
	 * Sets the action set id.
	 */
	void setActionSetId(String newActionSetId);
}
