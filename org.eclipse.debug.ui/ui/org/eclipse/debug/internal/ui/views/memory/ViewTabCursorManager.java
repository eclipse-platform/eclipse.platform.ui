/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory;

import java.math.BigInteger;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;


/**
 * Manages the cursor in the table.
 * Since table content is dynamic, default TableCursor does not handle dynamic
 * content so well.  This manager is created to make sure selection is maintainted properly.
 * In the future, it should be moidified such that it allows user to do multiple
 * cell selection.  (use fTrailCursors)
 * 
 * @since 3.0
 */
public class ViewTabCursorManager
{
	private TableCursor fTableCursor;
	private MemoryViewTab fViewTab;
	private MenuManager fMenuManager;
	protected int fRow;
	protected int fCol;
	private TableEditor editor;	
	private boolean fMenuDisposed = false;
	
	private Table fTable;
	private TableViewer fTableViewer;
	private String fRenderingId;
	private IFixedLengthOutputRenderer fRenderer;
	
	private TraverseEventListener fTraverseEvtListener;
	private TextFocusListener fTextFocusListener;
	private TextKeyListener fTextKeyListener;
	private MouseEventListener fMouseEventListener;
	
	class MouseEventListener extends MouseAdapter
	{
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDown(MouseEvent e)
		{	
			if (e.getSource() instanceof Table)
				handleTableMouseEvent(e);
			else if (e.getSource() instanceof TableCursor)
				handleCursorMouseEvent(e);
		}
		
		private void handleCursorMouseEvent(MouseEvent e){
			if (e.button == 1)
			{
				if (fCol > 0 && fCol <= (getNumCol()))
					activateCellEditor(null);
			}			
		}

		private void handleTableMouseEvent(MouseEvent e) {
			// figure out new cursor position based on here the mouse is pointing
			TableItem[] selections = fTableViewer.getTable().getSelection();
			TableItem selectedRow = null;
			int colNum = -1;
			
			if (selections.length > 0)
			{
				selectedRow = selections[0];
				
				int numCol = fTableViewer.getColumnProperties().length;
				
				for (int i=0; i<numCol; i++)
				{
					Rectangle bound = selectedRow.getBounds(i);
					if (bound.contains(e.x, e.y))
					{
						colNum = i;
						break;
					}
				}
			}
			
			// if column position cannot be determined, return
			if (colNum < 1)
				return;
			
			
			// handle user mouse click onto table
			// move cursor to new position
			if (selectedRow != null)
			{
				int row = fTableViewer.getTable().indexOf(selectedRow);
				
				updateCursorPosition(row, colNum, true);
			}			
			
			fViewTab.updateSyncTopAddress(true);
			// selected address changes because user has selected a new position
			fViewTab.updateSelectedAddress(fTableCursor.getRow(), fTableCursor.getColumn());

			// keep table selection up to date
			fViewTab.updateTableSelection();			
			
			setCursorFocus();
		}
	}
	
	/**
	 * Text Traverse Listener for the edit text box in Memory View Tab
	 * Moves cursor around as user tabs in the view tab.
	 */
	class TraverseEventListener implements TraverseListener
	{
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.TraverseListener#keyTraversed(org.eclipse.swt.events.TraverseEvent)
		 */
		public void keyTraversed(TraverseEvent event)
		{
			if (event.getSource() instanceof Text)
				handleTextTraverseEvt(event);
			else if (event.getSource() instanceof TableCursor)
				handleCursorTraverseEvt(event);
		}

		/**
		 * @param event
		 */
		private void handleTextTraverseEvt(TraverseEvent event) {
			final TraverseEvent e = event;
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					try
					{
						// if cursor is at boundry, reload
						if (fViewTab.getMemoryBlock() instanceof IExtendedMemoryBlock)
						{
							if (fRow+1 >= fTable.getItemCount() || fRow-1 <= 0)
							{
								BigInteger topAddress = fViewTab.getTopVisibleAddress();
								fViewTab.reloadTable(topAddress, false);
							}
						}
					}
					catch (DebugException e1)
					{
						DebugUIPlugin.log(e1);
						return;
					}					
					
					if (e.detail == SWT.TRAVERSE_TAB_NEXT)
					{
						if (!(fViewTab.getMemoryBlock() instanceof IExtendedMemoryBlock))
						{
							if (fRow+1 >= fTable.getItemCount() &&
								fCol == getNumCol())
							{
								return;
							}
						}
						
						// if the cursor is at the end of the line, move to next line
						if (fCol == getNumCol())
						{
							fCol = 1;
							fRow++;
						}
						else
						{
							fCol++;
						}
					}
					else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS)
					{
						if (!(fViewTab.getMemoryBlock() instanceof IExtendedMemoryBlock))
						{
							if (fRow-1 < 0 && fCol == 1)
							{
								return;
							}
						}
												
						// if the cursor is at the beginning  of the line, move to previous line
						if (fCol == 1)
						{
							fCol = getNumCol();
							fRow--;
						}
						else
						{
							fCol--;
						}
					}
					else
						return;
						
