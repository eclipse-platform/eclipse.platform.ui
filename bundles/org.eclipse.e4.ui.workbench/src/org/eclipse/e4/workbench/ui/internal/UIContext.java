/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.e4.core.services.Context;
import org.eclipse.swt.widgets.Display;

public class UIContext extends Context {

	/**
	 * @param parent
	 * @param name
	 */
	public UIContext(Context parent, String name) {
		super(parent, name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.e4.core.services.Context#schedule(java.lang.Runnable)
	 */
	@Override
	protected void schedule(Runnable runnable) {
		Display.getDefault().asyncExec(runnable);
	}

}
