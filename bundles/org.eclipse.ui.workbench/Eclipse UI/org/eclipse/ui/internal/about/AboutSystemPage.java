/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.about;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ConfigurationInfo;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;

/**
 * Displays system information about the eclipse application. The content of
 * what is displayed is selectable through the
 * <code>org.eclipse.ui.systemSummaryExtensions</code> extension point.
 */
public final class AboutSystemPage extends ProductInfoPage {

	class CopyHandler extends AbstractHandler {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands
		 * .ExecutionEvent)
		 */
		public Object execute(ExecutionEvent event) throws ExecutionException {
			if (text == null) {
				return null;
			}

			Clipboard clipboard = null;
			try {
				clipboard = new Clipboard(getShell().getDisplay());
				String contents = text.getSelectionText();
				if (contents.length() == 0)
					contents = text.getText();
				clipboard.setContents(new Object[] { contents },
						new Transfer[] { TextTransfer.getInstance() });
			} finally {
				if (clipboard != null) {
					clipboard.dispose();
				}
			}
			return null;
		}

		public boolean isEnabled() {
			return true;
		}
	}

	// This id should *not* be the same id used for contributing the page in
	// the installationPage extension. It is used by ProductInfoDialog
	// to ensure a different namespace for button contributions than the id
	// for the page appearing in the InstallationDialog
	private static final String ID = "productInfo.system"; //$NON-NLS-1$

	private Text text;

	protected Control createPageControl(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IWorkbenchHelpContextIds.SYSTEM_SUMMARY_DIALOG);

		Composite outer = createOuterComposite(parent);

		text = new Text(outer, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY
				| SWT.V_SCROLL | SWT.NO_FOCUS | SWT.H_SCROLL);
		text.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL);
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = convertVerticalDLUsToPixels(300);
		gridData.widthHint = convertHorizontalDLUsToPixels(400);
		text.setLayoutData(gridData);
		text.setText(ConfigurationInfo.getSystemSummary());
		text.setFont(JFaceResources.getTextFont());
		return outer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.about.ProductInfoPage#getId()
	 */
	String getId() {
		return ID;
	}

	public void copyToClipboard() {
		if (text == null) {
			return;
		}

		Clipboard clipboard = null;
		try {
			clipboard = new Clipboard(text.getShell().getDisplay());
			String contents = text.getSelectionText();
			if (contents.length() == 0)
				contents = text.getText();
			clipboard.setContents(new Object[] { contents },
					new Transfer[] { TextTransfer.getInstance() });
		} finally {
			if (clipboard != null) {
				clipboard.dispose();
			}
		}
	}
}
