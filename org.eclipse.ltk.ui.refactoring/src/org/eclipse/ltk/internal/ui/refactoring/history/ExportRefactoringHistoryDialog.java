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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.ltk.internal.core.refactoring.history.IRefactoringSessionTransformer;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringDescriptorHandle;
import org.eclipse.ltk.internal.core.refactoring.history.XmlRefactoringSessionTransformer;
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

import org.w3c.dom.Node;

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
	 * @param input
	 *            the sorted input of the dialog
	 * @param id
	 *            the ID of the dialog button
	 */
	public ExportRefactoringHistoryDialog(final Shell parent, final ResourceBundle bundle, final RefactoringDescriptorHandle[] input, final int id) {
		super(parent, bundle, input, id);
	}

	/**
	 * @inheritDoc
	 */
	protected final void addDescriptor(final RefactoringDescriptorHandle handle, final boolean selected) {
		super.addDescriptor(handle, selected);
		if (selected) {
			if (fExportAllButton != null)
				fExportAllButton.setEnabled(true);
			if (fExportButton != null)
				fExportButton.setEnabled(true);
		}
	}

	/**
	 * @inheritDoc
	 */
	protected final void configureShell(final Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IRefactoringHelpContextIds.REFACTORING_HISTORY_EXPORT_DIALOG);
	}

	/**
	 * Creates a new core exception from the specified throwable
	 * 
	 * @param throwable
	 *            the throwable
	 * @return the created core exception
	 */
	protected CoreException createCoreException(Throwable throwable) {
		return new CoreException(new Status(IStatus.ERROR, RefactoringUIPlugin.getPluginId(), 0, throwable.getLocalizedMessage(), null));
	}

	/**
	 * @inheritDoc
	 */
	protected final Tree createHistoryTree(final Composite parent) {
		Assert.isNotNull(parent);
		return new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
	}

	/**
	 * @inheritDoc
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
	 * @param handles
	 *            the refactoring descriptor handles to export
	 */
	protected final void handleExport(final String caption, final RefactoringDescriptorHandle[] handles) {
		Assert.isNotNull(caption);
		Assert.isNotNull(handles);
		final FileDialog dialog= new FileDialog(getShell(), SWT.SAVE);
		dialog.setText(caption);
		dialog.setFilterNames(new String[] { RefactoringUIMessages.ExportRefactoringHistoryDialog_file_filter_name});
		dialog.setFilterExtensions(new String[] { RefactoringUIMessages.ExportRefactoringHistoryDialog_file_filter_extension});
		dialog.setFileName(RefactoringUIMessages.ExportRefactoringHistoryDialog_file_default_name);
		final String path= dialog.open();
		if (path != null) {
			try {
				final IRefactoringSessionTransformer transformer= new XmlRefactoringSessionTransformer();
				try {
					transformer.beginSession(null);
					for (int index= 0; index < handles.length; index++) {
						final RefactoringDescriptorHandle handle= handles[index];
						final RefactoringDescriptor descriptor= handle.resolveDescriptor();
						if (descriptor != null) {
							try {
								transformer.beginRefactoring(descriptor.getID(), descriptor.getProject(), descriptor.getDescription(), descriptor.getComment());
								for (final Iterator iterator= descriptor.getArguments().entrySet().iterator(); iterator.hasNext();) {
									final Map.Entry entry= (Entry) iterator.next();
									transformer.createArgument((String) entry.getKey(), (String) entry.getValue());
								}
							} finally {
								transformer.endRefactoring();
							}
						}
					}
				} finally {
					transformer.endSession();
				}
				final Object result= transformer.getResult();
				if (result instanceof Node) {
					OutputStream stream= null;
					try {
						final File file= new File(path);
						if (file.exists()) {
							if (!MessageDialog.openQuestion(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, RefactoringUIMessages.ExportRefactoringHistoryDialog_file_overwrite_query))
								return;
						}
						stream= new BufferedOutputStream(new FileOutputStream(file));
						final Transformer transform= TransformerFactory.newInstance().newTransformer();
						transform.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
						transform.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
						transform.transform(new DOMSource((Node) result), new StreamResult(stream));
					} catch (TransformerConfigurationException exception) {
						throw createCoreException(exception);
					} catch (TransformerFactoryConfigurationError exception) {
						throw createCoreException(exception);
					} catch (TransformerException exception) {
						if (exception.getException() instanceof IOException)
							throw (IOException) exception.getException();
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
				}
			} catch (IOException exception) {
				MessageDialog.openError(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, exception.getLocalizedMessage());
			} catch (CoreException exception) {
				RefactoringUIPlugin.log(exception);
			}
		}
	}

	/**
	 * Handles the export all event.
	 */
	protected final void handleExportAll() {
		handleExport(RefactoringUIMessages.ExportRefactoringHistoryDialog_export_all_caption, fHistoryInput);
	}

	/**
	 * @inheritDoc
	 */
	protected final void handleSelection(final Widget widget, final Object object, final boolean check) {
		super.handleSelection(widget, object, check);

		fExportAllButton.setEnabled(true);
		fExportButton.setEnabled(true);
	}
}
