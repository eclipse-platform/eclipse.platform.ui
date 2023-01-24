/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
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
package org.eclipse.ui.tests.api.workbenchpart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.tests.TestPlugin;

public class HeavyResourceView extends ViewPart {

	private Button useAllComposites;
	private Button releaseAllComposites;

	private Shell tempShell;
	private Composite control;

	@Override
	public void createPartControl(Composite parent) {
		control = parent;

		SelectionListener listener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);

				if (e.widget == useAllComposites) {
					useAll();
				} else if (e.widget == releaseAllComposites) {
					releaseAll();
				}
			}
		};

		Label explanation = new Label(parent, SWT.WRAP);
		explanation.setText("""
				This view allocates all available SWT resources on demand.
				This is not supposed to be a recoverable error, and is expected to crash the workbench.
				This view allows us observe the workbench when it crashes in this manner.
				""");

		useAllComposites = new Button(parent, SWT.PUSH);
		useAllComposites.setText("&Allocate all available composites (very slow!)");
		useAllComposites.addSelectionListener(listener);

		releaseAllComposites = new Button(parent, SWT.PUSH);
		releaseAllComposites.setText("&Release all composites");
		releaseAllComposites.addSelectionListener(listener);

	}

	@Override
	public void setFocus() {
		control.setFocus();
	}

	public void useAll() {
		releaseAll();
		tempShell = new Shell(Display.getCurrent(), SWT.NONE);
		try {
			for(;;) {
				new Composite(tempShell, SWT.NONE);
			}
		} catch (SWTError e) {
			TestPlugin.getDefault().getLog().log(WorkbenchPlugin.getStatus(e));
		}
	}

	public void releaseAll() {
		if (tempShell != null) {
			tempShell.dispose();
			tempShell = null;
		}
	}

	@Override
	public void dispose() {
		releaseAll();
		super.dispose();
	}

}
