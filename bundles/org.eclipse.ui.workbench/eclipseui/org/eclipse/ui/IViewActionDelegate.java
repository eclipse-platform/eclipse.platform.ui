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
package org.eclipse.ui;

/**
 * Interface for an action that is contributed into a view's local tool bar,
 * pulldown menu, or popup menu. It extends <code>IActionDelegate</code> and
 * adds an initialization method for connecting the delegate to the view it
 * should work with.
 */
public interface IViewActionDelegate extends IActionDelegate {
	/**
	 * Initializes this action delegate with the view it will work in.
	 *
	 * @param view the view that provides the context for this delegate
	 */
	void init(IViewPart view);
}
