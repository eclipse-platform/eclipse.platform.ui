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

package org.eclipse.ui.tests.manual;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * @since 3.3
 * 
 */
public class ExplicitlyBusyView extends ViewPart {

	private IWorkbenchSiteProgressService progressService;
	private Object family = new Object();
	private int counter;

	class SomeJob extends Job {
		public SomeJob(String name) {
			super(name);
		}

		protected IStatus run(IProgressMonitor monitor) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return Status.OK_STATUS;
		}

		public boolean belongsTo(Object family) {
			return family == ExplicitlyBusyView.this.family;
		}
	}

	public void createPartControl(Composite parent) {
		progressService = (IWorkbenchSiteProgressService) getSite().getAdapter(
				IWorkbenchSiteProgressService.class);
		progressService.showBusyForFamily(family);
		{
			final Button button = new Button(parent, SWT.CHECK);
			button.setText("Busy");
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (button.getSelection()) {
						progressService.incrementBusy();
					} else {
						progressService.decrementBusy();
					}
				}
			});
		}
		{
			Button button = new Button(parent, SWT.PUSH);
			button.setText("Increment Busy");
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					progressService.incrementBusy();
				}
			});
		}
		{
			Button button = new Button(parent, SWT.PUSH);
			button.setText("Decrement Busy");
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					progressService.decrementBusy();
				}
			});
		}
		{
			Button button = new Button(parent, SWT.PUSH);
			button.setText("Spawn Job");
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					new SomeJob("Some Job " + counter++).schedule();
				}
			});
		}
		GridLayoutFactory.swtDefaults().applyTo(parent);
	}

	public void setFocus() {
	}

}
