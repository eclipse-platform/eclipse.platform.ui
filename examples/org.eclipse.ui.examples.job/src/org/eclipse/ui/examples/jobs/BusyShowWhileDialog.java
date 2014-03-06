/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.examples.jobs;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.progress.ProgressManager;
/**
 *BusyShowWhileDialog is a test of busyShowWhile in a modal dialog.
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
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		Button detailsButton = createButton(parent, 4, "Start busy show while", false); //$NON-NLS-1$
		detailsButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					ProgressManager.getInstance().busyCursorWhile(new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException,
								InterruptedException {
							long time = System.currentTimeMillis();
							long delay = PlatformUI.getWorkbench().getProgressService().getLongOperationTime();
							long end = time + delay + delay;
							while (end > System.currentTimeMillis()) {
								final Shell myShell = BusyShowWhileDialog.this.getShell();
								myShell.getDisplay().asyncExec(new Runnable() {
									@Override
									public void run() {
										if(myShell.isDisposed())
											return;
										myShell.getDisplay().sleep();
										myShell.setText(String.valueOf(System.currentTimeMillis()));
									}
								});
							}
						}
					});
				} catch (InvocationTargetException error) {
					error.printStackTrace();
				} catch (InterruptedException error) {
					//ignore - in this context it means cancellation
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				//do nothing
			}
		});
	}
}