					// update cursor location and selection in table	
					updateCursorPosition(fRow, fCol, true);
					
					fViewTab.updateSyncTopAddress(true);
					fViewTab.updateSelectedAddress(fTableCursor.getRow(), fTableCursor.getColumn());
					fViewTab.updateTableSelection();	
					
					Text text = (Text)e.getSource();
					removeListeners(text);

					activateCellEditor(null);
				}


			});
		}
		
		private void handleCursorTraverseEvt(TraverseEvent e){
			if (fCol == getNumCol() && e.keyCode == SWT.ARROW_RIGHT)
			{
				if (fRow + 1>= fTable.getItemCount())
				{
					return;
				}
				
				fRow = fRow +1;
				fCol = 0;
				
				updateCursorPosition(fRow, fCol, true);
			}
			if (fCol == 1 && e.keyCode == SWT.ARROW_LEFT)
			{
				if (fRow-1 < 0)
				{
					return;
				}
				
				fRow = fRow - 1;
				fCol = getNumCol()+1;
				
				updateCursorPosition(fRow, fCol, true);
			}			
		}
	}
		
	/**
	 * Focus listener for the text editor box in Memory View Tab
	 * Commit changes to IMemoryBlock when the text box has lost focus.
	 */
	class TextFocusListener implements FocusListener
	{

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
		 */
		public void focusGained(FocusEvent e)
		{	
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
		 */
		public void focusLost(FocusEvent event)
		{
			
			final FocusEvent e = event;

			Display.getDefault().syncExec(new Runnable() {

				public void run()
				{
					try
					{
						Text text = (Text)e.getSource();
						removeListeners(text);
	
						// get new value
						String newValue = text.getText();
						
						// modify memory at fRow and fCol
						modifyValue(fRow, fCol, newValue);
								
						// show cursor after modification is completed
						showCursor();
					}
					catch (NumberFormatException e1)
					{
						MemoryViewUtil.openError(DebugUIMessages.getString(MemoryViewCellModifier.TITLE), 
							DebugUIMessages.getString(MemoryViewCellModifier.DATA_IS_INVALID), null);
					}		
				}
			});
		}
	}
	
	/**
	 * Key Listener to the edit text box in Memory View Tab
	 * Moves the text box arround as user hits the arrow keys.
	 * Change is not committed as user hits the Escape key.
	 * Move edit box to the next cell as user has typed in the max
	 * number of characters for a cell.
	 */
	class TextKeyListener extends KeyAdapter
	{
		/* (non-Javadoc)
		 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
		 */
		public void keyPressed(KeyEvent event)
		{
			if (event.getSource() instanceof Text)
				handleTextKeyEvt(event);			
		}
		
		/**
		 * @param event
		 */
		private void handleTextKeyEvt(KeyEvent event) {
			final KeyEvent e = event;
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					Text text = (Text)e.getSource();
					
					try
					{
						switch (e.keyCode)
						{
							case SWT.ARROW_UP :
								
								// move text editor box up one row
							
								if (fRow-1 < 0)
									return;
							
								// modify value for current cell
								modifyValue(fRow, fCol, text.getText());
														
								fRow--;

								//	update cursor location and selection in table	
								updateCursorPosition(fRow, fCol, true);
								
								fViewTab.updateSyncTopAddress(true);
								fViewTab.updateSelectedAddress(fTableCursor.getRow(), fTableCursor.getColumn());
								fViewTab.updateTableSelection();
						
								// remove listeners when focus is lost
								removeListeners(text);
								activateCellEditor(null);
								break;
							case SWT.ARROW_DOWN :
								
								// move text editor box down one row
								
								if (fRow+1 >= fTable.getItemCount())
									return;
								
								// modify value for current cell
								modifyValue(fRow, fCol, text.getText());
							
								fRow++;
								
								//	update cursor location and selection in table								
								updateCursorPosition(fRow, fCol, true);
								
								fViewTab.updateSyncTopAddress(true);
								fViewTab.updateSelectedAddress(fTableCursor.getRow(), fTableCursor.getColumn());
								fViewTab.updateTableSelection();
						
								// remove traverse listener when focus is lost
								removeListeners(text);
								activateCellEditor(null);		
								break;
							case 0:
								
								// if user has entered the max number of characters allowed in a cell, move to next cell
								// Extra changes will be used as initial value for the next cell
								
								if (fRenderer != null)
								{								
									if (text.getText().length() > fViewTab.getColumnSize()*fRenderer.getNumCharPerByte())
									{
										String newValue = text.getText();
										text.setText(newValue.substring(0, fViewTab.getColumnSize()*fRenderer.getNumCharPerByte()));
										
										modifyValue(fRow, fCol, text.getText());
										
										// if cursor is at the end of a line, move to next line
										if (fCol >= getNumCol())
										{
											fCol = 1;
											fRow++;
										}
										else
										{
											// move to next column
											fCol++;
										}
										
										// update cursor position and selected address
										updateCursorPosition(fRow, fCol, true);
										
										fViewTab.updateSyncTopAddress(true);
										fViewTab.updateSelectedAddress(fTableCursor.getRow(), fTableCursor.getColumn());
										fViewTab.updateTableSelection();
										
										removeListeners(text);
							
										// activate text editor at next cell
										activateCellEditor(newValue.substring(fViewTab.getColumnSize()*fRenderer.getNumCharPerByte()));
									}
								}
								break;	
							case SWT.ESC:

								// if user has pressed escape, do not commit the changes
								// that's why "modifyValue" is not called
								updateCursorPosition(fRow, fCol, true);
								fViewTab.updateSelectedAddress(fTableCursor.getRow(), fTableCursor.getColumn());
								fViewTab.updateTableSelection();
						
								removeListeners(text);
								
								// cursor needs to have focus to remove focus from cell editor
								setCursorFocus();
								break;	
							default :
							if (fRenderer != null)
							{
								if (text.getText().length()> fViewTab.getColumnSize()* fRenderer.getNumCharPerByte())
								{
									String newValue = text.getText();
									text.setText(newValue.substring(0,fViewTab.getColumnSize()* fRenderer.getNumCharPerByte()));
									modifyValue(fRow, fCol, text.getText());
									// if cursor is at the end of a line, move to next line
									if (fCol >= getNumCol())
									{
										fCol = 1;
										fRow++;
									}
									else
									{
										fCol++;
									}
									
									updateCursorPosition(fRow, fCol, true);
									
									fViewTab.updateSyncTopAddress(true);
									fViewTab.updateSelectedAddress(fTableCursor.getRow(), fTableCursor.getColumn());
									fViewTab.updateTableSelection();
									removeListeners(text);
									activateCellEditor(
										newValue.substring(
											fViewTab.getColumnSize()
												* fRenderer.getNumCharPerByte()));
								}
							}
							break;
						}
					}
					catch (NumberFormatException e1)
					{
						MemoryViewUtil.openError(DebugUIMessages.getString(MemoryViewCellModifier.TITLE), 
							DebugUIMessages.getString(MemoryViewCellModifier.DATA_IS_INVALID), null);
						
						updateCursorPosition(fRow, fCol, true);
						fViewTab.updateSelectedAddress(fTableCursor.getRow(), fTableCursor.getColumn());
						fViewTab.updateTableSelection();
				
						removeListeners(text);
						showCursor();
					}
				}
			});
		}
	}
	
	/**
	 * Create cursor manager for view tab
	 * @param viewTab
	 * @param initialRow
	 * @param initialCol
	 * @param menuManager
	 */
	public ViewTabCursorManager(MemoryViewTab viewTab, int initialRow, int initialCol, MenuManager menuManager)
	{
		fViewTab = viewTab;
		fMenuManager = menuManager;
		fRow = initialRow;
		fCol = initialCol;
		fTableViewer = viewTab.getTableViewer();
		fTable = fTableViewer.getTable();
		editor = new TableEditor (fTable);
		fRenderingId = fViewTab.getRenderingId();
		
		IBaseLabelProvider labelProvider = fTableViewer.getLabelProvider();
		if (labelProvider instanceof AbstractTableViewTabLabelProvider)
		{
			AbstractMemoryRenderer renderer = ((AbstractTableViewTabLabelProvider)labelProvider).getRenderer();
			if (renderer instanceof IFixedLengthOutputRenderer)
				fRenderer = (IFixedLengthOutputRenderer)renderer;
		}
		
		fTraverseEvtListener = new TraverseEventListener();
		fTextFocusListener = new TextFocusListener();
		fTextKeyListener = new TextKeyListener();

		fMouseEventListener = new MouseEventListener();
		fTable.addMouseListener(fMouseEventListener);
		
		createCursor();
	}
	
	private void createCursor()
	{
		fTableCursor = new TableCursor(fTable, SWT.NONE);
		
		Display display = fTableCursor.getDisplay();
		
		// set up cursor color
		fTableCursor.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
		fTableCursor.setForeground(display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));

		updateCursorPosition(fRow, fCol, true);
		
		fTableCursor.setFont(JFaceResources.getFont(IInternalDebugUIConstants.FONT_NAME));
		fTableCursor.setVisible(true);
		fTableCursor.setFocus();
	
		fTableCursor.addSelectionListener(new SelectionListener() {

			// for keeping track of cursor positions
			public void widgetSelected(SelectionEvent e)
			{
				if (fTableCursor != null)
				{
					fViewTab.updateSyncTopAddress(true);
					
					fRow = fTable.indexOf(fTableCursor.getRow());
					fCol = fTableCursor.getColumn();
					
					// cursor position has changed, update selected address
					fViewTab.updateSelectedAddress(fTableCursor.getRow(), fTableCursor.getColumn());
					fViewTab.updateTableSelection();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e)
			{
			
			}});	
			
		fTableCursor.addTraverseListener(fTraverseEvtListener);
			
		// set up context menu for the cursor
		// otherwise, cell will not show context menu when it is highlighted	
		if (fMenuManager != null)
		{
			createContextMenu();
		}	
		
		// TODO:  Moved this to viewTabCursorManager?
		// need to revisit this
		fTableCursor.addKeyListener(fViewTab);	
		fTableCursor.addMouseListener(fMouseEventListener);
	}
	

	/**
	 * Hook up context menu with the cursor.
	 * This is needed when user RMC at cursor.  Without this the context
	 * menu activation will not be detected by the cursor
	 * @return 
	 */
	private Menu createContextMenu()
	{
		fMenuDisposed = false;
		
		Menu menu = fMenuManager.createContextMenu(fTable);
		
		fTable.setMenu(menu);
		
		if (fTableCursor != null)
		{
			Control menuControl = fTableCursor;
			menuControl.setMenu(menu);
			
			menu.addDisposeListener(new DisposeListener(){

				public void widgetDisposed(DisposeEvent e)
				{
					fMenuDisposed = true;	
					
					Menu disposedMenu = (Menu)e.getSource();
					disposedMenu.removeDisposeListener(this);			
				}});
		}
		
		return menu;
	}
	
	/**
	 * Redraw cursors
	 */
	public void redrawCursors()
	{
		if (fTableCursor != null && fTableCursor.isVisible())
		{
			fTableCursor.setSelection(fRow, fCol);
			fTableCursor.redraw();
		}
	}
	
	/**
	 * Clean up cursor manager
	 */
	public void dispose()
	{
		if (fTableCursor != null)
		{
			// TableCursor changes the way how it is disposed.
			// SWT disposes the cursor when the view tab is dispose
			// disposing the cursor now causes SWT error
//			fTableCursor.dispose();
			fTableCursor = null;
		}
			
		if (editor != null)
		{
			editor.dispose();
			editor = null;	
		}
		
		if (fTable != null)
			fTable.removeMouseListener(fMouseEventListener);
	}
	
	/**
	 * @return lead cursor
	 */
	public TableCursor getLeadCursor()
	{
		return fTableCursor;
	}
	
	/**
	 * Hide cursor
	 */
	public void hideCursor()
	{	
		fTableCursor.setVisible(false);
	}
	
	/**
	 * When cursor is to be shown again, if the cursor is previously dispose,
	 * create a new cursor and position at last remembered position
	 */
	public void showCursor()
	{
		if (fTableCursor == null)
		{
			createCursor();
		}
		if (fMenuDisposed)
		{
			createContextMenu();			
		}
		
		if (!fTableCursor.isVisible())
		{	
			fTableCursor.setVisible(true);
		}
	}
	
	/**
	 * Causes the cursor to gain focus
	 */
	public void setCursorFocus()
	{
		if (fTableCursor != null)
			fTableCursor.setFocus();
	}
	
	
	/**
	 * Set new row/col position in cursor.  If cursor is not visible, (cursor is null),
	 * call showCursor to create new cursor.
	 * @param row
	 * @param col
	 */
	public void updateCursorPosition(int row, int col, boolean showCursor)
	{
		if (row < 0
			|| row >= fTable.getItemCount()
			|| col < 0
			|| col >= fTable.getColumnCount()) {
			return;
		}

		this.fRow = row;
		this.fCol = col;

		fTableCursor.setSelection(row, col);

		if (showCursor)
			showCursor();
	}
	
	/**
	 * Activate celll editor and prefill it with initial value.
	 * If initialValue is null, use cell content as initial value
	 * @param initialValue
	 */
	public void activateCellEditor(String initialValue) {
		
		// do not allow user to edit address column
		if (fCol == 0 || fCol > getNumCol())
		{
			return;
		}
		
		ICellModifier cellModifier = null;
		
		if (fTableViewer == null)
		{
			return;
		}
		cellModifier = fTableViewer.getCellModifier();
		
		TableItem tableItem = fTable.getItem(fRow);
		
		Object element = tableItem.getData();
		Object property = fTableViewer.getColumnProperties()[fCol];
		Object value = cellModifier.getValue(element, (String)property);
		
		// The cell modifier canModify function always returns false if the edit action 
		// is not invoked from here.  This is to prevent data to be modified when
		// the table cursor loses focus from a cell.  By default, data will
		// be changed in a table when the cell loses focus.  This is to workaround
		// this default behaviour and only change data when the cell editor
		// is activated.
		((MemoryViewCellModifier)cellModifier).setEditActionInvoked(true);
		boolean canEdit = cellModifier.canModify(element, (String)property);
		((MemoryViewCellModifier)cellModifier).setEditActionInvoked(false);
		
		if (!canEdit)
			return;
		
		// activate based on current cursor position
		TextCellEditor selectedEditor = (TextCellEditor)fTableViewer.getCellEditors()[fCol];

		
		if (fTableViewer != null && cellModifier != null && selectedEditor != null && tableItem != null)
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
			
			if (fRenderingId.equals(IMemoryViewConstants.RENDERING_RAW_MEMORY))
				cellValue = cellValue.toUpperCase();
			
			text.setText(cellValue);
	
			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
	
			// Open the text editor in selected column of the selected row.
			editor.setEditor (text, tableItem, fCol);
	
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
	
	/**
	 * @return
	 */
	private int getNumCol() {
		
		int bytesPerLine = fViewTab.getBytesPerLine();
		int columnSize = fViewTab.getColumnSize();
		
		return bytesPerLine/columnSize;
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
		
		TableItem tableItem = fTable.getItem(row);

		Object property = fTableViewer.getColumnProperties()[col];

		// get old value
		Object element = tableItem.getData();
		
		ICellModifier cellModifier = fTableViewer.getCellModifier();
		String oldValue = (String)cellModifier.getValue(element, (String)property);		

		if (fRenderingId.equals(IMemoryViewConstants.RENDERING_RAW_MEMORY))
		{
			oldValue = oldValue.toUpperCase();
			newValue = newValue.toUpperCase();

			// only compare up to the length of new value, leave the rest of the memory unchanged
			if (newValue.length() <= oldValue.length())
			{
				oldValue = oldValue.substring(0, newValue.length());
			}
	
			if (!oldValue.equals(newValue))
			{
				fTableViewer.getCellModifier().modify(tableItem, (String)property, newValue);
			}
		}
		else
		{
			// just modify
			fTableViewer.getCellModifier().modify(tableItem, (String)property, newValue);
		}
	}
	
	/**
	 * @param font
	 */
	protected void setFont(Font font)
	{
		if (fTableCursor != null)
		{
			fTableCursor.setFont(font);
		}
	}
	
	/**
	 * @param text
	 */
	private void addListeners(Text text) {
		// add listeners to text 
		text.addFocusListener(fTextFocusListener);						
		text.addTraverseListener(fTraverseEvtListener);
		text.addKeyListener(fTextKeyListener);
		text.addKeyListener(fViewTab);
	}
	
	/**
	 * @param text
	 */
	private void removeListeners(Text text) {
		// remove traverse listener when focus is lost
		text.removeTraverseListener(fTraverseEvtListener);
		text.removeFocusListener(fTextFocusListener);
		text.removeKeyListener(fTextKeyListener);
		text.removeKeyListener(fViewTab);
	}
}
