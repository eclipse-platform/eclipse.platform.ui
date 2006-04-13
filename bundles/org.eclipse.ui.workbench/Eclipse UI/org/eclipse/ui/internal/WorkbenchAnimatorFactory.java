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

package org.eclipse.ui.internal;

import org.eclipse.jface.dialogs.AnimatorFactory;
import org.eclipse.jface.dialogs.ControlAnimator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * Factory for workbench control animators to be used by JFace in animating
 * the display of an SWT Control.
 * 
 * @since 3.2
 * 
 */
public class WorkbenchAnimatorFactory extends AnimatorFactory {
	
	/**
	 * Creates a new WorkbenchControlAnimator for use by JFace in animating
	 * the display of an SWT Control.
	 * 
	 * @param control the SWT Control to de animated
	 * @return animator the WorkbenchControlAnimator
	 */
	public ControlAnimator createAnimator(Control control) {
		if(PrefUtil.getAPIPreferenceStore()
				.getBoolean(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS) 
				&& PlatformUI.isWorkbenchRunning())
			return new WorkbenchControlAnimator(control);
		return new ControlAnimator(control);
	}
}
