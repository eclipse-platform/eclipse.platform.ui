/*******************************************************************************
 * Copyright (c) 2003, 2020 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474273
 *******************************************************************************/
package org.eclipse.ui.internal.about;

import java.io.File;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.text.StringMatcher;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ConfigurationInfo;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Displays system information about the eclipse application. The content of
 * what is displayed is selectable through the
 * <code>org.eclipse.ui.systemSummaryExtensions</code> extension point.
 */
public final class AboutSystemPage extends ProductInfoPage {

	// This id is used when the system page is opened in its own dialog
	private static final String ID = "productInfo.system"; //$NON-NLS-1$

	private static final int BROWSE_ERROR_LOG_BUTTON = IDialogConstants.CLIENT_ID;

	private static final int COPY_TO_CLIPBOARD_BUTTON = IDialogConstants.CLIENT_ID + 1;

	private Text text;
	private String info = ""; //$NON-NLS-1$

	@Override
	public void createControl(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IWorkbenchHelpContextIds.SYSTEM_SUMMARY_DIALOG);

		Composite outer = createOuterComposite(parent);

		final Text filterText = new Text(outer, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
		filterText.setLayoutData(GridDataFactory.fillDefaults().create());
		filterText.setFont(JFaceResources.getDialogFont());
		filterText.setMessage(WorkbenchMessages.AboutPluginsDialog_filterTextMessage);
		filterText.setFocus();

		text = new Text(outer, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.NO_FOCUS | SWT.H_SCROLL);
		text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);

		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = convertVerticalDLUsToPixels(300);
		gridData.widthHint = convertHorizontalDLUsToPixels(400);
		text.setLayoutData(gridData);
		text.setFont(JFaceResources.getTextFont());
		fetchConfigurationInfo(text);
		filterText.addModifyListener(e -> {
			String filter = filterText.getText();
			if (filter.isEmpty()) {
				text.setText(info);
			} else {
				StringMatcher matcher = new StringMatcher('*' + filter + '*', true, false);

				StringBuilder filteredInfo = new StringBuilder();
				boolean previousLineEmpty = false;
				String delim = System.lineSeparator();
				String[] infoLines = info.split(delim);
				for (String line : infoLines) {
					boolean lineEmpty = line.isEmpty();
					if (lineEmpty && previousLineEmpty) {
						continue;
					}
					if (lineEmpty || line.startsWith("***") || matcher.match(line)) { //$NON-NLS-1$
						previousLineEmpty = lineEmpty;
						filteredInfo.append(line).append(delim);
					}
				}
				text.setText(filteredInfo.toString());
			}
		});

		setControl(outer);
	}

	@Override
	public void createPageButtons(Composite parent) {
		Button button = createButton(parent, BROWSE_ERROR_LOG_BUTTON,
				WorkbenchMessages.AboutSystemDialog_browseErrorLogName);
		String filename = Platform.getLogFileLocation().toOSString();
		button.setEnabled(new File(filename).exists());

		createButton(parent, COPY_TO_CLIPBOARD_BUTTON, WorkbenchMessages.AboutSystemDialog_copyToClipboardName);
	}

	@Override
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
			if (contents.isEmpty())
				contents = text.getText();
			clipboard.setContents(new Object[] { contents }, new Transfer[] { TextTransfer.getInstance() });
		} finally {
			if (clipboard != null) {
				clipboard.dispose();
			}
		}
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case BROWSE_ERROR_LOG_BUTTON:
			openErrorLogBrowser();
			break;
		case COPY_TO_CLIPBOARD_BUTTON:
			runCopyToClipboard();
			break;
		}
		super.buttonPressed(buttonId);
	}

	private void openErrorLogBrowser() {
		AboutUtils.openErrorLogBrowser(getShell());
	}

	private void runCopyToClipboard() {
		if (text == null) {
			return;
		}

		Clipboard clipboard = null;
		try {
			clipboard = new Clipboard(getShell().getDisplay());
			clipboard.setContents(new Object[] { text.getText() }, new Transfer[] { TextTransfer.getInstance() });
		} finally {
			if (clipboard != null) {
				clipboard.dispose();
			}
		}
	}

	private void fetchConfigurationInfo(final Text text) {
		text.setText(WorkbenchMessages.AboutSystemPage_RetrievingSystemInfo);
		Job job = Job.create(WorkbenchMessages.AboutSystemPage_FetchJobTitle, (IJobFunction) monitor -> {
			info = ConfigurationInfo.getSystemSummary();
			if (!text.isDisposed()) {
				text.getDisplay().asyncExec(() -> {
					if (!text.isDisposed()) {
						text.setText(info);
					}
				});
			}
			return Status.OK_STATUS;
		});
		job.schedule();
	}
}
