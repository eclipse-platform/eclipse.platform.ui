/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator.internal.filters;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * @since 3.2
 * 
 */
public class CustomizationTab extends Composite {

	protected static final int MARGIN = 3;

	protected static final int TABLE_HEIGHT = 20;

	protected static final int TABLE_WIDTH = 125;

	protected static final int LABEL_HEIGHT = 15;

	protected static final int LABEL_WIDTH = 150;

	private FontMetrics fontMetrics;

	private Table table;

	private final INavigatorContentService contentService;

	private CheckboxTableViewer tableViewer;
 
	protected CustomizationTab(Composite parent,
			INavigatorContentService aContentService) {
		super(parent, SWT.RESIZE);

		initializeDialogUnits();
		contentService = aContentService;
 
	}

	protected Table getTable() {
		return table;
	}

	protected void addSelectionChangedListener(
			ISelectionChangedListener selectionListener) {
		if (tableViewer != null)
			tableViewer
					.addSelectionChangedListener(selectionListener);
	}

	protected void createControl() {

		GridLayout layout = new GridLayout();
		layout.marginHeight = Dialog.convertVerticalDLUsToPixels(
				getFontMetrics(), MARGIN);
		layout.marginWidth = Dialog.convertHorizontalDLUsToPixels(
				getFontMetrics(), MARGIN);
		layout.verticalSpacing = Dialog.convertVerticalDLUsToPixels(
				getFontMetrics(), IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = Dialog.convertHorizontalDLUsToPixels(
				getFontMetrics(), IDialogConstants.HORIZONTAL_SPACING);
		// layout.numColumns = 1;
		setLayout(layout);
		setLayoutData(new GridData(GridData.FILL_BOTH));

		table = new Table(this, SWT.CHECK | SWT.BORDER | SWT.RESIZE | SWT.FULL_SELECTION);
		layoutTable(table);  

		tableViewer = new CheckboxTableViewer(getTable());

	} 

	protected void createInstructionsLabel(String labelText) {
		Label extensionsInstructionLabel = new Label(this, SWT.BOLD | SWT.WRAP);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL);
		gridData.horizontalIndent = Dialog.convertHorizontalDLUsToPixels(
				getFontMetrics(), 2);

		gridData.widthHint = Dialog.convertHorizontalDLUsToPixels(
				getFontMetrics(), LABEL_WIDTH);

		extensionsInstructionLabel.setLayoutData(gridData);
		extensionsInstructionLabel
				.setText(labelText);
	}
	

	private void layoutTable(Table aTable) {
		GridLayout tableLayout = new GridLayout();
		tableLayout.marginHeight = 0; // convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		tableLayout.marginWidth = 0; // convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		tableLayout.verticalSpacing = 0; // convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		tableLayout.horizontalSpacing = 0; // convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		tableLayout.numColumns = 2;
		GridData tableGridData = new GridData(GridData.FILL_BOTH);
		tableGridData.widthHint = Dialog.convertHorizontalDLUsToPixels(
				getFontMetrics(), TABLE_WIDTH);
//		tableGridData.heightHint = Dialog.convertVerticalDLUsToPixels(
//				getFontMetrics(), TABLE_HEIGHT);
		aTable.setLayout(tableLayout);
		aTable.setLayoutData(tableGridData);
	}

	private void initializeDialogUnits() {
		// Compute and store a font metric
		GC gc = new GC(this);
		gc.setFont(JFaceResources.getDialogFont());
		fontMetrics = gc.getFontMetrics();
		gc.dispose();
	}

	protected final FontMetrics getFontMetrics() {
		return fontMetrics;
	}

	protected final INavigatorContentService getContentService() {
		return contentService;
	}

	protected final CheckboxTableViewer getTableViewer() {
		return tableViewer;
	}

}
