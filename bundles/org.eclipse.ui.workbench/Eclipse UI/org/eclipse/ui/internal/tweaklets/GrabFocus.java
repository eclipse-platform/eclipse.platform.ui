/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.tweaklets;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;

/**
 * The tweaklet provider can prevent the workbench page from grabbing focus.
 * 
 * @since 3.3
 */
public abstract class GrabFocus {
	static {
		Tweaklets.setDefault(GrabFocus.class, new AllowGrabFocus());
	}

	public abstract boolean grabFocusAllowed(IWorkbenchPart part);

	public abstract void init(Display display);

	public abstract void dispose();
}
