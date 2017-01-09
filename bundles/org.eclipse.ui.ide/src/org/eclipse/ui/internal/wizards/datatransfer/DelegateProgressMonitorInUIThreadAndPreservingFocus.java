/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc., and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * A progress monitor that delegates report to another one wrapping invocations
 * of delegate methods in Dislay.asyncExec() and ensuring focus is preserved
 * during the beginTask operation.
 *
 * @since 3.12
 */
class DelegateProgressMonitorInUIThreadAndPreservingFocus implements IProgressMonitorWithBlocking {
	private ProgressMonitorPart delegate;
	private Display display;

	/**
	 * @param delegate
	 */
	public DelegateProgressMonitorInUIThreadAndPreservingFocus(ProgressMonitorPart delegate) {
		this.delegate = delegate;
		this.display = delegate.getDisplay();
	}

	private void inUIThread(Runnable r) {
		if (display == Display.getCurrent()) {
			if (!delegate.isDisposed()) {
				r.run();
			}
		} else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
				if (!delegate.isDisposed()) {
					r.run();
				}
			});
		}
	}

	@Override
	public void worked(int work) {
		inUIThread(() -> delegate.worked(work));
	}

	@Override
	public void subTask(String name) {
		inUIThread(() -> delegate.subTask(name));
	}

	@Override
	public void setTaskName(String name) {
		inUIThread(() -> delegate.setTaskName(name));
	}

	@Override
	public void setCanceled(boolean value) {
		inUIThread(() -> delegate.setCanceled(value));
	}

	@Override
	public boolean isCanceled() {
		return delegate.isCanceled();
	}

	@Override
	public void internalWorked(double work) {
		inUIThread(() -> delegate.internalWorked(work));
	}

	@Override
	public void done() {
		inUIThread(() -> delegate.done());
	}

	@Override
	public void beginTask(String name, int totalWork) {
		inUIThread(() -> {
			Point initialSelection = null;
			Control focusControl = Display.getCurrent().getFocusControl();
			if (focusControl != null && focusControl instanceof Combo) {
				initialSelection = ((Combo) focusControl).getSelection();
			}
			delegate.beginTask(name, totalWork);
			// this is necessary because ProgressMonitorPart
			// sets focus on Stop button
			if (focusControl != null) {
				focusControl.setFocus();
				if (focusControl instanceof Combo && initialSelection != null) {
					((Combo) focusControl).setSelection(initialSelection);
				}
			}
		});
	}

	@Override
	public void setBlocked(IStatus reason) {
		inUIThread(() -> delegate.setBlocked(reason));
	}

	@Override
	public void clearBlocked() {
		inUIThread(() -> delegate.clearBlocked());
	}
}