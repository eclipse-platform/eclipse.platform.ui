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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.PlatformUI;

/**
 * Dialog to let the user select refactorings from the refactoring history to
 * export.
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
				handleExport(RefactoringUIMessages.ExportRefactoringHistoryControl_export_all_caption, fRefactoringHistory.getDescriptors());
			}
		});
		control.getExportButton().addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				handleExport(RefactoringUIMessages.ExportRefactoringHistoryControl_export_caption, fHistoryControl.getCheckedDescriptors());
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
	 * 
	 * @param caption
	 *            the caption of the export dialog
	 * @param proxies
	 *            the refactoring descriptor proxies to export
	 */
	private void handleExport(final String caption, final RefactoringDescriptorProxy[] proxies) {
		Assert.isNotNull(caption);
		Assert.isNotNull(proxies);
		RefactoringDescriptorProxy[] writable= proxies;
		final FileDialog dialog= new FileDialog(getShell(), SWT.SAVE);
		dialog.setText(caption);
		dialog.setFilterNames(new String[] { RefactoringUIMessages.ExportRefactoringHistoryControl_file_filter_name, RefactoringUIMessages.ExportRefactoringHistoryControl_wildcard_filter_name});
		dialog.setFilterExtensions(new String[] { RefactoringUIMessages.ExportRefactoringHistoryControl_file_filter_extension, RefactoringUIMessages.ExportRefactoringHistoryControl_wildcard_filter_extension});
		dialog.setFileName(RefactoringUIMessages.ExportRefactoringHistoryControl_file_default_name);
		final String path= dialog.open();
		if (path != null) {
			final File file= new File(path);
			if (file.exists()) {
				final MessageDialog message= new MessageDialog(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, null, Messages.format(RefactoringUIMessages.ExportRefactoringHistoryControl_file_overwrite_query, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}), MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL}, 0);
				final int result= message.open();
				if (result == 0) {
					InputStream stream= null;
					try {
						stream= new BufferedInputStream(new FileInputStream(file));
						final RefactoringDescriptorProxy[] existing= RefactoringCore.getRefactoringHistoryService().readRefactoringHistory(stream, RefactoringDescriptor.NONE).getDescriptors();
						final Set set= new HashSet();
						for (int index= 0; index < existing.length; index++)
							set.add(existing[index]);
						for (int index= 0; index < proxies.length; index++)
							set.add(proxies[index]);
						writable= new RefactoringDescriptorProxy[set.size()];
						set.toArray(writable);
					} catch (FileNotFoundException exception) {
						MessageDialog.openError(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, exception.getLocalizedMessage());
					} catch (CoreException exception) {
						final Throwable throwable= exception.getStatus().getException();
						if (throwable instanceof IOException)
							MessageDialog.openError(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, throwable.getLocalizedMessage());
						else
							RefactoringUIPlugin.log(exception);
					} finally {
						if (stream != null) {
							try {
								stream.close();
							} catch (IOException exception) {
								// Do nothing
							}
						}
					}
				} else if (result == 2)
					return;
			}
			OutputStream stream= null;
			try {
				stream= new BufferedOutputStream(new FileOutputStream(file));
				Arrays.sort(writable, new Comparator() {

					public final int compare(final Object first, final Object second) {
						final RefactoringDescriptorProxy predecessor= (RefactoringDescriptorProxy) first;
						final RefactoringDescriptorProxy successor= (RefactoringDescriptorProxy) second;
						return (int) (predecessor.getTimeStamp() - successor.getTimeStamp());
					}
				});
				RefactoringCore.getRefactoringHistoryService().writeRefactoringDescriptors(writable, stream, RefactoringDescriptor.NONE, new NullProgressMonitor());
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
}
