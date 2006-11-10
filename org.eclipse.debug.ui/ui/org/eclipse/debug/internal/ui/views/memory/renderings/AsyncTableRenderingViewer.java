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

package org.eclipse.debug.internal.ui.views.memory.renderings;

import java.math.BigInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.memory.provisional.AbstractAsyncTableRendering;
import org.eclipse.debug.internal.ui.viewers.AbstractUpdatePolicy;
import org.eclipse.debug.internal.ui.viewers.AsynchronousModel;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.ui.commands.IStatusMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.progress.UIJob;

public class AsyncTableRenderingViewer extends AsyncVirtualContentTableViewer {

	private AbstractAsyncTableRendering fRendering;
	
	// selection keys
	private Object fPendingSelection;
	private Object fSelectionKey;
	
	// cursor and associated listeners
	private TableCursor fTableCursor;
	private KeyAdapter fCursorKeyAdapter;
	private TraverseListener fCursorTraverseListener;
	private MouseAdapter fCursorMouseListener;
	private SelectionAdapter fCursorSelectionListener;

	// cursor editor and associated listeners
	private TableEditor fCursorEditor;
	private FocusAdapter fEditorFocusListener;
	private KeyAdapter fEditorKeyListener;
	
	private boolean fPendingFormatViewer;
	
