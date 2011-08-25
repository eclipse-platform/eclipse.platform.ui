/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts.debug.ui;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class ContextAllocation {
	static private final String EMPTY = ""; //$NON-NLS-1$
	static private final String NEW_LINE = System.getProperty("line.separator"); //$NON-NLS-1$

	final private TabFolder folder;

	private TabItem tabData;
	private Text text;
	private IEclipseContext selectedContext;

	public ContextAllocation(TabFolder folder) {
		this.folder = folder;
	}

	public void createControls() {
		tabData = new TabItem(folder, SWT.NONE, 1);
		tabData.setText(ContextMessages.allocationsTab);

		Composite pageData = new Composite(folder, SWT.NONE);
		tabData.setControl(pageData);

		new Label(pageData, SWT.NONE).setText(ContextMessages.allocationsLabel);
		text = new Text(pageData, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		text.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		GridLayout rightPaneLayout = new GridLayout();
		rightPaneLayout.marginHeight = 0;
		rightPaneLayout.marginWidth = 0;
		pageData.setLayout(rightPaneLayout);
	}

	public void setInput(IEclipseContext newContext) {
		if (newContext == selectedContext)
			return;
		selectedContext = newContext;
		if (selectedContext == null) {
			text.setText(EMPTY);
			return;
		}
		Throwable t = AllocationRecorder.getDefault().getTrace(selectedContext);
		String traceText;
		if (t == null)
			traceText = EMPTY;
		else {
			StackTraceElement[] elements = t.getStackTrace();
			StringBuffer tmp = new StringBuffer();
			for (int i = 1; i < elements.length; i++) { // skip 1st element - that's debug class
				StackTraceElement stackTraceElement = elements[i];
				tmp.append(stackTraceElement.toString());
				tmp.append(NEW_LINE);
			}
			traceText = tmp.toString();
		}
		text.setText(traceText);
	}

}
