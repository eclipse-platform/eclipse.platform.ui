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

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IEclipseContextScheduler;
import org.eclipse.e4.core.services.context.spi.IRunAndTrack;
import org.eclipse.swt.widgets.Display;

public class UIContextScheduler implements IEclipseContextScheduler {

	static final public IEclipseContextScheduler instance = new UIContextScheduler();

	public void schedule(Runnable runnable) {
		Display.getDefault().asyncExec(runnable);
	}

	public boolean schedule(final IEclipseContext context,
			final IRunAndTrack runnable, final String name,
			final int eventType, final Object[] args) {
		final boolean[] result = new boolean[1];
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				result[0] = runnable.notify(context, name, eventType, args);
			}
		});
		return result[0];
	}
}
