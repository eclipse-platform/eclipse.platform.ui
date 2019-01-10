/*******************************************************************************
 * Copyright (c) 2003, 2017 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 424111
 *******************************************************************************/

package org.eclipse.ui.internal.views.log;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

/**
 * Displays the error log in non-Win32 platforms - see bug 55314.
 */
public final class OpenLogDialog extends TrayDialog {
	// input log file
	private File logFile;
	// location/size configuration
	private IDialogSettings dialogSettings;
	private Point dialogLocation;
	private Point dialogSize;
	private int DEFAULT_WIDTH = 750;
	private int DEFAULT_HEIGHT = 800;

	public OpenLogDialog(Shell parentShell, File logFile) {
		super(parentShell);
		this.logFile = logFile;
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.MODELESS);

	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.OpenLogDialog_title);
		readConfiguration();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
	}

	@Override
	public void create() {
		super.create();
		// dialog location
		if (dialogLocation != null)
			getShell().setLocation(dialogLocation);
		// dialog size
		if (dialogSize != null)
			getShell().setSize(dialogSize);
		else
			getShell().setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		getButton(IDialogConstants.CLOSE_ID).setFocus();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite outer = (Composite) super.createDialogArea(parent);
		Text text = new Text(outer, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.NO_FOCUS | SWT.H_SCROLL);
		text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		text.setLayoutData(gridData);
		text.setText(getLogSummary());
		return outer;
	}

	private String getLogSummary() {
		StringWriter out = new StringWriter();
		try (PrintWriter writer = new PrintWriter(out)) {
			if (logFile.length() > LogReader.MAX_FILE_LENGTH) {
				readLargeFileWithMonitor(writer);
			} else {
				readFileWithMonitor(writer);
			}
		}
		return out.toString();
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.CLOSE_ID) {
			storeSettings();
			close();
		}
		super.buttonPressed(buttonId);
	}

	//--------------- configuration handling --------------
	/**
	 * Stores the current state in the dialog settings.
	 *
	 * @since 2.0
	 */
	private void storeSettings() {
		writeConfiguration();
	}

	/**
	 * Returns the dialog settings object used to share state between several
	 * event detail dialogs.
	 *
	 * @return the dialog settings to be used
	 */
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		dialogSettings = settings.getSection(getClass().getName());
		if (dialogSettings == null)
			dialogSettings = settings.addNewSection(getClass().getName());
		return dialogSettings;
	}

	/**
	 * Initializes itself from the dialog settings with the same state as at the
	 * previous invocation.
	 */
	private void readConfiguration() {
		IDialogSettings s = getDialogSettings();
		try {
			int x = s.getInt("x"); //$NON-NLS-1$
			int y = s.getInt("y"); //$NON-NLS-1$
			dialogLocation = new Point(x, y);
			x = s.getInt("width"); //$NON-NLS-1$
			y = s.getInt("height"); //$NON-NLS-1$
			dialogSize = new Point(x, y);
		} catch (NumberFormatException e) {
			dialogLocation = null;
			dialogSize = null;
		}
	}

	private void writeConfiguration() {
		IDialogSettings s = getDialogSettings();
		Point location = getShell().getLocation();
		s.put("x", location.x); //$NON-NLS-1$
		s.put("y", location.y); //$NON-NLS-1$
		Point size = getShell().getSize();
		s.put("width", size.x); //$NON-NLS-1$
		s.put("height", size.y); //$NON-NLS-1$
	}

	// reading file within MAX_FILE_LENGTH size
	void readFile(PrintWriter writer) throws FileNotFoundException, IOException {
		try (BufferedReader bReader = new BufferedReader(new FileReader(logFile))) {

			while (bReader.ready()) {
				writer.println(bReader.readLine());
			}
		}
	}

	// reading large files
	void readLargeFile(PrintWriter writer) throws FileNotFoundException, IOException {
		boolean hasStarted = false;
		try (RandomAccessFile random = new RandomAccessFile(logFile, "r");) { //$NON-NLS-1$

			random.seek(logFile.length() - LogReader.MAX_FILE_LENGTH);
			for (;;) {
				String line = random.readLine();
				if (line == null)
					break;
				line = line.trim();
				if (line.length() == 0)
					continue;
				if (!hasStarted && (line.startsWith("!ENTRY") || line.startsWith(LogSession.SESSION))) //$NON-NLS-1$
					hasStarted = true;
				if (hasStarted)
					writer.println(line);
				continue;
			}
		}
	}

	private void readLargeFileWithMonitor(final PrintWriter writer) {
		IRunnableWithProgress runnable = monitor -> {
			monitor.beginTask(Messages.OpenLogDialog_message, IProgressMonitor.UNKNOWN);
			try {
				readLargeFile(writer);
			} catch (IOException e) {
				writer.println(Messages.OpenLogDialog_cannotDisplay);
			}
		};
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getParentShell());
		try {
			dialog.run(true, true, runnable);
		} catch (InvocationTargetException e) { // do nothing
		} catch (InterruptedException e) { // do nothing
		}
	}

	private void readFileWithMonitor(final PrintWriter writer) {
		IRunnableWithProgress runnable = monitor -> {
			monitor.beginTask(Messages.OpenLogDialog_message, IProgressMonitor.UNKNOWN);
			try {
				readFile(writer);
			} catch (IOException e) {
				writer.println(Messages.OpenLogDialog_cannotDisplay);
			}
		};
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getParentShell());
		try {
			dialog.run(true, true, runnable);
		} catch (InvocationTargetException e) { // do nothing
		} catch (InterruptedException e) { // do nothing
		}
	}
}
