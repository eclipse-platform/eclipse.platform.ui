/*******************************************************************************
 * Copyright (c) 20020 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.swt;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

/**
 * @since 0.16
 */
public final class DisplayUISynchronize extends UISynchronize {

	private Display display;

	public DisplayUISynchronize(Display display) {
		this.display = display;
	}

	@Override
	public void syncExec(Runnable runnable) {
		if (display != null && !display.isDisposed()) {
			display.syncExec(runnable);
		}
	}

	@Override
	public void asyncExec(Runnable runnable) {
		if (display != null && !display.isDisposed()) {
			display.asyncExec(runnable);
		}
	}

	@Override
	public boolean isUIThread(Thread thread) {
		return Display.findDisplay(thread) != null;
	}

	@Override
	protected void showBusyWhile(Runnable runnable) {
		BusyIndicator.showWhile(display, runnable);
	}

	@Override
	protected boolean dispatchEvents() {
		if (display != null && !display.isDisposed()) {
			return display.readAndDispatch();
		}
		return false;
	}
}
