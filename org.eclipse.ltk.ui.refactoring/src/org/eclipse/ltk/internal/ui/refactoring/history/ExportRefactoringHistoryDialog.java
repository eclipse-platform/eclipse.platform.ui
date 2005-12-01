/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.ui.refactoring.Assert;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.PlatformUI;

/**
 * Dialog to let the user select entries from the refactoring history to export.
 * 
 * @since 3.2
 */
public final class ExportRefactoringHistoryDialog extends RefactoringHistoryDialog {

	/**
	 * Creates a new export refactoring history dialog.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param configuration
	 *            the refactoring history dialog configuration to use
	 * @param history
	 *            the refactoring history to display
	 * @param id
	 *            the commit button's id
	 */
	public ExportRefactoringHistoryDialog(final Shell parent, final RefactoringHistoryDialogConfiguration configuration, final RefactoringHistory history, final int id) {
		super(parent, configuration, history, id);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IRefactoringHelpContextIds.REFACTORING_HISTORY_EXPORT_DIALOG);
	}

	/**
	 * {@inheritDoc}
	 */
	public void create() {
		super.create();
		final ExportRefactoringHistoryControl control= (ExportRefactoringHistoryControl) fHistoryControl;
		control.getExportAllButton().addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				handleExportAll();
			}
		});
		control.getExportButton().addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				handleExport();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	protected RefactoringHistoryControl createHistoryControl(Composite parent) {
		return new ExportRefactoringHistoryControl(parent, fDialogConfiguration);
	}

	/**
	 * Handles the export event.
	 */
	protected void handleExport() {
		handleExport(RefactoringUIMessages.ExportRefactoringHistoryDialog_export_caption, fHistoryControl.getSelectedDescriptors());
	}

	/**
	 * Handles the export event.
	 * 
	 * @param caption
	 *            the caption of the export dialog
	 * @param proxies
	 *            the refactoring descriptor proxies to export
	 */
	protected void handleExport(final String caption, final RefactoringDescriptorProxy[] proxies) {
		Assert.isNotNull(caption);
		Assert.isNotNull(proxies);
		final FileDialog dialog= new FileDialog(getShell(), SWT.SAVE);
		dialog.setText(caption);
		dialog.setFilterNames(new String[] { RefactoringUIMessages.ExportRefactoringHistoryDialog_file_filter_name});
		dialog.setFilterExtensions(new String[] { RefactoringUIMessages.ExportRefactoringHistoryDialog_file_filter_extension});
		dialog.setFileName(RefactoringUIMessages.ExportRefactoringHistoryDialog_file_default_name);
		final String path= dialog.open();
		if (path != null) {
			final File file= new File(path);
			if (file.exists()) {
				if (!MessageDialog.openQuestion(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, RefactoringUIMessages.ExportRefactoringHistoryDialog_file_overwrite_query))
					return;
			}
			OutputStream stream= null;
			try {
				stream= new BufferedOutputStream(new FileOutputStream(file));
				Arrays.sort(proxies, new Comparator() {

					public final int compare(final Object first, final Object second) {
						final RefactoringDescriptorProxy predecessor= (RefactoringDescriptorProxy) first;
						final RefactoringDescriptorProxy successor= (RefactoringDescriptorProxy) second;
						return (int) (predecessor.getTimeStamp() - successor.getTimeStamp());
					}
				});
				RefactoringCore.getRefactoringHistoryService().writeRefactoringDescriptors(proxies, stream);
			} catch (CoreException exception) {
				final Throwable throwable= exception.getStatus().getException();
				if (throwable instanceof IOException)
					MessageDialog.openError(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, throwable.getLocalizedMessage());
				else
					RefactoringUIPlugin.log(exception);
			} catch (FileNotFoundException exception) {
				MessageDialog.openError(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, exception.getLocalizedMessage());
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException exception) {
						// Do nothing
					}
				}
			}
		}
	}

	/**
	 * Handles the export all event.
	 */
	protected void handleExportAll() {
		handleExport(RefactoringUIMessages.ExportRefactoringHistoryDialog_export_all_caption, fRefactoringHistory.getDescriptors());
	}
}