	public AsyncTableRenderingViewer(AbstractAsyncTableRendering rendering, Composite parent, int style) {
		super(parent, style);
		fRendering = rendering;
		
		getTable().addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				handleTableMouseEvent(e);
			}});
		
		createCursor(getTable());
	}

	public AbstractUpdatePolicy createUpdatePolicy() {
		return new AsyncTableRenderingUpdatePolicy();
	}
	
	public AbstractAsyncTableRendering getRendering()
	{
		return fRendering;
	}
	
	private void createCursor(Table table)
	{
		fTableCursor = new TableCursor(table, SWT.NONE);
		Display display = fTableCursor.getDisplay();
		
		// set up cursor color
		fTableCursor.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
		fTableCursor.setForeground(display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
		
		fTableCursor.setFont(JFaceResources.getFont(IInternalDebugUIConstants.FONT_NAME));
		
		fCursorKeyAdapter = new KeyAdapter() {
			public void keyPressed(KeyEvent e)
			 {
			 	handleCursorKeyPressed(e);
			 }	
		};
		
		fTableCursor.addKeyListener(fCursorKeyAdapter);
		
		fCursorTraverseListener = new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				handleCursorTraverseEvt(e);
			}};
					
		fTableCursor.addTraverseListener(fCursorTraverseListener);
		
		fCursorMouseListener = new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				handleCursorMouseEvent(e);
			}};
		fTableCursor.addMouseListener(fCursorMouseListener);
		
		// cursor may be disposed before disposed is called
		// remove listeners whenever the cursor is disposed
		fTableCursor.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (fTableCursor == null)
					return;
				fTableCursor.removeTraverseListener(fCursorTraverseListener);
				fTableCursor.removeKeyListener(fCursorKeyAdapter);
				fTableCursor.removeMouseListener(fCursorMouseListener);
				fTableCursor.removeSelectionListener(fCursorSelectionListener);
			}});
		
		fCursorSelectionListener = new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						handleCursorMoved();
					}
				};
		fTableCursor.addSelectionListener(fCursorSelectionListener);
		fCursorEditor = new TableEditor (getTable());	
	}
	
	private void handleCursorKeyPressed(KeyEvent event)
	{
		if (event.character == '\r' && event.getSource() instanceof TableCursor)
		{
			activateCellEditor(null);
			return;
		}		
		
		if (MemoryViewUtil.isValidEditEvent(event.keyCode))
		{	
			// activate edit as soon as user types something at the cursor
			if (event.getSource() instanceof TableCursor)
			{
				String initialValue = String.valueOf(event.character);
				activateCellEditor(initialValue);
				return;
			}
		}
	}
	
	private void handleCursorMouseEvent(MouseEvent e){
		if (e.button == 1)
		{
			int col = fTableCursor.getColumn();
			if (col > 0 && col <= (getNumCol()))
				activateCellEditor(null);
		}			
	}
	
	private void handleCursorTraverseEvt(TraverseEvent e){
		if (fTableCursor.getRow() == null)
			return;
		
		Table table = (Table)fTableCursor.getParent();
		int row = table.indexOf(fTableCursor.getRow());
		int col = fTableCursor.getColumn();
		if (col == getNumCol() && e.keyCode == SWT.ARROW_RIGHT)
		{
			if (row + 1>= table.getItemCount())
			{
				return;
			}
			
			row = row +1;
			col = 0;
			fTableCursor.setSelection(row, col);
		}
		if (col <= 1 && e.keyCode == SWT.ARROW_LEFT)
		{
			if (row-1 < 0)
			{
				return;
			}
			
			row = row - 1;
			col = getNumCol()+1;
			fTableCursor.setSelection(row, col);
		}			
		
		handleCursorMoved();
	}
	
	/**
	 * Update selected address.
	 * Load more memory if required.
	 */
	private void handleCursorMoved()
	{	
		fSelectionKey = getSelectionKeyFromCursor();
		fPendingSelection = null;
		
		if (AsyncVirtualContentTableViewer.DEBUG_DYNAMIC_LOADING)
			System.out.println(Thread.currentThread().getName() + " cursor moved selection is: " + ((BigInteger)fSelectionKey).toString(16)); //$NON-NLS-1$
		
		// now check to see if the cursor is approaching buffer limit
		handleScrollBarSelection();
		fireSelectionChanged(fSelectionKey);
	}
	
	private int getNumCol() {
		
		int bytesPerLine = fRendering.getBytesPerLine();
		int columnSize = fRendering.getBytesPerColumn();
		
		return bytesPerLine/columnSize;
	}
	
	/**
	 * Sets the cursor at the specified address
	 * @param key selection key
	 */
	public void setSelection(Object key)
	{
		fPendingSelection = key;
		attemptSetKeySelection();
	}
	
	public Object getSelectionKey()
	{
		return fSelectionKey;
	}
	
	private synchronized void attemptSetKeySelection()
	{
		if (fPendingSelection != null) {
            doAttemptSetKeySelection(fPendingSelection);
		}
		
	}
	
	synchronized private Object doAttemptSetKeySelection(final Object key)
	{	
		if (getBufferTopKey() == null || getBufferEndKey() == null)
			return key;
		
		// calculate selected row address
		int[] location = getCoordinatesFromKey(key);
		if(location.length == 0)
		{
			return key;
		}
		
		UIJob uiJob = new UIJob("Set Cursor Selection"){ //$NON-NLS-1$

			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					if (AsyncVirtualContentTableViewer.DEBUG_DYNAMIC_LOADING)
						System.out.println(getRendering() + " set cursor selection " + ((BigInteger)key).toString(16)); //$NON-NLS-1$
					
					if (fPendingSelection != null && fPendingSelection != key)
						return Status.OK_STATUS;
					
					if (fTableCursor.isDisposed())
						return Status.OK_STATUS;
					
					// by the time this is called, the location may not be valid anymore
					int[] newLocation = getCoordinatesFromKey(key);
					if (newLocation.length == 0)
					{
						Object selectionKey = getSelectionKey();
						fPendingSelection = selectionKey;
						return Status.OK_STATUS;
					}
					
					fSelectionKey = key;
					fPendingSelection = null;
					
					if (AsyncVirtualContentTableViewer.DEBUG_DYNAMIC_LOADING)
					{
						System.out.println(getRendering() + " set cursor selection, row is " + getTable().getItem(newLocation[0]).getData()); //$NON-NLS-1$
						System.out.println(getRendering() + " set cursor selection, model is " + getVirtualContentModel().getElement(newLocation[0])); //$NON-NLS-1$
					}
					
					fTableCursor.setSelection(newLocation[0], newLocation[1]);
					showTableCursor(true);
					
					int topIndex = getTable().getTopIndex();
					Object topKey = getVirtualContentModel().getKey(topIndex);
					setTopIndexKey(topKey);
					
				} catch (RuntimeException e) {
					
					// by the time this is called, the selection may no longer
					// get the latest selection and try to set selection again
					Object selectionKey = getSelectionKey();
					fPendingSelection = selectionKey;
					doAttemptSetKeySelection(selectionKey);
				}
				return Status.OK_STATUS;
			}};
			
		uiJob.setSystem(true);
		uiJob.schedule();
		
		return null;
	}
	
	/**
	 * 
	 * @param key
	 * @return the coordinates of the key
	 * Element[0] is the row index
	 * Element[1] is the column index
	 */
	private int[] getCoordinatesFromKey(Object key)
	{
		final int row = indexOf(key);
		
		if (row == -1)
		{
			return new int[0];
		}
		
		Object element = getVirtualContentModel().getElement(row);
		final int col = columnOf(element, key);
		
		if (col == -1)
		{
			return new int[0];
		}
		return new int[]{row, col};
	}
	
	private Object getSelectionKeyFromCursor()
	{	
		int idx = getTable().indexOf(fTableCursor.getRow());		
		int col = fTableCursor.getColumn();
		
		return getVirtualContentModel().getKey(idx, col);
	}
	
	private Object getBufferTopKey()
	{
		return getKey(0);
	}
	
	private Object getBufferEndKey()
	{
		AbstractVirtualContentTableModel model = getVirtualContentModel();
		
		if (model != null)
			return getKey(model.getElements().length-1);
		return null;
	}
	
	public int indexOf(Object key)
	{
		int idx = -1;
		AbstractVirtualContentTableModel model = getVirtualContentModel();
		if (model != null)
			idx = model.indexOfKey(key);
		return idx;
	}
	
	private int columnOf(Object element, Object key)
	{
		int idx = -1;
		AbstractVirtualContentTableModel model = getVirtualContentModel();
		if (model != null)
		{
			idx = model.columnOf(element, key);
		}
		return idx;
	}
	
	public Object getKey(int index)
	{
		AbstractVirtualContentTableModel model = getVirtualContentModel();
		if (model != null)
		{
			Object key = model.getKey(index);
			return key;
		}
		return null;
	}
	
	public Object getKey(int row, int col)
	{		
		AbstractVirtualContentTableModel model = getVirtualContentModel();
		if (model != null)
			return model.getKey(row, col);
		return null;
	}
	
	
	protected synchronized void preservingSelection(Runnable updateCode) {
		Object oldTopIndexKey = null;
		
		if (getPendingSetTopIndexKey() == null) {
			// preserve selection
			oldTopIndexKey = getTopIndexKey();
		}
		else
		{
			oldTopIndexKey = getPendingSetTopIndexKey();
		}
		Object oldSelectionKey = null;
		try {
			if (AsyncVirtualContentTableViewer.DEBUG_DYNAMIC_LOADING)
			{
				if (oldTopIndexKey != null)
					System.out.println(getRendering() + " preserve top index: " + ((BigInteger)oldTopIndexKey).toString(16)); //$NON-NLS-1$
				else
					System.out.println("top index key is null, nothing to preserve"); //$NON-NLS-1$
			}

			if (fPendingSelection != null)
				oldSelectionKey = fPendingSelection;
			else
				oldSelectionKey = getSelectionKey();
			
			if (AsyncVirtualContentTableViewer.DEBUG_DYNAMIC_LOADING)
			{
				if (oldTopIndexKey != null)
					System.out.println(getRendering() + " preserve selection: " + ((BigInteger)oldSelectionKey).toString(16)); //$NON-NLS-1$
				else
					System.out.println("selection key is null, nothing to preserve"); //$NON-NLS-1$
			}
			
			// perform the update
			updateCode.run();
			
		} finally {
			
			if (oldSelectionKey != null)
			{
				if (AsyncVirtualContentTableViewer.DEBUG_DYNAMIC_LOADING)
					System.out.println(getRendering() + " preserved selection " + ((BigInteger)oldSelectionKey).toString(16)); //$NON-NLS-1$
				setSelection(oldSelectionKey);
			}
			
			if (getPendingSetTopIndexKey() != null)
			{
				if (AsyncVirtualContentTableViewer.DEBUG_DYNAMIC_LOADING)
					System.out.println(getRendering() + " finished top index: " + ((BigInteger)oldTopIndexKey).toString(16)); //$NON-NLS-1$
				setTopIndex(getPendingSetTopIndexKey());
			}
			else if (oldTopIndexKey != null)
			{
				setTopIndex(oldTopIndexKey);
				
				if (AsyncVirtualContentTableViewer.DEBUG_DYNAMIC_LOADING)
					System.out.println(getRendering() + " finished top index: " + ((BigInteger)oldTopIndexKey).toString(16)); //$NON-NLS-1$
			}
		}
	}
	
	public void dispose()
	{
		super.dispose();
		
		if (fTableCursor != null && !fTableCursor.isDisposed())
		{
			fCursorEditor.dispose();
			fCursorEditor = null;
			
			fTableCursor.removeTraverseListener(fCursorTraverseListener);
			fTableCursor.removeKeyListener(fCursorKeyAdapter);
			fTableCursor.removeMouseListener(fCursorMouseListener);
			fTableCursor.removeSelectionListener(fCursorSelectionListener);
			
			fTableCursor.dispose();
			fTableCursor = null;
		}
	}
	
	public void showTableCursor(final boolean show)
	{
		
		Display display = DebugUIPlugin.getDefault().getWorkbench().getDisplay();
		if (Thread.currentThread() == display.getThread())
		{
			if (!fTableCursor.isDisposed())
			{
				if (fTableCursor.isVisible() != show)
					fTableCursor.setVisible(show);
			}
		}
		else
		{
			UIJob job = new UIJob("show table cursor"){ //$NON-NLS-1$
	
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (!fTableCursor.isDisposed())
					{
						if (fTableCursor.isVisible() != show)
							fTableCursor.setVisible(show);
					}
					return Status.OK_STATUS;
				}};
				
			job.setSystem(true);
			job.schedule();
		}
	}

	private void handleTableMouseEvent(MouseEvent e) {
		// figure out new cursor position based on here the mouse is pointing
		TableItem[] tableItems = getTable().getItems();
		TableItem selectedRow = null;
		int colNum = -1;
		int numCol = getColumnProperties().length;
		
		for (int j=0; j<tableItems.length; j++)
		{
			TableItem item = tableItems[j];
			if (item.getData() != null)
			{
				for (int i=0; i<numCol; i++)
				{
					Rectangle bound = item.getBounds(i);
					if (bound.contains(e.x, e.y))
					{
						colNum = i;
						selectedRow = item;
						break;
					}
				}
			}
			if (colNum >= 0)
				break;
		}
		
		// if column position cannot be determined, return
		if (colNum < 1)
			return;
		
		// handle user mouse click onto table
		// move cursor to new position
		if (selectedRow != null)
		{
			int row = getTable().indexOf(selectedRow);
			showTableCursor(true);
			fTableCursor.setSelection(row, colNum);
			
			// manually call this since we don't get an event when
			// the table cursor changes selection.
			handleCursorMoved();
			
			fTableCursor.setFocus();
		}			
	}

	/**
	 * Activate cell editor and prefill it with initial value.
	 * If initialValue is null, use cell content as initial value
	 * @param initialValue
	 */
	private void activateCellEditor(String initialValue) {
		
		int col = fTableCursor.getColumn();
		int row = indexOf(fSelectionKey);
		
		if (row < 0)
			return;
		
		// do not allow user to edit address column
		if (col == 0 || col > getNumCol())
		{
			return;
		}
		
		ICellModifier cellModifier = null;
		
		cellModifier = getCellModifier();
		
		TableItem tableItem = getTable().getItem(row);
		
		Object element = tableItem.getData();
		
		if (element != null)
		{
			Object property = getColumnProperties()[col];
			Object value = cellModifier.getValue(element, (String)property);
			
			// The cell modifier canModify function always returns false if the edit action 
			// is not invoked from here.  This is to prevent data to be modified when
			// the table cursor loses focus from a cell.  By default, data will
			// be changed in a table when the cell loses focus.  This is to workaround
			// this default behaviour and only change data when the cell editor
			// is activated.
			((AsyncTableRenderingCellModifier)cellModifier).setEditActionInvoked(true);
			boolean canEdit = cellModifier.canModify(element, (String)property);
			((AsyncTableRenderingCellModifier)cellModifier).setEditActionInvoked(false);
			
			if (!canEdit)
				return;
			
			// activate based on current cursor position
			TextCellEditor selectedEditor = (TextCellEditor)getCellEditors()[col];
	
			if (selectedEditor != null)
			{
				// The control that will be the editor must be a child of the Table
				Text text = (Text)selectedEditor.getControl();
				
				String cellValue  = null;
				
				if (initialValue != null)
				{
					cellValue = initialValue;	
				}
				else	
				{
					cellValue = ((String)value);
				}
				
				text.setText(cellValue);
		
				fCursorEditor.horizontalAlignment = SWT.LEFT;
				fCursorEditor.grabHorizontal = true;
		
				// Open the text editor in selected column of the selected row.
				fCursorEditor.setEditor (text, tableItem, col);
		
				// Assign focus to the text control
				selectedEditor.setFocus();
				
				if (initialValue != null)
				{
					text.clearSelection();
				}
				
				text.setFont(JFaceResources.getFont(IInternalDebugUIConstants.FONT_NAME));
	
				// add listeners for the text control
				addListeners(text);
				
				// move cursor below text control
				fTableCursor.moveBelow(text);
			}
		}
	}
	
	/*
	 * @param text
	 */
	private void addListeners(Text text) {
		fEditorFocusListener = new FocusAdapter() {
			public void focusLost(FocusEvent e)
			{
				handleTableEditorFocusLost(e);
			}
		};
		text.addFocusListener(fEditorFocusListener);
		
		fEditorKeyListener = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyEventInEditor(e);
			}
		};

		text.addKeyListener(fEditorKeyListener);
	}
	
	private void handleTableEditorFocusLost(FocusEvent event)
	{
		final FocusEvent e = event;

		Display.getDefault().syncExec(new Runnable() {

			public void run()
			{
				try
				{
					int row = indexOf(fSelectionKey);
					int col = fTableCursor.getColumn();
					
					Text text = (Text)e.getSource();
					removeListeners(text);

					// get new value
					String newValue = text.getText();
					
					// modify memory at fRow and fCol
					modifyValue(row, col, newValue);
							
					// show cursor after modification is completed
					setSelection(fSelectionKey);
					fTableCursor.moveAbove(text);
					showTableCursor(true);
				}
				catch (NumberFormatException e1)
				{
					MemoryViewUtil.openError(DebugUIMessages.MemoryViewCellModifier_failure_title, 
						DebugUIMessages.MemoryViewCellModifier_data_is_invalid, null);
				}		
			}
		});		
	}
	
	/**
	 * @param event
	 */
	private void handleKeyEventInEditor(KeyEvent event) {
		
		final KeyEvent e = event;
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				Text text = (Text)e.getSource();
				int row = indexOf(fSelectionKey);
				int col = fTableCursor.getColumn();
				
				try
				{
					switch (e.keyCode)
					{
						case SWT.ARROW_UP :
							
							// move text editor box up one row		
							if (row-1 < 0)
								return;
						
							// modify value for current cell
							modifyValue(row, col, text.getText());
													
							row--;

							//	update cursor location and selection in table	
							fTableCursor.setSelection(row, col);
							handleCursorMoved();
							
							// remove listeners when focus is lost
							removeListeners(text);
							activateCellEditor(null);
							break;
						case SWT.ARROW_DOWN :
							
							// move text editor box down one row
							
							if (row+1 >= getTable().getItemCount())
								return;
							
							// modify value for current cell
							modifyValue(row, col, text.getText());
						
							row++;
							
							//	update cursor location and selection in table								
							fTableCursor.setSelection(row, col);
							handleCursorMoved();
												
							// remove traverse listener when focus is lost
							removeListeners(text);
							activateCellEditor(null);		
							break;
						case 0:
							
						// if user has entered the max number of characters allowed in a cell, move to next cell
						// Extra changes will be used as initial value for the next cell
							int numCharsPerByte = fRendering.getNumCharsPerByte();
							if (numCharsPerByte > 0)
							{
								if (text.getText().length() > fRendering.getBytesPerColumn()*numCharsPerByte)
								{
									String newValue = text.getText();
									text.setText(newValue.substring(0, fRendering.getBytesPerColumn()*numCharsPerByte));
									
									modifyValue(row, col, text.getText());
									
									// if cursor is at the end of a line, move to next line
									if (col >= getNumCol())
									{
										col = 1;
										row++;
									}
									else
									{
										// move to next column
										row++;
									}
									
									// update cursor position and selected address
									fTableCursor.setSelection(row, col);
									handleCursorMoved();
									
									removeListeners(text);
						
									// activate text editor at next cell
									activateCellEditor(newValue.substring(fRendering.getBytesPerColumn()*numCharsPerByte));
								}
							}
							break;	
						case SWT.ESC:

							// if user has pressed escape, do not commit the changes
							// that's why "modifyValue" is not called
							fTableCursor.setSelection(row, col);
							handleCursorMoved();
					
							removeListeners(text);
							
							// cursor needs to have focus to remove focus from cell editor
							fTableCursor.setFocus();
							break;	
						default :
							numCharsPerByte = fRendering.getNumCharsPerByte();
							if (numCharsPerByte > 0)
							{								
								if (text.getText().length()> fRendering.getBytesPerColumn()* numCharsPerByte)
								{
									String newValue = text.getText();
									text.setText(newValue.substring(0,fRendering.getBytesPerColumn()* numCharsPerByte));
									modifyValue(row, col, text.getText());
									// if cursor is at the end of a line, move to next line
									if (col >= getNumCol())
									{
										col = 1;
										row++;
									}
									else
									{
										col++;
									}
									
									fTableCursor.setSelection(row, col);
									handleCursorMoved();
									
									removeListeners(text);
									
									activateCellEditor(newValue.substring(fRendering.getBytesPerColumn()*numCharsPerByte));
								}
							}
						break;
					}
				}
				catch (NumberFormatException e1)
				{
					MemoryViewUtil.openError(DebugUIMessages.MemoryViewCellModifier_failure_title, 
						DebugUIMessages.MemoryViewCellModifier_data_is_invalid, null);
					
					fTableCursor.setSelection(row, col);
					handleCursorMoved();
			
					removeListeners(text);
				}
			}
		});
	}
	
	/**
	 * @param text
	 */
	private void removeListeners(Text text) {
		
		text.removeFocusListener(fEditorFocusListener);
		text.removeKeyListener(fEditorKeyListener);
	}
	
	/**
	 * Modify value and send new value to debug adapter
	 * @param row
	 * @param col
	 * @param newValue
	 * @throws NumberFormatException
	 */
	private void modifyValue(int row, int col, String newValue) throws NumberFormatException
	{
		if (newValue.length() == 0)
		{	
			// do not do anything if user has not entered anything
			return;
		}
		
		if (row >= 0 && row < getTable().getItemCount())
		{
			TableItem tableItem = getTable().getItem(row);
	
			Object property = getColumnProperties()[col];
			getCellModifier().modify(tableItem, (String)property, newValue);
		}
	}
	
	public TableCursor getCursor()
	{
		return fTableCursor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ContentViewer#getLabelProvider()
	 * Implemented minimum to work with PrintTableRendering action.
	 * This is not a real table labe provider, only goes to the table
	 * to get the text at the specified row and column.
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new ITableLabelProvider() {

			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				int idx = getVirtualContentModel().indexOfElement(element);
				if (idx >= 0 )
				{	
					TableItem item = getTable().getItem(idx);
					return item.getText(columnIndex);
				}
				return ""; //$NON-NLS-1$
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {
			}};
	}
	
	public void formatViewer()
	{
		if (getModel() == null || !hasPendingUpdates())
			doFormatViewer();
		else 
			// do not format in the middle of an update
			// set pending update and will format when update is completed
			fPendingFormatViewer = true;
	}

	/**
	 * 
	 */
	private void doFormatViewer() {
		fPendingFormatViewer = false;
		preservingSelection(new Runnable() {

			public void run() {
				// causes the content of the table viewer to be replaced
				// without asking content adapter for content
				AbstractVirtualContentTableModel model = getVirtualContentModel();
				if (model != null)
				{
					model.handleViewerChanged();
				}
			}});
	}
	
	private void fireSelectionChanged(Object selectionKey)
	{
		if (selectionKey != null)
		{
			SelectionChangedEvent evt = new SelectionChangedEvent(this, new StructuredSelection(selectionKey));
			fireSelectionChanged(evt);
		}
	}

	public void handlePresentationFailure(IStatusMonitor monitor, IStatus status) {
		super.handlePresentationFailure(monitor, status);
	}
	
	public void refresh(boolean getContent)
	{
		if (getContent)
			refresh();
		else
		{
			preservingSelection(new Runnable() {

				public void run() {
					AbstractVirtualContentTableModel model = getVirtualContentModel();
					if (model != null)
					{
						Object[] elements = model.getElements();
						model.remove(elements);
						model.add(elements);
					}
				}});
		}
	}
	
	protected void tableTopIndexSetComplete() {
		
		if (!fTableCursor.isDisposed())
		{
			// TODO:  work around swt bug, must force a table cursor redraw after top index is changed
			// BUG 130130
			int[] coordinates = getCoordinatesFromKey(getSelectionKey());
			if (coordinates.length > 0)
				fTableCursor.setVisible(true);
			else
				fTableCursor.setVisible(false);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.AsynchronousViewer#getModel()
	 */
	public AsynchronousModel getModel() {
		return super.getModel();
	}
	
	// TODO:  need pluggable model to be truly flexible
	protected AbstractVirtualContentTableModel createVirtualContentTableModel() {
		return new TableRenderingModel(this);
	}

	protected void updateComplete(IStatusMonitor monitor) {
		super.updateComplete(monitor);
		
		if (!hasPendingUpdates() && !fTableCursor.isDisposed())
		{
			attemptSetKeySelection();
			fTableCursor.redraw();
			
			// if the viewer has pending top index, then more updates will come in
			// and the cursor should not be redrawn yet.
			if (!hasPendingSetTopIndex())
			{
				preservingSelection(new Runnable() {

					public void run() {

						int[] coordinates = getCoordinatesFromKey(getSelectionKey());
						if (coordinates.length > 0)
							fTableCursor.setVisible(true);
						else
							fTableCursor.setVisible(false);
					}});
			}
		}
		
		if (!hasPendingUpdates() && fPendingFormatViewer)
		{
			formatViewer();
			resizeColumnsToPreferredSize();
		}
	}

	protected void clear(Widget item) {
		super.clear(item);
		
		// this table viewer assumes that #getData will return null
		// set data to null when clearing an item.
		// only visible item will get SET DATA event again and at that time
		// the viewer would set the data back.
		if (item instanceof TableItem)
		{
			item.setData(null);
		}
	}
}
