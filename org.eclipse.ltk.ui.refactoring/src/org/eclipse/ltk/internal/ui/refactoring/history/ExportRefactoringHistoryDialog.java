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
import java.util.ResourceBundle;

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.PlatformUI;

/**
 * Dialog to let the user select entries from the refactoring history to export.
 * 
 * @since 3.2
 */
public final class ExportRefactoringHistoryDialog extends RefactoringHistoryDialog {

	/** The export all label key */
	private static final String EXPORT_ALL_LABEL= "exportAllLabel"; //$NON-NLS-1$

	/** The export label key */
	private static final String EXPORT_LABEL= "exportLabel"; //$NON-NLS-1$

	/** The export all button, or <code>null</code> */
	private Button fExportAllButton= null;

	/** The export button, or <code>null</code> */
	private Button fExportButton= null;

	/**
	 * Creates a new export refactoring history dialog.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param bundle
	 *            the resource bundle to use
	 * @param history
	 *            the refactoring history to display
	 * @param id
	 *            the ID of the dialog button
	 */
	public ExportRefactoringHistoryDialog(final Shell parent, final ResourceBundle bundle, final RefactoringHistory history, final int id) {
		super(parent, bundle, history, id);
	}

	/**
	 * {@inheritDoc}
	 */
	protected final void addDescriptor(final RefactoringDescriptorProxy proxy, final boolean selected) {
		super.addDescriptor(proxy, selected);
		if (selected) {
			getShell().getDisplay().syncExec(new Runnable() {

				public final void run() {
					if (fExportAllButton != null)
						fExportAllButton.setEnabled(true);
					if (fExportButton != null)
						fExportButton.setEnabled(true);
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected final void configureShell(final Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IRefactoringHelpContextIds.REFACTORING_HISTORY_EXPORT_DIALOG);
	}

	/**
	 * {@inheritDoc}
	 */
	protected final Tree createHistoryTree(final Composite parent) {
		Assert.isNotNull(parent);
		return new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
	}

	/**
	 * {@inheritDoc}
	 */
	protected final void createVerticalButtonBar(final Composite parent) {
		Assert.isNotNull(parent);
		final Composite composite= new Composite(parent, SWT.NONE);
		final GridLayout layout= new GridLayout(1, false);
		composite.setLayout(layout);

		GridData data= new GridData();
		data.grabExcessHorizontalSpace= false;
		data.grabExcessVerticalSpace= true;
		data.horizontalAlignment= SWT.FILL;
		data.verticalAlignment= SWT.TOP;
		composite.setLayoutData(data);

		fExportButton= new Button(composite, SWT.NONE);
		fExportButton.setEnabled(false);
		fExportButton.setText(fBundle.getString(EXPORT_LABEL));
		fExportButton.addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				handleExport();
			}
		});
		data= new GridData();
		data.horizontalAlignment= SWT.FILL;
		fExportButton.setLayoutData(data);

		fExportAllButton= new Button(composite, SWT.NONE);
		fExportAllButton.setEnabled(false);
		fExportAllButton.setText(fBundle.getString(EXPORT_ALL_LABEL));
		fExportAllButton.addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				handleExportAll();
			}
		});

		data= new GridData();
		data.horizontalAlignment= SWT.FILL;
		fExportAllButton.setLayoutData(data);
	}

	/**
	 * Handles the export event.
	 */
	protected final void handleExport() {
		handleExport(RefactoringUIMessages.ExportRefactoringHistoryDialog_export_caption, getSelection());
	}

	/**
	 * Handles the export event.
	 * 
	 * @param caption
	 *            the caption of the export dialog
	 * @param proxies
	 *            the refactoring descriptor proxies to export
	 */
	protected final void handleExport(final String caption, final RefactoringDescriptorProxy[] proxies) {
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
				final RefactoringDescriptorProxy[] result= new RefactoringDescriptorProxy[proxies.length];
				for (int index= 0; index < proxies.length; index++)
					result[proxies.length - 1 - index]= proxies[index];
				RefactoringCore.getRefactoringHistoryService().writeRefactoringHistory(result, stream);
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
	protected final void handleExportAll() {
		handleExport(RefactoringUIMessages.ExportRefactoringHistoryDialog_export_all_caption, fDescriptorProxies);
	}

	/**
	 * {@inheritDoc}
	 */
	protected final void handleSelection(final Widget widget, final Object object, final boolean check) {
		super.handleSelection(widget, object, check);

		fExportAllButton.setEnabled(true);
		fExportButton.setEnabled(true);
	}
}
