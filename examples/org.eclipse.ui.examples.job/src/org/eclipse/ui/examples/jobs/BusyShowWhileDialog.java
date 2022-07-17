/*******************************************************************************
 * Copyright (c) 2014, 2018 IBM Corporation and others.
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

package org.eclipse.ui.examples.jobs;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * BusyShowWhileDialog is a test of busyShowWhile in a modal dialog.
 */
public class BusyShowWhileDialog extends IconAndMessageDialog {
	/**
	 * @param parentShell
	 * @todo Generated comment
	 */
	public BusyShowWhileDialog(Shell parentShell) {
		super(parentShell);
		message = "Busy While Test"; //$NON-NLS-1$
	}

	@Override
	protected Image getImage() {
		return null;
	}

	@Override
	@SuppressWarnings("restriction")
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		Button detailsButton = createButton(parent, 4, "Start busy show while", false); //$NON-NLS-1$
		detailsButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			try {
				org.eclipse.ui.internal.progress.ProgressManager.getInstance().busyCursorWhile(monitor -> {
					long time = System.currentTimeMillis();
					long delay = PlatformUI.getWorkbench().getProgressService().getLongOperationTime();
					long end = time + delay + delay;
					while (end > System.currentTimeMillis()) {
						final Shell myShell = BusyShowWhileDialog.this.getShell();
						myShell.getDisplay().asyncExec(() -> {
							if (myShell.isDisposed())
								return;
							myShell.getDisplay().sleep();
							myShell.setText(String.valueOf(System.currentTimeMillis()));
						});
					}
				});
			} catch (InvocationTargetException error) {
				error.printStackTrace();
			} catch (InterruptedException error) {
				// ignore - in this context it means cancellation
			}
		}));
	}
}