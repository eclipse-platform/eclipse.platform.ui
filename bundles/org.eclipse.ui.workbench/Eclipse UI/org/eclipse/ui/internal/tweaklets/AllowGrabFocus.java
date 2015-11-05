/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 * @since 3.3
 *
 */
public class AllowGrabFocus extends GrabFocus {

	@Override
	public boolean grabFocusAllowed(IWorkbenchPart part) {
		return true;
	}

	@Override
	public void init(Display display) {
	}

	@Override
	public void dispose() {
	}
}
