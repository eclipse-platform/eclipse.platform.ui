/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory.renderings;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


/**
 * Print view tab toolbar action
 * 
 * @since 3.0
 */
public class PrintTableRenderingAction extends Action
{
	private AbstractBaseTableRendering fRendering;
	private StructuredViewer fViewer;
	
	private static final String COLUMN_SEPERATOR = "  "; //$NON-NLS-1$
	
	public PrintTableRenderingAction(AbstractBaseTableRendering rendering, StructuredViewer viewer)
	{
		super(DebugUIMessages.PrintViewTabAction_title);
		fRendering = rendering;
		fViewer = viewer;
		setToolTipText(DebugUIMessages.PrintViewTabAction_tooltip);
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_PRINT_TOP_VIEW_TAB));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_PRINT_TOP_VIEW_TAB));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_PRINT_TOP_VIEW_TAB));		
	}

	/*
	 * draws a Table to a GC which has been initialized with a Printer.
	 * startJob() and startPage() must be called before printTable(...),
	 * and endPage() and endJob() must be called after printTable(...).
	 */
	private void printTable(TableItem[] itemList, GC printGC, Printer printer) {
		
		String tableContents = new String();
		int numColumns = ((Table)fViewer.getControl()).getColumnCount();
		ITableLabelProvider labelProvider = (ITableLabelProvider)fViewer.getLabelProvider();		
		int lineNum = 1;

		int charsPerByte = fRendering.getNumCharsPerByte();
		if (charsPerByte < 0)
			charsPerByte = 4;
		
		// return line number after column labels are printed
		lineNum = printColumnLabels(printGC, lineNum);

		//for all items in the table
		for (int i=0; i < itemList.length; i++) {
			tableContents = ""; //$NON-NLS-1$
			//print all columns for this row
			for (int j=0; j < numColumns; j++) {
				String columnText = labelProvider.getColumnText(itemList[i].getData(), j);
				
				while (columnText.length() < fRendering.getBytesPerColumn() * charsPerByte)
				{
					 columnText += " "; //$NON-NLS-1$
				}
				tableContents += COLUMN_SEPERATOR + columnText;							 
			}
			printGC.drawString(tableContents, 10, 10+(lineNum*printGC.getFontMetrics().getHeight()));
			lineNum++;

			// if we've run over the end of a page, start a new one
			if (20+lineNum*printGC.getFontMetrics().getHeight() > printer.getClientArea().height) {
				lineNum=1;
				printer.endPage();
				printer.startPage();
				lineNum = printColumnLabels(printGC, lineNum);
			}
		}
	}
	
	private int printColumnLabels(GC printGC, int lineNum)
	{	
		String tableContents = ""; //$NON-NLS-1$
		int numColumns = ((Table)fViewer.getControl()).getColumnCount();		
		TableColumn columns[] = ((Table)fViewer.getControl()).getColumns();
		
		int charsPerByte = fRendering.getNumCharsPerByte();
		if (charsPerByte < 0)
			charsPerByte = 4;
	
		//get the column headers
		for (int k=0; k < numColumns; k++) {
	
			StringBuffer columnLabel = new StringBuffer(columns[k].getText());
			int numBytes = 0;
	
			if (k > 0)
			{
				numBytes = fRendering.getBytesPerColumn();
			}
			else
			{
				// special for address column
				IMemoryBlock memBlock = fRendering.getMemoryBlock();
				if (memBlock instanceof IMemoryBlockExtension)
				{
					try {
						numBytes = ((IMemoryBlockExtension)memBlock).getAddressSize();
					} catch (DebugException e) {
						numBytes = 0;
					}
					
					if (numBytes <= 0)
						numBytes = 4;
				}
				else
				{
					numBytes = 4;
				}
		
			}
	
			 while (columnLabel.length() < numBytes * charsPerByte)
			 {
				 columnLabel.append(" "); //$NON-NLS-1$
			 }
	 
			tableContents += COLUMN_SEPERATOR + columnLabel;
		}
		printGC.drawString(tableContents, 10, 10+(lineNum*printGC.getFontMetrics().getHeight()));
		lineNum++;		
		
		return lineNum;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		
		if (!(fViewer.getControl() instanceof Table))
			return;
		
		PrintDialog printDialog = new PrintDialog(fViewer.getControl().getDisplay().getActiveShell());
		PrinterData printerData = printDialog.open();	// pop up a system print dialog
		if (printerData == null) {setChecked(false); return;}
		Printer printer = new Printer(printerData);
		GC gc = new GC(printer);
		TableItem[] tableItems = ((Table)fViewer.getControl()).getItems();
		
		// start the print job and assign it a title
		printer.startJob(DebugUIMessages.PrintViewTabAction_jobtitle + fRendering.getLabel());
		printer.startPage();					// start the first page
		printTable(tableItems, gc, printer);	// print all rows of the table
		printer.endPage();						// end the last page
		printer.endJob();						// end the print job
		gc.dispose();
		printer.dispose();
		setChecked(false);
	}
}
