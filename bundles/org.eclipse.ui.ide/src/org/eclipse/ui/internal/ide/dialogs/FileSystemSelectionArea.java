/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.internal.ide.filesystem.FileSystemConfiguration;
import org.eclipse.ui.internal.ide.filesystem.FileSystemMessages;
import org.eclipse.ui.internal.ide.filesystem.FileSystemSupportRegistry;

/**
 * FileSystemSelectionArea is the area used to select the file system.
 * @since 3.2
 */

public class FileSystemSelectionArea {

	private Label fileSystemTitle;
	private ComboViewer fileSystems;

	/**
	 * Create a new instance of the receiver.
	 */
	public FileSystemSelectionArea(){

	}

	/**
	 * Create the contents of the receiver in composite.
	 */
	public void createContents(Composite composite) {

		fileSystemTitle = new Label(composite, SWT.NONE);
		fileSystemTitle.setText(FileSystemMessages.FileSystemSelection_title);
		fileSystemTitle.setFont(composite.getFont());

		fileSystems = new ComboViewer(composite, SWT.READ_ONLY);
		fileSystems.getControl().setFont(composite.getFont());

		fileSystems.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FileSystemConfiguration) element).getLabel();
			}
		});

		fileSystems.setContentProvider(ArrayContentProvider.getInstance());

		fileSystems.setInput(FileSystemSupportRegistry.getInstance()
				.getConfigurations());
		fileSystems.setSelection(new StructuredSelection(
				FileSystemSupportRegistry.getInstance()
						.getDefaultConfiguration()));
	}

	/**
	 * Return the selected configuration.
	 * @return FileSystemConfiguration or <code>null</code> if nothing
	 * is selected.
	 */
	public FileSystemConfiguration getSelectedConfiguration() {
		ISelection selection = fileSystems.getSelection();

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection) selection;
			if (structured.size() == 1) {
				return (FileSystemConfiguration) structured.getFirstElement();
			}
		}

		return null;
	}

	/**
	 * Set the enablement state of the widget.
	 */
	public void setEnabled(boolean enabled) {
		fileSystemTitle.setEnabled(enabled);
		fileSystems.getControl().setEnabled(enabled);

	}
}
