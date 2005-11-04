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

package org.eclipse.debug.internal.ui.views.memory.renderings;

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.AbstractTableRendering;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;

public class FormatTableRenderingAction extends Action {

	AbstractTableRendering fRendering;
	
	private int fColumnSize = -1;
	private int fRowSize = -1;
	private boolean fSetDefault = false;
	
	class FormatTableRenderingDialog extends Dialog
	{
		private int[] fColumnSizes = new int[] {1, 2, 4, 8, 16};
		private int[] fRowSizes = new int[] {1, 2, 4, 8, 16};
		private Combo fColumnControl;
		private Combo fRowControl;
		
		private int fCurrentColIdx = -1;
		private int fCurrentRowIdx = -1;
		private Control fPreivewPage;
		private PageBook fPreviewPageBook;
		private Button fDefaultButton;
		

		protected FormatTableRenderingDialog(Shell parentShell) {
			super(parentShell);
			setShellStyle(getShellStyle() | SWT.RESIZE);
		}
		
		protected Control createDialogArea(Composite parent) {
			
			getShell().setText(DebugUIMessages.FormatTableRenderingAction_0);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(), IDebugUIConstants.PLUGIN_ID + ".FormatTableRenderingDialog_context"); //$NON-NLS-1$
			
			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			composite.setLayout(layout);
			GridData data = new GridData();
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			data.horizontalAlignment = SWT.FILL;
			data.verticalAlignment = SWT.FILL;
			data.heightHint = 250;
			data.widthHint = 300;
			composite.setLayoutData(data);
			
			Label label = new Label(composite, SWT.NONE);
			label.setText(DebugUIMessages.FormatTableRenderingAction_1);
			data = new GridData();
			data.grabExcessHorizontalSpace = true;
			data.horizontalAlignment = SWT.BEGINNING;
			data.horizontalSpan = 3;
			label.setLayoutData(data);
			
			Label rowLabel = new Label(composite, SWT.NONE);
			rowLabel.setText(DebugUIMessages.FormatTableRenderingAction_2);
			fRowControl = new Combo(composite, SWT.READ_ONLY);
			for (int i=0; i<fRowSizes.length; i++)
			{
				fRowControl.add(String.valueOf(fRowSizes[i]));
			}
			
			fRowControl.addSelectionListener(new SelectionListener() {

				public void widgetSelected(SelectionEvent e) {
					if (fCurrentRowIdx != fRowControl.getSelectionIndex())
					{
						fCurrentRowIdx = fRowControl.getSelectionIndex();
						refreshPreviewPage();
						updateOKButton();
					}
				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}});
			
			data = new GridData();
			data.grabExcessHorizontalSpace = true;
			data.horizontalAlignment = SWT.FILL;
			fRowControl.setLayoutData(data);
			
			Label unit = new Label(composite, SWT.NONE);
			unit.setText(DebugUIMessages.FormatTableRenderingAction_3);		
			
			Label columnLabel = new Label(composite, SWT.NONE);
			columnLabel.setText(DebugUIMessages.FormatTableRenderingAction_4);
			fColumnControl = new Combo(composite, SWT.READ_ONLY);
			for (int i=0; i<fColumnSizes.length; i++)
			{
				fColumnControl.add(String.valueOf(fColumnSizes[i]));
			}
			
			fColumnControl.addSelectionListener(new SelectionListener() {

				public void widgetSelected(SelectionEvent e) {
					if (fCurrentColIdx != fColumnControl.getSelectionIndex())
					{
						fCurrentColIdx = fColumnControl.getSelectionIndex();
						refreshPreviewPage();
						updateOKButton();
					}
				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}});
			
			data = new GridData();
			data.grabExcessHorizontalSpace = true;
			data.horizontalAlignment = SWT.FILL;
			fColumnControl.setLayoutData(data);
			
			unit = new Label(composite, SWT.NONE);
			unit.setText(DebugUIMessages.FormatTableRenderingAction_5);
			
			populateDialog();
			
			Button restoreButton = new Button(composite, SWT.NONE);
			restoreButton.setText(DebugUIMessages.FormatTableRenderingAction_6);
			restoreButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					int defaultRowSize = DebugUITools.getPreferenceStore().getInt(IDebugPreferenceConstants.PREF_ROW_SIZE);
					int defaultColSize = DebugUITools.getPreferenceStore().getInt(IDebugPreferenceConstants.PREF_COLUMN_SIZE);
					
					populateControl(defaultRowSize, fRowSizes, fRowControl);
					populateControl(defaultColSize, fColumnSizes, fColumnControl);
					
					fCurrentRowIdx = fRowControl.getSelectionIndex();
					fCurrentColIdx = fColumnControl.getSelectionIndex();
					
