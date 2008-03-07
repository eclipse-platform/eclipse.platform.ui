/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.tweaklets;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.tweaklets.Tweaklets.TweakKey;

/**
 * Tweak that enables experimental Eclipse 4.0 functionality
 * 
 * @since 3.4
 *
 */
public abstract class WorkbenchImplementation {
	public static TweakKey KEY = new Tweaklets.TweakKey(WorkbenchImplementation.class);

	static {
		Tweaklets.setDefault(WorkbenchImplementation.KEY, new Workbench3xImplementation());
	}

	/** Default c'tor */
	public WorkbenchImplementation() {}

	/**
	 * @param newWindowNumber
	 * @return
	 */
	public abstract WorkbenchWindow createWorkbenchWindow(int newWindowNumber);

	/**
	 * @param workbenchWindow
	 * @param perspID
	 * @param input
	 * @return
	 * @throws WorkbenchException 
	 */
	public abstract WorkbenchPage createWorkbenchPage(WorkbenchWindow workbenchWindow,
			String perspID, IAdaptable input) throws WorkbenchException;

	/**
	 * @param workbenchWindow
	 * @param finalInput
	 * @return
	 * @throws WorkbenchException 
	 */
	public abstract WorkbenchPage createWorkbenchPage(WorkbenchWindow workbenchWindow,
			IAdaptable finalInput) throws WorkbenchException;

	/**
	 * @param desc
	 * @param workbenchPage
	 * @return
	 * @throws WorkbenchException 
	 */
	public abstract Perspective createPerspective(PerspectiveDescriptor desc,
			WorkbenchPage workbenchPage) throws WorkbenchException;
}
