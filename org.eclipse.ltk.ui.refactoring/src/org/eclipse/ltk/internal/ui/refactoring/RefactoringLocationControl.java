/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;

/**
 * Control which maintains a selectable location field with persisted history.
 *
 * @since 3.2
 */
public class RefactoringLocationControl extends Composite {

	/** The combo */
	protected final Combo fCombo;

	/** The dialog settings key */
	protected final String fKey;

	/** The wizard */
	protected final IWizard fWizard;

	/** The maximum size of the history private static final int */
	protected final int MAX_HISTORY_SIZE= 5;

	/**
	 * Creates a new refactoring location control.
	 *
	 * @param wizard
	 *            the wizard
	 * @param parent
	 *            the parent control
	 * @param key
	 *            the dialog settings key
	 */
	public RefactoringLocationControl(final IWizard wizard, final Composite parent, final String key) {
		super(parent, SWT.NONE);
		final GridLayout gridLayout= new GridLayout(1, true);
		gridLayout.horizontalSpacing= 0;
		gridLayout.marginWidth= 0;
		setLayout(gridLayout);
		fCombo= new Combo(this, SWT.SINGLE | SWT.BORDER);
		fCombo.setLayoutData(createGridData(GridData.FILL_BOTH, 1, 0));
		Assert.isNotNull(wizard);
		Assert.isLegal(key != null && !"".equals(key)); //$NON-NLS-1$
		fWizard= wizard;
		fKey= key;
	}

	/**
	 * Creates a new grid data.
	 *
	 * @param flag
	 *            the flags to use
	 * @param hspan
	 *            the horizontal span
	 * @param indent
	 *            the indent
	 * @return the grid data
	 */
	protected GridData createGridData(final int flag, final int hspan, final int indent) {
		final GridData data= new GridData(flag);
		data.horizontalIndent= indent;
		data.horizontalSpan= hspan;
		return data;
	}

	/**
	 * Returns the combo control.
	 *
	 * @return the combo control
	 */
	public Combo getControl() {
		return fCombo;
	}

	/**
	 * Returns the text of this control.
	 *
	 * @return the text
	 */
	public String getText() {
		return fCombo.getText();
	}

	/**
	 * Restores the history of this control.
	 */
	public void loadHistory() {
		final IDialogSettings settings= fWizard.getDialogSettings();
		if (settings != null) {
			String[] locations= settings.getArray(fKey);
			if (locations == null || locations.length == 0)
				return;
			for (int index= 0; index < locations.length; index++)
				fCombo.add(locations[index]);
			fCombo.select(0);
		}
	}

	/**
	 * Saves the history of this control.
	 */
	public void saveHistory() {
		final IDialogSettings settings= fWizard.getDialogSettings();
		if (settings != null) {
			final LinkedList locations= new LinkedList();
			final String[] items= fCombo.getItems();
			for (int index= 0; index < items.length; index++)
				locations.add(items[index]);
			final String text= fCombo.getText().trim();
			if (!"".equals(text)) { //$NON-NLS-1$
				locations.remove(text);
				locations.addFirst(text);
			}
			final int size= locations.size();
			for (int index= 0; index < size - MAX_HISTORY_SIZE; index++)
				locations.removeLast();
			settings.put(fKey, (String[]) locations.toArray(new String[locations.size()]));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		fCombo.setEnabled(enabled);
	}

	/**
	 * Sets the text of this control.
	 *
	 * @param text
	 *            the text to set
	 */
	public void setText(final String text) {
		fCombo.setText(text);
	}
}