					refreshPreviewPage();
					updateOKButton();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}});
			data = new GridData();
			data.grabExcessHorizontalSpace = true;
			data.horizontalAlignment = SWT.END;
			data.horizontalSpan = 3;
			restoreButton.setLayoutData(data);
			
			Group group = new Group(composite, SWT.NONE);
			group.setText(DebugUIMessages.FormatTableRenderingAction_7);
			group.setLayout(new GridLayout());
			data = new GridData();
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			data.horizontalAlignment = SWT.FILL;
			data.verticalAlignment = SWT.FILL;
			data.horizontalSpan = 3;
			group.setLayoutData(data);
			
			fPreviewPageBook = new PageBook(group, SWT.NONE);
			data = new GridData();
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			data.horizontalAlignment = SWT.FILL;
			data.verticalAlignment = SWT.FILL;
			fPreviewPageBook.setLayoutData(data);
			
			int rowSize = fRowSizes[fRowControl.getSelectionIndex()];
			int colSize = fColumnSizes[fColumnControl.getSelectionIndex()];

			fPreivewPage = createPreviewPage(fPreviewPageBook, rowSize, colSize);
			fPreviewPageBook.showPage(fPreivewPage);
			
			fDefaultButton = new Button(composite, SWT.CHECK);
			fDefaultButton.setText(DebugUIMessages.FormatTableRenderingAction_8);
			data = new GridData();
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			data.horizontalAlignment = SWT.FILL;
			data.verticalAlignment = SWT.FILL;
			data.horizontalSpan = 3;
			fDefaultButton.setLayoutData(data);
			
			return composite;
		}

		protected void okPressed() {
			int idx = fColumnControl.getSelectionIndex();
			fColumnSize = fColumnSizes[idx];
			fRowSize = fRowSizes[fRowControl.getSelectionIndex()];
			fSetDefault = fDefaultButton.getSelection();
			
			super.okPressed();
		}
		
		private void populateDialog()
		{
			int currentColSize = fRendering.getAddressableUnitPerColumn();
			fCurrentColIdx = populateControl(currentColSize, fColumnSizes, fColumnControl);
			
			int currentRowSize = fRendering.getAddressableUnitPerLine();
			fCurrentRowIdx = populateControl(currentRowSize, fRowSizes, fRowControl);
		}

		private int populateControl(int currentSize, int[] searchArray, Combo control) {
			int idx = 0;
			for (int i=0 ;i<searchArray.length; i++)
			{
				if (searchArray[i] == currentSize)
				{
					idx = i;
					break;
				}
			}
			control.select(idx);
			return idx;
		}
		
		private Control createPreviewPage(Composite parent, int rowSize, int colSize)
		{			
			if (!isValid(rowSize, colSize))
			{	
				Label label = new Label(parent, SWT.NONE);
				StringBuffer errorMsg = new StringBuffer();
				errorMsg.append(DebugUIMessages.FormatTableRenderingAction_9);
				errorMsg.append("\n"); //$NON-NLS-1$
				errorMsg.append(DebugUIMessages.FormatTableRenderingAction_11);
				
				if (colSize > rowSize)
				{
					errorMsg.append("\n"); //$NON-NLS-1$
					errorMsg.append(DebugUIMessages.FormatTableRenderingAction_13);
				}
				
				label.setText(errorMsg.toString());
				
				return label;
			}
			
			Table table = new Table(parent, SWT.BORDER);
			table.setHeaderVisible(true);
			
			int numCol = rowSize/colSize;
			
			TableColumn addressCol = new TableColumn(table, SWT.NONE);
			
			TableColumn[] columns = new TableColumn[numCol];
			for (int i=0; i<columns.length; i++)
			{
				columns[i] = new TableColumn(table, SWT.NONE);
			}
			
			StringBuffer buf = new StringBuffer();
			for (int j=0; j<colSize; j++)
			{
				buf.append("X"); //$NON-NLS-1$
			}
			
			for (int i = 0; i < 4; i++) {
				TableItem tableItem = new TableItem(table, SWT.NONE);
				
				String[] text = new String[numCol + 1];
				text[0] = DebugUIMessages.FormatTableRenderingAction_15;
				for (int j=1; j<text.length; j++)
				{
					text[j] = buf.toString(); 
				}
				
				tableItem.setText(text);
			}
			
			addressCol.pack();
			for (int i=0; i<columns.length; i++)
			{
				columns[i].pack();
			}
			
			
			return table;
		}
		
		private boolean isValid(int rowSize, int colSize)
		{
			if (rowSize % colSize != 0)
				return false;
			
			if (colSize > rowSize)
				return false;
			
			return true;
		}

		private void refreshPreviewPage() {
			fPreivewPage.dispose();
			
			int rowSize = fRowSizes[fRowControl.getSelectionIndex()];
			int colSize = fColumnSizes[fColumnControl.getSelectionIndex()];
			fPreivewPage = createPreviewPage(fPreviewPageBook, rowSize, colSize);
			fPreviewPageBook.showPage(fPreivewPage);
		}

		private void updateOKButton() {
			int rowSize = fRowSizes[fRowControl.getSelectionIndex()];
			int colSize = fColumnSizes[fColumnControl.getSelectionIndex()];
			Button button = getButton(IDialogConstants.OK_ID);
			if (!isValid(rowSize, colSize))
			{
				button.setEnabled(false);
			}
			else
			{
				button.setEnabled(true);
			}
		}
		
	}
	
	public FormatTableRenderingAction(AbstractTableRendering rendering)
	{
		fRendering = rendering;
		setText(DebugUIMessages.FormatTableRenderingAction_16);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugUIConstants.PLUGIN_ID + ".FormatTableRenderingAction_context"); //$NON-NLS-1$
	}

	public void run() {
		FormatTableRenderingDialog dialog = new FormatTableRenderingDialog(DebugUIPlugin.getShell());
		dialog.open();
		if (fColumnSize > 0 && fRowSize > 0)
		{
			int addressableSize = fRendering.getAddressableSize();
			int columnSizeInBytes = addressableSize * fColumnSize;
			int rowSizeInBytes = addressableSize * fRowSize;
			fRendering.format(rowSizeInBytes, columnSizeInBytes);
			
			if (fSetDefault)
			{
				DebugUITools.getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_ROW_SIZE, fRowSize);
				DebugUITools.getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_COLUMN_SIZE, fColumnSize);
			}
		}
	}
}
