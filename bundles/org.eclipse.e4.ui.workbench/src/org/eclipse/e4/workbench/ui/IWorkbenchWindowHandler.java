/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.workbench.ui;

/**
 *
 */
public interface IWorkbenchWindowHandler {

	/**
	 * @param appWindow
	 */
	void dispose(Object appWindow);

	/**
	 * @param appWindow
	 */
	void open(Object appWindow);

	/**
	 * @param appWindow
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	void setBounds(Object appWindow, int x, int y, int width, int height);

	/**
	 * @param appWindow
	 */
	void layout(Object appWindow);

	/**
	 * @param appWindow
	 */
	void runEvenLoop(Object appWindow);

	/**
	 * @param widget
	 */
	void close(Object widget);

}
