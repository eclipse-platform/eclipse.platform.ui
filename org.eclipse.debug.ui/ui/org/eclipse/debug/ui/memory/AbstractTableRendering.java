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

package org.eclipse.debug.ui.memory;

import java.math.BigInteger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.internal.ui.views.memory.renderings.CopyTableRenderingToClipboardAction;
import org.eclipse.debug.internal.ui.views.memory.renderings.FormatColumnAction;
import org.eclipse.debug.internal.ui.views.memory.renderings.GoToAddressAction;
import org.eclipse.debug.internal.ui.views.memory.renderings.PrintTableRenderingAction;
import org.eclipse.debug.internal.ui.views.memory.renderings.ReformatAction;
import org.eclipse.debug.internal.ui.views.memory.renderings.ResetToBaseAddressAction;
import org.eclipse.debug.internal.ui.views.memory.renderings.SetColumnSizeDefaultAction;
import org.eclipse.debug.internal.ui.views.memory.renderings.TableRenderingCellModifier;
import org.eclipse.debug.internal.ui.views.memory.renderings.TableRenderingContentInput;
import org.eclipse.debug.internal.ui.views.memory.renderings.TableRenderingContentProvider;
import org.eclipse.debug.internal.ui.views.memory.renderings.TableRenderingLabelProvider;
import org.eclipse.debug.internal.ui.views.memory.renderings.TableRenderingLine;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;

/**
 * This is an abstract implementation to a table rendering.  Clients should
 * subclass from this class if they wish to provide a table rendering.
 * 
 * @since 3.1
 */
public abstract class AbstractTableRendering extends AbstractMemoryRendering implements IPropertyChangeListener{	

	// Property Ids for the selected address in a table rendering
	public static final String PROPERTY_SELECTED_ADDRESS = "selectedAddress"; //$NON-NLS-1$
	// Property Ids for the column size in a table rendering
	public static final String PROPERTY_COL_SIZE = "columnSize"; //$NON-NLS-1$
	// Property Ids for the top row address in a table rendering
	public static final String PROPERTY_TOP_ADDRESS = "topAddress"; //$NON-NLS-1$
	
	private PageBook fPageBook;
	private TableViewer fTableViewer;
	private TextViewer fTextViewer;
	
	private int fBytePerLine;								// number of bytes per line: 16
	private int fColumnSize;								// number of bytes per column:  1,2,4,8
	private int fAddressableSize;	
	
	private boolean fIsShowingErrorPage;
	
	private TableRenderingContentProvider fContentProvider;
	private BigInteger fSelectedAddress;
	private TableRenderingContentInput fContentInput;
	private TableRenderingCellModifier fCellModifier;
	private boolean fIsCreated;
	private CellEditor[] fEditors;
	private String fLabel;
	private TableCursor fTableCursor;
	private boolean fIsDisposed;
	private TraverseListener fCursorTraverseListener;
	private KeyAdapter fCursorKeyAdapter;
	private BigInteger fTopRowAddress;
	
	private CopyTableRenderingToClipboardAction fCopyToClipboardAction;
	private GoToAddressAction fGoToAddressAction;
	private ResetToBaseAddressAction fResetMemoryBlockAction;
	private PrintTableRenderingAction fPrintViewTabAction;
	private Action[] fFormatColumnActions;
	private ReformatAction fReformatAction;
	private ToggleAddressColumnAction fToggleAddressColumnAction;
	private EventHandleLock fEvtHandleLock = new EventHandleLock();
	private TableEditor fCursorEditor;
	private FocusAdapter fEditorFocusListener;
	private MouseAdapter fCursorMouseListener;
	private KeyAdapter fEditorKeyListener;
	private SelectionAdapter fCursorSelectionListener;
	
	private boolean fIsShowAddressColumn = true;
	private SelectionAdapter fScrollbarSelectionListener;
	
	private class EventHandleLock
	{
		Object fOwner;
		
		public boolean acquireLock(Object client)
		{
			if (fOwner == null)
			{
				fOwner = client;
				return true;
			}
			return false;
		}
		
		public boolean releaseLock(Object client)
		{
			if (fOwner == client)
			{
				fOwner = null;
				return true;
			}
			return false;
		}
		
		public boolean isLocked()
		{
			return (fOwner != null);
		}
	}	
	
	
	private class ToggleAddressColumnAction extends Action {

		public ToggleAddressColumnAction() {
			super();
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugUIConstants.PLUGIN_ID
					+ ".ShowAddressColumnAction_context"); //$NON-NLS-1$
			updateActionLabel();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			fIsShowAddressColumn = !fIsShowAddressColumn;
			resizeColumnsToPreferredSize();
			updateActionLabel();
		}

		/**
		 * 
		 */
		private void updateActionLabel() {
			if (fIsShowAddressColumn) {
				setText(DebugUIMessages.getString("ShowAddressColumnAction.0")); //$NON-NLS-1$
			} else {
				setText(DebugUIMessages.getString("ShowAddressColumnAction.1")); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * @param renderingId
	 */
	public AbstractTableRendering(String renderingId) {
		super(renderingId);
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		// if memory view table font has changed
		if (event.getProperty().equals(IInternalDebugUIConstants.FONT_NAME))
		{
			if (!fIsDisposed)
			{			
				Font memoryViewFont = JFaceResources.getFont(IInternalDebugUIConstants.FONT_NAME);
				setFont(memoryViewFont);		
			}
			return;
		}
		
		if (event.getProperty().equals(IDebugPreferenceConstants.PREF_PADDED_STR))
		{
			if (!fIsDisposed)
			{
				fTableViewer.refresh();
				fTableCursor.redraw();
			}
			return;
		}
		
		Object evtSrc = event.getSource();
		
		// do not handle event if the rendering is displaying an error
		if (isDisplayingError())
			return;
		
		// do not handle property change event if the rendering is not visible
		if (!isVisible())
			return;
		
		if (evtSrc == this)
			return;
		
		if (!(evtSrc instanceof IMemoryRendering))
			return;
		
		IMemoryRendering rendering = (IMemoryRendering)evtSrc;
		IMemoryBlock memoryBlock = rendering.getMemoryBlock();
		
		// do not handle event from renderings displaying other memory blocks
		if (memoryBlock != getMemoryBlock())
			return;
	
		String propertyName = event.getProperty();
		Object value = event.getNewValue();
		
		if (propertyName.equals(AbstractTableRendering.PROPERTY_SELECTED_ADDRESS) && value instanceof BigInteger)
		{
			selectedAddressChanged((BigInteger)value);
		}
		else if (propertyName.equals(AbstractTableRendering.PROPERTY_COL_SIZE) && value instanceof Integer)
		{
			columnSizeChanged(((Integer)value).intValue());
		}
		else if (propertyName.equals(AbstractTableRendering.PROPERTY_TOP_ADDRESS) && value instanceof BigInteger)
		{
			if (needMoreLines())
			{
				reloadTable(getTopVisibleAddress(), false);
			}
			topVisibleAddressChanged((BigInteger)value);
		}
	}

	/**
	 * Handle top visible address change event from synchronizer
	 * @param address
	 */
	private void topVisibleAddressChanged(final BigInteger address)
	{
		// do not handle event if view tab is disabled
		if (!isVisible())
			return;
	
		if (!address.equals(fTopRowAddress))
		{
			fTopRowAddress = address;
			updateSyncTopAddress();
			if (getMemoryBlock() instanceof IMemoryBlockExtension)
			{
			
				handleTopAddressChangedforExtended(address);
			}
			else
			{
				handleTopAddressChangedForSimple(address);
			}
		}
	}

	/**
	 * @param address
	 */
	private void handleTopAddressChangedForSimple(final BigInteger address) {
		// IMemoryBlock support
		int index = findAddressIndex(address);
		Table table = fTableViewer.getTable();
		if (index >= 0)
		{
			setTopIndex(table,  index);
		}
		
		if (isAddressVisible(fSelectedAddress))
			fTableCursor.setVisible(true);
		else
			fTableCursor.setVisible(false);
		
	}

	/**
	 * @param address
	 */
	private void handleTopAddressChangedforExtended(final BigInteger address) {
		
		Object evtLockClient = new Object();
		try 
		{
		if (!fEvtHandleLock.acquireLock(evtLockClient))
			return;
		
		if (!isAddressOutOfRange(address))
		{
			Table table = fTableViewer.getTable();
			int index = findAddressIndex(address);
			if (index >= 3 && table.getItemCount() - (index+getNumberOfVisibleLines()) >= 3)
			{
				// update cursor position
				setTopIndex(table, index);
			}
			else
			{
				int numInBuffer = table.getItemCount();
				if (index < 3)
				{
					if(isAtTopLimit())
					{
						setTopIndex(table, index);
					}
					else
					{
						reloadTable(address, false);
					}
				}
				else if ((numInBuffer-(index+getNumberOfVisibleLines())) < 3)
				{
					if (!isAtBottomLimit())
						reloadTable(address, false);
					else
						setTopIndex(table, index);
				}
			}
		}
		else
		{	
			// approaching limit, reload table
			reloadTable(address, false);
		}
		
		if (isAddressVisible(fSelectedAddress))
			fTableCursor.setVisible(true);
		else
			fTableCursor.setVisible(false);
		}
		finally
		{
			fEvtHandleLock.releaseLock(evtLockClient);
		}
	}	
	
	/**
	 * @param value
	 */
	private void selectedAddressChanged(BigInteger value) {
		
		try {
			goToAddress(value);
		} catch (DebugException e) {
			DebugUIPlugin.log(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRendering#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		
		fPageBook = new PageBook(parent, SWT.NONE);
		createErrorPage(fPageBook);
		createTableViewer(fPageBook);
		
		fTableViewer.getTable().redraw();
		
		return fPageBook;
	}

	/**
	 * Create the table viewer and other support controls
	 * for this rendering
	 * @param parent
	 */
	private void createTableViewer(Composite parent) {
		
		fTableViewer= new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.HIDE_SELECTION | SWT.BORDER);
		
		TableRenderingLabelProvider labelProvider = new TableRenderingLabelProvider(this);
		fTableViewer.setLabelProvider(labelProvider);
		
		fContentProvider = new TableRenderingContentProvider();
		fTableViewer.setContentProvider(fContentProvider);		
		fContentProvider.setViewer(fTableViewer);
		
		ScrollBar scroll = ((Table)fTableViewer.getControl()).getVerticalBar();
		scroll.setMinimum(-100);
		scroll.setMaximum(200);		

		fTableViewer.getTable().setHeaderVisible(true);
		fTableViewer.getTable().setLinesVisible(true);
		
// FORMAT RENDERING
		// set up addressable size and figure out number of bytes required per line
		fAddressableSize = -1;
		if (getMemoryBlock() instanceof IMemoryBlockExtension)
			fAddressableSize = ((IMemoryBlockExtension)getMemoryBlock()).getAddressableSize();
		if (fAddressableSize < 1)
			fAddressableSize = 1;
		int bytePerLine = IInternalDebugUIConstants.ADD_UNIT_PER_LINE * fAddressableSize;
		
		// get default column size from preference store
		IPreferenceStore prefStore = DebugUIPlugin.getDefault().getPreferenceStore();
		// column size is now stored as number of addressable units
		int columnSize = prefStore.getInt(IDebugPreferenceConstants.PREF_COLUMN_SIZE);
		// actual column size is number of addressable units * size of the addressable unit
		columnSize = columnSize * getAddressableSize();
		
		// check synchronized col size
		Integer colSize = (Integer)getSynchronizedProperty(AbstractTableRendering.PROPERTY_COL_SIZE);
		
		if (colSize != null)
		{
			int syncColSize = colSize.intValue(); 
			if (syncColSize > 0)
			{
				columnSize = syncColSize;
			}	
		}
		
		// format memory block with specified "bytesPerLine" and "columnSize"	
		boolean ok = format(bytePerLine, columnSize);

		if (!ok)
		{
			DebugException e = new DebugException(DebugUIPlugin.newErrorStatus(DebugUIMessages.getString("AbstractTableRendering.0"), null)); //$NON-NLS-1$
			displayError(e);
			return;
		}
// END FORMAT RENDERING		
		
		// figure out selected address 
		BigInteger selectedAddress = (BigInteger) getSynchronizedProperty(AbstractTableRendering.PROPERTY_SELECTED_ADDRESS);
		if (selectedAddress == null)
		{
			if (getMemoryBlock() instanceof IMemoryBlockExtension) {
				selectedAddress = ((IMemoryBlockExtension) getMemoryBlock())
						.getBigBaseAddress();
	
				if (selectedAddress == null) {
					selectedAddress = new BigInteger("0"); //$NON-NLS-1$
				}
	
			} else {
				long address = getMemoryBlock().getStartAddress();
				selectedAddress = BigInteger.valueOf(address);
			}
		}
		fSelectedAddress = selectedAddress;
		
		// figure out top visible address
		BigInteger topVisibleAddress = (BigInteger) getSynchronizedProperty(AbstractTableRendering.PROPERTY_TOP_ADDRESS);
		if (topVisibleAddress == null)
		{
			if (getMemoryBlock() instanceof IMemoryBlockExtension)
			{
				topVisibleAddress = ((IMemoryBlockExtension)getMemoryBlock()).getBigBaseAddress();
			}
			else
			{
				topVisibleAddress = BigInteger.valueOf(getMemoryBlock().getStartAddress());
			}
		}
		
		fContentInput = new TableRenderingContentInput(this, 20, 20, 20, topVisibleAddress, getNumberOfVisibleLines(), false);
		fTableViewer.setInput(fContentInput);
		
		fCellModifier = new TableRenderingCellModifier(this);
		fTableViewer.setCellModifier(fCellModifier);
		
				
// SET UP FONT		
		// set to a non-proportional font
		fTableViewer.getTable().setFont(JFaceResources.getFont(IInternalDebugUIConstants.FONT_NAME));
		
		if (!(getMemoryBlock() instanceof IMemoryBlockExtension))
		{		
			// If not extended memory block, do not create any buffer
			// no scrolling
			fContentInput.setPreBuffer(0);
			fContentInput.setPostBuffer(0);
			fContentInput.setDefaultBufferSize(0);
		}
		
		createCursor(fTableViewer.getTable(), fSelectedAddress);
		
		fTableViewer.getTable().addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				handleTableMouseEvent(e);
			}});
		
		// create pop up menu for the rendering
		createActions();
		createPopupMenu(fTableViewer.getControl());
		createPopupMenu(fTableCursor);

		getPopupMenuManager().addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			}});

		fIsCreated = true;

		addRenderingToSyncService();
		//synchronize
		synchronize();
		
		fTopRowAddress = getTopVisibleAddress();
		
		// Need to resize column after content is filled in
		// Pack function does not work unless content is not filled in
		// since the table is not able to compute the preferred size.
		resizeColumnsToPreferredSize();
		
		if (getMemoryBlock() instanceof IMemoryBlockExtension)
		{
			if(((IMemoryBlockExtension)getMemoryBlock()).getBigBaseAddress() == null)
			{
				DebugException e = new DebugException(DebugUIPlugin.newErrorStatus(DebugUIMessages.getString("AbstractTableRendering.1"), null)); //$NON-NLS-1$
				displayError(e);				
			}
		}
		
		// add font change listener and update font when the font has been changed
		JFaceResources.getFontRegistry().addListener(this);
		
		
		fScrollbarSelectionListener = new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				handleScrollBarSelection();
				
			}};
		scroll.addSelectionListener(fScrollbarSelectionListener);
		
		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		
	}
	
	private void createCursor(Table table, BigInteger address)
	{
		fTableCursor = new TableCursor(table, SWT.NONE);
		Display display = fTableCursor.getDisplay();
		
		// set up cursor color
		fTableCursor.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
		fTableCursor.setForeground(display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
		
		fTableCursor.setFont(JFaceResources.getFont(IInternalDebugUIConstants.FONT_NAME));
		fTableCursor.setVisible(true);
		fTableCursor.setFocus();
		
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
						
						if (!fEvtHandleLock.acquireLock(this))
							return;
	
						handleCursorMoved();
						
						fEvtHandleLock.releaseLock(this);

					}
				};
		fTableCursor.addSelectionListener(fCursorSelectionListener);
		
		
		setCursorAtAddress(address);
		
		fCursorEditor = new TableEditor (fTableViewer.getTable());	
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
		
		Object evtLockClient = new Object();
		if (!fEvtHandleLock.acquireLock(evtLockClient))
			return;
		
		handleCursorMoved();
		
		fEvtHandleLock.releaseLock(evtLockClient);

	}
	
	/**
	 * Update selected address.
	 * Load more memory if required.
	 */
	private void handleCursorMoved()
	{	
		if (fIsDisposed)
			return;
		
		BigInteger selectedAddress = getSelectedAddressFromCursor(fTableCursor);
		
		// when the cursor is moved, the selected address is changed
		if (selectedAddress != null && !selectedAddress.equals(fSelectedAddress))
		{
			fSelectedAddress = selectedAddress;
			updateSyncSelectedAddress();
		}
		
		// now check to see if the cursor is approaching buffer limit
		TableItem item = fTableCursor.getRow();
		if (item == null)
			return;
		
		if (getMemoryBlock() instanceof IMemoryBlockExtension)
		{
			int row = fTableViewer.getTable().indexOf(item);
			
			if (row < 3)
			{
				
				if (!isAtTopLimit())
				{
					refresh();
					setCursorAtAddress(fSelectedAddress);
				}
			}
			else if (row >= fTableViewer.getTable().getItemCount() - 3)
			{
				if (!isAtBottomLimit())
				{
					refresh();
					setCursorAtAddress(fSelectedAddress);
				}
	
			}
		}
		
		// if the cursor has moved, the top index of the table may change
		// just update the synchronization service
		BigInteger address = getTopVisibleAddress();
		if (!address.equals(fTopRowAddress))
		{
			fTopRowAddress = address;
			updateSyncTopAddress();
		}
	}
	
	private void handleCursorKeyPressed(KeyEvent event)
	{
		// allow edit if user hits return
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
	
	/**
	 * Calculate selected address based on cursor's current position
	 * @param cursor
	 * @return the selected address
	 */
	private BigInteger getSelectedAddressFromCursor(TableCursor cursor)
	{
		TableItem row = cursor.getRow();
		int col = cursor.getColumn();
		
		if (row == null)
			return null;
		
		// get row address
		String temp = ((TableRenderingLine)row.getData()).getAddress();
		BigInteger rowAddress = new BigInteger(temp, 16);
		
		int offset;
		if (col > 0)
		{	
			// 	get address offset
			int addressableUnit = getAddressableUnitPerColumn();
			offset = (col-1) * addressableUnit;
		}
		else
		{
			offset = 0;
		}
		
		return rowAddress.add(BigInteger.valueOf(offset));
	}
	
	
	/**
	 * Sets the cursor at the specified address
	 * @param address
	 * @return true if successful, false otherwise
	 */
	private boolean setCursorAtAddress(BigInteger address)
	{
		// selected address is out of range, simply return false
		if (address.compareTo(fContentProvider.getBufferTopAddress()) < 0)
			return false;
		
		// calculate selected row address
		int addressableUnit = getAddressableUnitPerLine();
		int numOfRows = address.subtract(fContentProvider.getBufferTopAddress()).intValue()/addressableUnit;
		BigInteger rowAddress = fContentProvider.getBufferTopAddress().add(BigInteger.valueOf(numOfRows * addressableUnit));

		// try to find the row of the selected address
		int row = findAddressIndex(address);
			
		if (row == -1)
		{
			return false;
		}
		
		// calculate offset to the row address
		BigInteger offset = address.subtract(rowAddress);
		
		// locate column
		int colAddressableUnit = getAddressableUnitPerColumn();
		int col = ((offset.intValue()/colAddressableUnit)+1);
		
		if (col == 0)
			col = 1;
		
		fTableCursor.setSelection(row, col);
		
		return true;		
	}
	
	
	/**
	 * Format view tab based on parameters.
	 * @param bytesPerLine - number of bytes per line, possible values: 16 * addressableSize
	 * @param columnSize - number of bytes per column, possible values: (1 / 2 / 4 / 8 / 16) * addressableSize
	 * @return true if format is successful, false, otherwise
	 */
	public boolean format(int bytesPerLine, int columnSize)
	{			
		// selected address gets changed as the cursor is moved
		// during the reformat.
		// Back up the address and restore it later.
		BigInteger selectedAddress = fSelectedAddress;
		
		// check parameter, limit number of addressable unit to 16
		if (bytesPerLine/fAddressableSize != IInternalDebugUIConstants.ADD_UNIT_PER_LINE)
		{
			return false;
		}
		
		// bytes per cell must be divisible to bytesPerLine
		if (bytesPerLine % columnSize != 0)
		{
			return false;
		}
		
		// do not format if the view tab is already in that format
		if(fBytePerLine == bytesPerLine && fColumnSize == columnSize){
			return false;
		}
		
		fBytePerLine = bytesPerLine;
		fColumnSize = columnSize;
		
		Object evtLockClient = new Object();
		if (!fEvtHandleLock.acquireLock(evtLockClient))
			return false;
		
		// if the tab is already created and is being reformated
		if (fIsCreated)
		{	
			if (fTableViewer == null)
				return false;
			
			if (fTableViewer.getTable() == null)
				return false;
			
			// clean up old columns
			TableColumn[] oldColumns = fTableViewer.getTable().getColumns();
			
			for (int i=0; i<oldColumns.length; i++)
			{
				oldColumns[i].dispose();
			}
			
			// clean up old cell editors
			CellEditor[] oldCellEditors = fTableViewer.getCellEditors();
			
			for (int i=0; i<oldCellEditors.length; i++)
			{
				oldCellEditors[i].dispose();
			}
		}
		
		TableColumn column0 = new TableColumn(fTableViewer.getTable(),SWT.LEFT,0);
		column0.setText(DebugUIMessages.getString("AbstractTableRendering.2")); //$NON-NLS-1$
		
		// create new byte columns
		TableColumn [] byteColumns = new TableColumn[bytesPerLine/columnSize];		
		
		String[] columnLabels = new String[0];
		IDebugModelPresentation presentation = DebugUIPlugin.getModelPresentation();
		if (presentation instanceof IMemoryBlockTablePresentation)
			columnLabels = ((IMemoryBlockTablePresentation)presentation).getColumnLabels(getMemoryBlock(), bytesPerLine, getNumCol());
		
		// check that column labels are not null
		if (columnLabels == null)
			columnLabels = new String[0];
		
		for (int i=0;i<byteColumns.length; i++)
		{
			TableColumn column = new TableColumn(fTableViewer.getTable(), SWT.LEFT, i+1);
			
			// if the number of column labels returned is correct
			// use supplied column labels
			if (columnLabels.length == byteColumns.length)
			{
				column.setText(columnLabels[i]);
			}
			else
			{
				// otherwise, use default
				int addressableUnit = columnSize/fAddressableSize;
				if (getAddressableUnitPerColumn() >= 4)
				{
					column.setText(Integer.toHexString(i*addressableUnit).toUpperCase() + 
						" - " + Integer.toHexString(i*addressableUnit+addressableUnit-1).toUpperCase()); //$NON-NLS-1$
				}
				else
				{
					column.setText(Integer.toHexString(i*addressableUnit).toUpperCase());
				}
			}
		}
		
		//Empty column for cursor navigation
		TableColumn emptyCol = new TableColumn(fTableViewer.getTable(),SWT.LEFT,byteColumns.length+1);
		emptyCol.setText(" "); //$NON-NLS-1$
		emptyCol.setWidth(1);
		emptyCol.setResizable(false);

		// +2 to include properties for address and navigation column
		String[] columnProperties = new String[byteColumns.length+2];
		columnProperties[0] = TableRenderingLine.P_ADDRESS;
		
		int addressableUnit = columnSize / getAddressableSize();

		// use column beginning offset to the row address as properties
		for (int i=1; i<columnProperties.length-1; i++)
		{
			// column properties are stored as number of addressable units from the
			// the line address
			columnProperties[i] = Integer.toHexString((i-1)*addressableUnit);
		}
		
		// Empty column for cursor navigation
		columnProperties[columnProperties.length-1] = " "; //$NON-NLS-1$
		
		fTableViewer.setColumnProperties(columnProperties);		
		
		
		Table table = fTableViewer.getTable();
		fEditors = new CellEditor[table.getColumnCount()];
		for (int i=0; i<fEditors.length; i++)
		{
			fEditors[i] = new TextCellEditor(table);
		}
		
		// create and set cell editors
		fTableViewer.setCellEditors(fEditors);	
		
		if (fIsCreated)
		{
			fTableViewer.refresh();
		}		
		
		
		resizeColumnsToPreferredSize();
		updateSyncColSize();
		
		if (fIsCreated)
		{
			// for Linux GTK, this must happen after table viewer is refreshed
			int i = findAddressIndex(fTopRowAddress);
			
			if (i >= 0)
				setTopIndex(fTableViewer.getTable(), i);
			
			if (isAddressVisible(selectedAddress))
				// after refresh, make sure the cursor is at the correct position
				setCursorAtAddress(selectedAddress);			
		}
		
		fEvtHandleLock.releaseLock(evtLockClient);
		
		return true;
	}
	
	/**
	 * Create the error page for this rendering.
	 * The error page is used to report any error resulted from
	 * getting memory from a memory block.
	 * @param parent
	 */
	private void createErrorPage(Composite parent)
	{
		if (fTextViewer == null)
		{
			fTextViewer = new TextViewer(parent, SWT.WRAP);	
			fTextViewer.setDocument(new Document());
			StyledText styleText = fTextViewer.getTextWidget();
			styleText.setEditable(false);
			styleText.setEnabled(false);
		}
	}
	
	/**
	 * Display content of the table viewer
	 */
	public void displayTable()
	{
		fIsShowingErrorPage = false;
		fPageBook.showPage(fTableViewer.getControl());
	}
	
	/**
	 * Display an error in the view tab.
	 * Make use of the text viewer instead of the table viewer.
	 * @param e
	 */
	public void displayError(DebugException e)
	{
		StyledText styleText = null;
		fIsShowingErrorPage = true;

		styleText = fTextViewer.getTextWidget();
		
		if (styleText != null)
			styleText.setText(DebugUIMessages.getString("AbstractTableRendering.3") + e.getMessage());	 //$NON-NLS-1$
		fPageBook.showPage(fTextViewer.getControl());
		
		// clear content cache if we need to display error
		fContentProvider.clearContentCache();
	}
	
	public boolean isDisplayingError()
	{	
		return fIsShowingErrorPage;
	}
	
	public Control getControl() {
		return fPageBook;
	}
	
	
	
	/**
	 * @return addressable size in bytes
	 */
	public int getAddressableSize() {
		return fAddressableSize;
	}
	
	private Object getSynchronizedProperty(String propertyId)
	{
		IMemoryRenderingSynchronizationService syncService = getMemoryRenderingContainer().getMemoryRenderingSite().getSynchronizationService();
		
		if (syncService == null)
			return null;
		
		return syncService.getProperty(getMemoryBlock(), propertyId);	
	}
	
	/**
	 * This method estimates the number of visible lines in the rendering
	 * table.  
	 * @return estimated number of visible lines in the table
	 */
	private int getNumberOfVisibleLines()
	{
		if(fTableViewer == null)
			return -1;
		
		Table table = fTableViewer.getTable();
		int height = fTableViewer.getTable().getSize().y;
		
		// when table is not yet created, height is zero
		if (height == 0)
		{
			// make use of the table viewer to estimate table size
			height = fTableViewer.getTable().getParent().getSize().y;
		}
		
		// height of border
		int border = fTableViewer.getTable().getHeaderHeight();
		
		// height of scroll bar
		int scroll = fTableViewer.getTable().getHorizontalBar().getSize().y;

		// height of table is table's area minus border and scroll bar height		
		height = height-border-scroll;

		// calculate number of visible lines
		int lineHeight = getMinTableItemHeight(table);
		
		int numberOfLines = height/lineHeight;
		
		if (numberOfLines <= 0)
			return 20;
	
		return numberOfLines;		
	}
	
	private static void  setTopIndex(Table table, int index)
	{
		MemoryViewUtil.linuxWorkAround(table);
		table.setTopIndex(index);
	}

	private void addRenderingToSyncService()
	{
		IMemoryRenderingSynchronizationService syncService = getMemoryRenderingContainer().getMemoryRenderingSite().getSynchronizationService();
		
		if (syncService == null)
			return;
		
		syncService.addPropertyChangeListener(this, null);
	
		if (!isDisplayingError())
		{
			if (getMemoryRenderingContainer().getMemoryRenderingSite().getSynchronizationProvider() == null)
				getMemoryRenderingContainer().getMemoryRenderingSite().setSynchronizationProvider(this);
			
			// check if there is already synchronization info available
			Object selectedAddress =getSynchronizedProperty( AbstractTableRendering.PROPERTY_SELECTED_ADDRESS);
			Object size =getSynchronizedProperty( AbstractTableRendering.PROPERTY_COL_SIZE);
			Object topAddress =getSynchronizedProperty( AbstractTableRendering.PROPERTY_TOP_ADDRESS);
			
			// if info is available, some other view tab has already been
			// created
			// do not overwirte info int he synchronizer if that's the case
			if (selectedAddress == null) {
				updateSyncSelectedAddress();
			}

			if (size == null) {
				updateSyncColSize();
			}
			if (topAddress == null) {
				updateSyncTopAddress();
			}
		}
	}
	
	/**
	 * Get properties from synchronizer and synchronize settings
	 */
	private void synchronize()
	{	
		Integer columnSize = (Integer) getSynchronizedProperty(AbstractTableRendering.PROPERTY_COL_SIZE);
		BigInteger selectedAddress = (BigInteger)getSynchronizedProperty(AbstractTableRendering.PROPERTY_SELECTED_ADDRESS);
		BigInteger topAddress = (BigInteger)getSynchronizedProperty(AbstractTableRendering.PROPERTY_TOP_ADDRESS);
		
		if (columnSize != null) {
			int colSize = columnSize.intValue();
			if (colSize > 0 && colSize != fColumnSize) {
				columnSizeChanged(colSize);
			}
		}
		if (topAddress != null) {
			if (!topAddress.equals(getTopVisibleAddress())) {
				if (selectedAddress != null) {
					if (!fSelectedAddress.equals(selectedAddress)) {
						selectedAddressChanged(selectedAddress);
					}
				}
				topVisibleAddressChanged(topAddress);
			}
		}
		if (selectedAddress != null) {
			if (selectedAddress.compareTo(fSelectedAddress) != 0) {
				selectedAddressChanged(selectedAddress);
			}
		}
	}
	
	/**
	 * Resize column to the preferred size
	 */
	public void resizeColumnsToPreferredSize() {
		// pack columns
		Table table = fTableViewer.getTable();
		TableColumn[] columns = table.getColumns();
		
		for (int i=0 ;i<columns.length-1; i++)
		{	
			columns[i].pack();
		}
		
		if (!fIsShowAddressColumn)
		{
			columns[0].setWidth(0);
		}
	}
	
	/**
	 * update selected address in synchronizer if update is true.
	 */
	private void updateSyncSelectedAddress() {
		
		if (!fIsCreated)
			return;
		PropertyChangeEvent event = new PropertyChangeEvent(this, AbstractTableRendering.PROPERTY_SELECTED_ADDRESS, null, fSelectedAddress);
		firePropertyChangedEvent(event);
	}

	/**
	 * update column size in synchronizer
	 */
	private void updateSyncColSize() {
		
		if (!fIsCreated)
			return;
		
		PropertyChangeEvent event = new PropertyChangeEvent(this, AbstractTableRendering.PROPERTY_COL_SIZE, null, new Integer(fColumnSize));
		firePropertyChangedEvent(event);
	}
	
	/**
	 * update top visible address in synchronizer
	 */
	private void updateSyncTopAddress() {
		
		if (!fIsCreated)
			return;

		PropertyChangeEvent event = new PropertyChangeEvent(this, AbstractTableRendering.PROPERTY_TOP_ADDRESS, null, fTopRowAddress);
		firePropertyChangedEvent(event);
	}
	
	/**
	 * Fill context menu of this rendering
	 * @param menu
	 */
	protected void fillContextMenu(IMenuManager menu) {
	
		menu.add(new Separator("topMenu")); //$NON-NLS-1$
		menu.add(fResetMemoryBlockAction);
		menu.add(fGoToAddressAction);
	
		menu.add(new Separator());
		
		if (fFormatColumnActions.length > 0)
		{
			// Format view tab actions
			IMenuManager formatMenu = new MenuManager(DebugUIMessages.getString("AbstractTableRendering.5"),  //$NON-NLS-1$
				"format"); //$NON-NLS-1$
			
			menu.appendToGroup("topMenu", formatMenu); //$NON-NLS-1$
		
			for (int i=0; i<fFormatColumnActions.length; i++)
			{
				formatMenu.add(fFormatColumnActions[i]);	
		
				// add check mark to the action to reflect current format of the view tab
				if (fFormatColumnActions[i] instanceof FormatColumnAction)
				{
					if (((FormatColumnAction)fFormatColumnActions[i]).getColumnSize() == getBytesPerColumn())
					{
						fFormatColumnActions[i].setChecked(true);
					}
					else
					{
						fFormatColumnActions[i].setChecked(false);
					}
				}
			}
		}
		
		menu.add(new Separator());
		menu.add(fReformatAction);
		menu.add(fToggleAddressColumnAction);
		menu.add(new Separator());
		menu.add(fCopyToClipboardAction);
		menu.add(fPrintViewTabAction);
	}
	
	/**
	 * @return number of addressable units per line
	 */
	public int getAddressableUnitPerLine() {
		return fBytePerLine / fAddressableSize;
	}
	
	/**
	 * @return number of addressable units per column
	 */
	public int getAddressableUnitPerColumn() {
		return fColumnSize / fAddressableSize;
	}
	
	/**
	 * @return current column size
	 */
	public int getBytesPerColumn()
	{
		return fColumnSize;
	}

	/**
	 * @return number of bytes per line
	 */
	public int getBytesPerLine()
	{
		return fBytePerLine;
	}
	
	/**
	 * Update labels of this rendering without regetting memory from the
	 * memory block.
	 */
	public void updateLabels()
	{
		// update tab labels
		updateRenderingLabel(true);
		
		if (fTableViewer != null)
		{
			// update column labels
			setColumnHeadings();
			fTableViewer.refresh();
		}
	}
	
	protected void updateRenderingLabel(boolean showAddress)
	{	
		fLabel = ""; //$NON-NLS-1$
		try {			
			if (getMemoryBlock() instanceof IMemoryBlockExtension)
			{
				fLabel = ((IMemoryBlockExtension)getMemoryBlock()).getExpression();
				
				if (fLabel.startsWith("&")) //$NON-NLS-1$
					fLabel = "&" + fLabel; //$NON-NLS-1$
				
				if (fLabel == null)
				{
					fLabel = DebugUIMessages.getString("AbstractTableRendering.8"); //$NON-NLS-1$
				}
				
				if (showAddress && ((IMemoryBlockExtension)getMemoryBlock()).getBigBaseAddress() != null)
				{	
					fLabel += " : 0x"; //$NON-NLS-1$
					fLabel += ((IMemoryBlockExtension)getMemoryBlock()).getBigBaseAddress().toString(16);
				}
			}
			else
			{
				long address = getMemoryBlock().getStartAddress();
				fLabel = Long.toHexString(address);
			}
		} catch (DebugException e) {
			fLabel = DebugUIMessages.getString("AbstractTableRendering.9");					 //$NON-NLS-1$
			DebugUIPlugin.log(e.getStatus());
		}
		
		String preName = DebugUITools.getMemoryRenderingManager().getRenderingType(getRenderingId()).getLabel();
		
		if (preName != null)
			fLabel += " <" + preName + ">"; //$NON-NLS-1$ //$NON-NLS-2$
		
		firePropertyChangedEvent(new PropertyChangeEvent(this, IBasicPropertyConstants.P_TEXT, null, fLabel));
	}
	
	private void setColumnHeadings()
	{
		String[] columnLabels = new String[0];

		IDebugModelPresentation presentation = DebugUIPlugin.getModelPresentation();
		if (presentation instanceof IMemoryBlockTablePresentation)
			columnLabels = ((IMemoryBlockTablePresentation)presentation).getColumnLabels(getMemoryBlock(), fBytePerLine, getNumCol());
		
		// check that column labels returned are not null
		if (columnLabels == null)
			columnLabels = new String[0];
		
		int numByteColumns = fBytePerLine/fColumnSize;
		
		TableColumn[] columns = fTableViewer.getTable().getColumns();
		
		int j=0;
		for (int i=1; i<columns.length-1; i++)
		{	
			// if the number of column labels returned is correct
			// use supplied column labels
			if (columnLabels.length == numByteColumns)
			{
				columns[i].setText(columnLabels[j]);
				j++;
			}
			else
			{
				// otherwise, use default
				if (fColumnSize >= 4)
				{
					columns[i].setText(Integer.toHexString(j*fColumnSize).toUpperCase() + 
							" - " + Integer.toHexString(j*fColumnSize+fColumnSize-1).toUpperCase()); //$NON-NLS-1$
				}
				else
				{
					columns[i].setText(Integer.toHexString(j*fColumnSize).toUpperCase());
				}
				j++;
			}
		}
	}
	
	/**
	 * Refresh the table viewer with the current top visible address.
	 * Update labels in the memory rendering.
	 */
	public void refresh()
	{	
		// refresh at start address of this memory block
		// address may change if expression is evaluated to a different value
		IMemoryBlock mem = getMemoryBlock();
		BigInteger address;
		
		if (mem instanceof IMemoryBlockExtension)
		{
			address = ((IMemoryBlockExtension)mem).getBigBaseAddress();
			
			if (address == null)
			{	
				DebugException e = new DebugException(DebugUIPlugin.newErrorStatus(DebugUIMessages.getString("AbstractTableRendering.10"), null)); //$NON-NLS-1$
				displayError(e);
				return;
			}
			
			updateRenderingLabel(true);
			
			// base address has changed
			if (address.compareTo(fContentProvider.getContentBaseAddress()) != 0)
			{
				// get to new address
				fSelectedAddress = address;
				updateSyncSelectedAddress();
				reloadTable(address, true);
				
				fTopRowAddress = address;
				updateSyncTopAddress();
				
				fContentInput.updateContentBaseAddress();
			}
			else
			{
				// reload at top of table
				address = getTopVisibleAddress();
				reloadTable(address, true);
			}				
		}
		else
		{
			address = BigInteger.valueOf(mem.getStartAddress());
			reloadTable(address, true);
		}
	}
	
	synchronized private void reloadTable(BigInteger topAddress, boolean updateDelta){
		
		if (fTableViewer == null)
			return;
		
		try
		{
			Table table = (Table)fTableViewer.getControl();	
			
			TableRenderingContentInput input = new TableRenderingContentInput(this, fContentInput.getPreBuffer(), fContentInput.getPostBuffer(), fContentInput.getDefaultBufferSize(), topAddress, getNumberOfVisibleLines(), updateDelta);
			fContentInput = input;
			fTableViewer.setInput(fContentInput);
	
			if (getMemoryBlock() instanceof IMemoryBlockExtension)
			{
				int topIdx = findAddressIndex(topAddress);
				
				if (topIdx != -1)
				{
					setTopIndex(table, topIdx);
				}
			}
			
			// cursor needs to be refreshed after reload
			if (isAddressVisible(fSelectedAddress))
				setCursorAtAddress(fSelectedAddress);
			
			MemoryViewUtil.linuxWorkAround(fTableViewer.getTable());
		}
		finally
		{
		}
	}
	
	private BigInteger getTopVisibleAddress() {
		
		if (fTableViewer == null)
			return BigInteger.valueOf(0);

		Table table = fTableViewer.getTable();
		int topIndex = getTopVisibleIndex(table);

		if (topIndex < 1) { topIndex = 0; }

		if (table.getItemCount() > topIndex) 
		{
			TableRenderingLine topItem = (TableRenderingLine)table.getItem(topIndex).getData();
			
			String calculatedAddress = null;
			if (topItem == null)
			{
				calculatedAddress = table.getItem(topIndex).getText();
			}
			else
			{
				calculatedAddress = topItem.getAddress();				
			}
			
			BigInteger bigInt = new BigInteger(calculatedAddress, 16);
			return bigInt;
		}
		return BigInteger.valueOf(0);
	}
	
	private int findAddressIndex(BigInteger address)
	{
		TableItem items[] = fTableViewer.getTable().getItems();
	
		for (int i=0; i<items.length; i++){
			
			// Again, when the table resizes, the table may have a null item
			// at then end.  This is to handle that.
			if (items[i] != null)
			{	
				TableRenderingLine line = (TableRenderingLine)items[i].getData();
				BigInteger lineAddress = new BigInteger(line.getAddress(), 16);
				int addressableUnit = getAddressableUnitPerLine();
				BigInteger endLineAddress = lineAddress.add(BigInteger.valueOf(addressableUnit));
				
				if (lineAddress.compareTo(address) <= 0 && endLineAddress.compareTo(address) > 0)
				{	
					return i;
				}
			}
		}
		
		return -1;
	}
	
	private static int getTopVisibleIndex(Table table)
	{
		MemoryViewUtil.linuxWorkAround(table);
		int index = table.getTopIndex();
		
		TableItem item = table.getItem(index);
		
		MemoryViewUtil.linuxWorkAround(table);
		while (item.getBounds(0).y < 0)
		{
			index++;
			item = table.getItem(index);
		}
		
		return index;
	}
	
	public TableViewer getTableViewer()
	{
		return fTableViewer;
	}
	
	public void dispose() {
		try {	
			// prevent rendering from being disposed again
			if (fIsDisposed)
				return;
			
			fIsDisposed = true;
			
			if (fContentProvider != null)
				fContentProvider.dispose();
			
			ScrollBar scroll = ((Table)fTableViewer.getControl()).getVerticalBar();
			if (scroll != null)
				scroll.removeSelectionListener(fScrollbarSelectionListener);
			
			fTableCursor.removeTraverseListener(fCursorTraverseListener);
			fTableCursor.removeKeyListener(fCursorKeyAdapter);
			fTableCursor.removeMouseListener(fCursorMouseListener);
			
			fCursorEditor.dispose();
			
			fTextViewer = null;
			fTableViewer = null;
			fTableCursor = null;
			
			// clean up cell editors
			for (int i=0; i<fEditors.length; i++)
			{
				fEditors[i].dispose();
			}
			
			// remove font change listener when the view tab is disposed
			JFaceResources.getFontRegistry().removeListener(this);
			
			// remove the view tab from the synchronizer
			IMemoryRenderingSynchronizationService syncService = getMemoryRenderingContainer().getMemoryRenderingSite().getSynchronizationService();
			if (syncService != null)
				syncService.removePropertyChangeListener(this);
			
			DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
			
			super.dispose();

		} catch (Exception e) {}
	}
	
	private int getNumCol() {
		
		int bytesPerLine = getBytesPerLine();
		int columnSize = getBytesPerColumn();
		
		return bytesPerLine/columnSize;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#setFont(org.eclipse.swt.graphics.Font)
	 */
	private void setFont(Font font)
	{	
		int oldIdx = getTopVisibleIndex(fTableViewer.getTable());
		
		// BUG in table, if font is changed when table is not starting
		// from the top, causes table gridline to be misaligned.
		setTopIndex(fTableViewer.getTable(),  0);
		
		// set font
		fTableViewer.getTable().setFont(font);
		fTableCursor.setFont(font);
		
		setTopIndex(fTableViewer.getTable(),  oldIdx);
		
		resizeColumnsToPreferredSize();
		
		// update table cursor and force redraw
		setCursorAtAddress(fSelectedAddress);
	}
	
	
	/**
	 * Move cursor to the specified address.
	 * Will load more memory if the address is not currently visible.
	 * @param address
	 * @throws DebugException
	 */
	public void goToAddress(BigInteger address) throws DebugException {
		Object evtLockClient = new Object();
		try
		{	
			if (!fEvtHandleLock.acquireLock(evtLockClient))
				return;

			// if address is within the range, highlight			
			if (!isAddressOutOfRange(address))
			{
				fSelectedAddress = address;
				updateSyncSelectedAddress();
				setCursorAtAddress(fSelectedAddress);
				
				// force the cursor to be shown
				if (!isAddressVisible(fSelectedAddress))
				{	
					int i = findAddressIndex(fSelectedAddress);
					fTableViewer.getTable().showItem(fTableViewer.getTable().getItem(i));
				}
			}
			else
			{
				// if not extended memory block
				// do not allow user to go to an address that's out of range
				if (!(getMemoryBlock() instanceof IMemoryBlockExtension))
				{
					Status stat = new Status(
					 IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(),
					 DebugException.NOT_SUPPORTED, DebugUIMessages.getString("AbstractTableRendering.11"), null  //$NON-NLS-1$
					);
					DebugException e = new DebugException(stat);
					throw e;
				}

				BigInteger startAdd = fContentInput.getStartAddress();
				BigInteger endAdd = fContentInput.getEndAddress();
				
				if (address.compareTo(startAdd) < 0 ||
					address.compareTo(endAdd) > 0)
				{
					Status stat = new Status(
					 IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(),
					 DebugException.NOT_SUPPORTED, DebugUIMessages.getString("AbstractTableRendering.11"), null  //$NON-NLS-1$
					);
					DebugException e = new DebugException(stat);
					throw e;
				}
				
				fSelectedAddress = address;
				updateSyncSelectedAddress();
				
				//otherwise, reload at the address
				reloadTable(address,false);
				
				// if the table is reloaded, the top address is chagned in this case
				fTopRowAddress = address;
				updateSyncTopAddress();
				
				// set the cursor at the selected address after reload
				setCursorAtAddress(address);
			}
			fTableCursor.setVisible(true);
		}
		catch (DebugException e)
		{
			throw e;
		}
		finally
		{
			fEvtHandleLock.releaseLock(evtLockClient);
		}
	}
	
	/**
	 * Check if address provided is out of buffered range
	 * @param address
	 * @return if address is out of bufferred range
	 */
	private boolean isAddressOutOfRange(BigInteger address)
	{
		return fContentProvider.isAddressOutOfRange(address);
	}
	
	/**
	 * Check if address is visible
	 * @param address
	 * @return if the given address is visible
	 */
	private boolean isAddressVisible(BigInteger address)
	{
		// if view tab is not yet created 
		// cursor should always be visible
		if (!fIsCreated)
			return true;
		
		BigInteger topVisible = getTopVisibleAddress();
		int addressableUnit = getAddressableUnitPerLine();
		BigInteger lastVisible = getTopVisibleAddress().add(BigInteger.valueOf((getNumberOfVisibleLines() * addressableUnit) + addressableUnit));
		
		if (topVisible.compareTo(address) <= 0 && lastVisible.compareTo(address) > 0)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Create actions for the view tab
	 */
	protected void createActions() {
		fCopyToClipboardAction = new CopyTableRenderingToClipboardAction(this);
		fGoToAddressAction = new GoToAddressAction(this);
		fResetMemoryBlockAction = new ResetToBaseAddressAction(this);
		fPrintViewTabAction = new PrintTableRenderingAction(this);
		
		fFormatColumnActions = new Action[6];
		fFormatColumnActions[0] =  new FormatColumnAction(1, fAddressableSize, this);
		fFormatColumnActions[1] =  new FormatColumnAction(2, fAddressableSize, this);
		fFormatColumnActions[2] =  new FormatColumnAction(4, fAddressableSize, this);
		fFormatColumnActions[3] =  new FormatColumnAction(8, fAddressableSize, this);
		fFormatColumnActions[4] =  new FormatColumnAction(16, fAddressableSize, this);
		fFormatColumnActions[5] =  new SetColumnSizeDefaultAction(this);
		
		fReformatAction = new ReformatAction(this);
		fToggleAddressColumnAction = new ToggleAddressColumnAction();
	}
	
	/**
	 * Handle scrollling and reload table if necessary
	 * @param event
	 */
	private synchronized void handleScrollBarSelection()
	{
		Object evtLockClient = new Object();
		try
		{			
			if (fIsDisposed)
				return;
			
			BigInteger address = getTopVisibleAddress();
	
			if (!fTopRowAddress.equals(address))
			{
				fTopRowAddress = address;
				updateSyncTopAddress();
			}
			
			if (!fEvtHandleLock.acquireLock(evtLockClient))
				return;
			
			if (getMemoryBlock() instanceof IMemoryBlockExtension)
			{
				if (!isAddressOutOfRange(address))
				{
					Table table = fTableViewer.getTable();
					int numInBuffer = table.getItemCount();
					int index = findAddressIndex(address);
					if (index < 3)
					{
						if (isAtTopLimit())
						{
							setTopIndex(table, index);
						}
						else
						{
							reloadTable(address, false);
						}
					}
					else if ((numInBuffer-(index+getNumberOfVisibleLines())) < 3)
					{
						if (!isAtBottomLimit())
							reloadTable(address, false);
					}
				}
				else
				{	
					// approaching limit, reload table
					reloadTable(address, false);
				}
				
				if (isAddressVisible(fSelectedAddress))
					fTableCursor.setVisible(true);
				else
					fTableCursor.setVisible(false);
			}
		}
		finally
		{
			fEvtHandleLock.releaseLock(evtLockClient);
		}
	}
	
	
	private boolean isAtTopLimit()
	{	
		BigInteger startAddress = fContentInput.getStartAddress();
		startAddress = alignDoubleWordBoundary(startAddress);
		
		BigInteger startBufferAddress = fContentProvider.getBufferTopAddress();
		startBufferAddress = alignDoubleWordBoundary(startBufferAddress);
		
		if (startAddress.compareTo(startBufferAddress) == 0)
			return true;
		
		return false;
	}
	
	private boolean isAtBottomLimit()
	{
		BigInteger endAddress = fContentInput.getEndAddress();
		endAddress = alignDoubleWordBoundary(endAddress);
		
		BigInteger endBufferAddress = fContentProvider.getBufferEndAddress();
		endBufferAddress = alignDoubleWordBoundary(endBufferAddress);
		
		if (endAddress.compareTo(endBufferAddress) == 0)
			return true;
		
		return false;		
	}
	
	private BigInteger alignDoubleWordBoundary(BigInteger integer)
	{
		String str =integer.toString(16);
		if (!str.endsWith("0")) //$NON-NLS-1$
		{
			str = str.substring(0, str.length() - 1);
			str += "0"; //$NON-NLS-1$
			integer = new BigInteger(str, 16);
		}		
		
		return integer;
	}
	
	private boolean needMoreLines()
	{
		if (getMemoryBlock() instanceof IMemoryBlockExtension)
		{		
			Table table = fTableViewer.getTable();
			TableItem firstItem = table.getItem(0);
			TableItem lastItem = table.getItem(table.getItemCount()-1);
			
			if (firstItem == null || lastItem == null)
				return true;
			
			TableRenderingLine first = (TableRenderingLine)firstItem.getData();
			TableRenderingLine last = (TableRenderingLine) lastItem.getData();
			
			if (first == null ||last == null)
			{
				// For some reason, the table does not return the correct number
				// of table items in table.getItemCount(), causing last to be null.
				// This check is to ensure that we don't get a null pointer exception.
				return true;
			}
			
			BigInteger startAddress = new BigInteger(first.getAddress(), 16);
			BigInteger lastAddress = new BigInteger(last.getAddress(), 16);
			int addressableUnit = getAddressableUnitPerLine();
			lastAddress = lastAddress.add(BigInteger.valueOf(addressableUnit));
			
			BigInteger topVisibleAddress = getTopVisibleAddress();
			long numVisibleLines = getNumberOfVisibleLines();
			long numOfBytes = numVisibleLines * addressableUnit;
			
			BigInteger lastVisibleAddrss = topVisibleAddress.add(BigInteger.valueOf(numOfBytes));
			
			// if there are only 3 lines left at the top, refresh
			BigInteger numTopLine = topVisibleAddress.subtract(startAddress).divide(BigInteger.valueOf(addressableUnit));
			if (numTopLine.compareTo(BigInteger.valueOf(3)) <= 0 && (startAddress.compareTo(BigInteger.valueOf(0)) != 0))
			{
				if (!isAtTopLimit())
					return true;
			}
			
			// if there are only 3 lines left at the bottom, refresh
			BigInteger numBottomLine = lastAddress.subtract(lastVisibleAddrss).divide(BigInteger.valueOf(addressableUnit));
			if (numBottomLine.compareTo(BigInteger.valueOf(3)) <= 0)
			{
				if (!isAtBottomLimit())
					return true;
			}
			
			return false;
		}
		
		return false;
	}

	private void handleTableMouseEvent(MouseEvent e) {
		// figure out new cursor position based on here the mouse is pointing
		TableItem[] tableItems = fTableViewer.getTable().getItems();
		TableItem selectedRow = null;
		int colNum = -1;
		int numCol = fTableViewer.getColumnProperties().length;
		
		for (int j=0; j<tableItems.length; j++)
		{
			TableItem item = tableItems[j];
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
		
		// if column position cannot be determined, return
		if (colNum < 1)
			return;
		
		// handle user mouse click onto table
		// move cursor to new position
		if (selectedRow != null)
		{
			int row = fTableViewer.getTable().indexOf(selectedRow);
			fTableCursor.setVisible(true);
			fTableCursor.setSelection(row, colNum);
			
			// manually call this since we don't get an event when
			// the table cursor changes selection.
			handleCursorMoved();
			
			fTableCursor.setFocus();
		}			
	}
	
	/**
	 * Handle column size changed event from synchronizer
	 * @param newColumnSize
	 */
	private void columnSizeChanged(final int newColumnSize) {
		// ignore event if view tab is disabled
		if (!isVisible())
			return;

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				format(getBytesPerLine(), newColumnSize);
			}
		});
	}
	
	private void handleCursorMouseEvent(MouseEvent e){
		if (e.button == 1)
		{
			int col = fTableCursor.getColumn();
			if (col > 0 && col <= (getNumCol()))
				activateCellEditor(null);
		}			
	}
	
	/**
	 * Activate celll editor and prefill it with initial value.
	 * If initialValue is null, use cell content as initial value
	 * @param initialValue
	 */
	private void activateCellEditor(String initialValue) {
		
		int col = fTableCursor.getColumn();
		int row = findAddressIndex(fSelectedAddress);
		
		if (row < 0)
			return;
		// do not allow user to edit address column
		if (col == 0 || col > getNumCol())
		{
			return;
		}
		
		ICellModifier cellModifier = null;
		
		if (fTableViewer == null)
		{
			return;
		}
		cellModifier = fTableViewer.getCellModifier();
		
		TableItem tableItem = fTableViewer.getTable().getItem(row);
		
		Object element = tableItem.getData();
		Object property = fTableViewer.getColumnProperties()[col];
		Object value = cellModifier.getValue(element, (String)property);
		
		// The cell modifier canModify function always returns false if the edit action 
		// is not invoked from here.  This is to prevent data to be modified when
		// the table cursor loses focus from a cell.  By default, data will
		// be changed in a table when the cell loses focus.  This is to workaround
		// this default behaviour and only change data when the cell editor
		// is activated.
		((TableRenderingCellModifier)cellModifier).setEditActionInvoked(true);
		boolean canEdit = cellModifier.canModify(element, (String)property);
		((TableRenderingCellModifier)cellModifier).setEditActionInvoked(false);
		
		if (!canEdit)
			return;
		
		// activate based on current cursor position
		TextCellEditor selectedEditor = (TextCellEditor)fTableViewer.getCellEditors()[col];

		
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
	
	/**
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
	
	/**
	 * @param text
	 */
	private void removeListeners(Text text) {
		
		text.removeFocusListener(fEditorFocusListener);
		text.removeKeyListener(fEditorKeyListener);
	}
	
	private void handleTableEditorFocusLost(FocusEvent event)
	{
		final FocusEvent e = event;

		Display.getDefault().syncExec(new Runnable() {

			public void run()
			{
				try
				{
					int row = findAddressIndex(fSelectedAddress);
					int col = fTableCursor.getColumn();
					
					Text text = (Text)e.getSource();
					removeListeners(text);

					// get new value
					String newValue = text.getText();
					
					// modify memory at fRow and fCol
					modifyValue(row, col, newValue);
							
					// show cursor after modification is completed
					setCursorAtAddress(fSelectedAddress);
					fTableCursor.moveAbove(text);
					fTableCursor.setVisible(false);
					fTableCursor.setVisible(true);
				}
				catch (NumberFormatException e1)
				{
					MemoryViewUtil.openError(DebugUIMessages.getString(TableRenderingCellModifier.TITLE), 
						DebugUIMessages.getString(TableRenderingCellModifier.DATA_IS_INVALID), null);
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
				int row = findAddressIndex(fSelectedAddress);
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
							
							if (row+1 >= fTableViewer.getTable().getItemCount())
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
							int numCharsPerByte = getNumCharsPerByte();
							if (numCharsPerByte > 0)
							{
								if (text.getText().length() > getBytesPerColumn()*numCharsPerByte)
								{
									String newValue = text.getText();
									text.setText(newValue.substring(0, getBytesPerColumn()*numCharsPerByte));
									
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
									activateCellEditor(newValue.substring(getBytesPerColumn()*numCharsPerByte));
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
							numCharsPerByte = getNumCharsPerByte();
							if (numCharsPerByte > 0)
							{								
								if (text.getText().length()> getBytesPerColumn()* numCharsPerByte)
								{
									String newValue = text.getText();
									text.setText(newValue.substring(0,getBytesPerColumn()* numCharsPerByte));
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
									
									activateCellEditor(newValue.substring(getBytesPerColumn()*numCharsPerByte));
								}
							}
						break;
					}
				}
				catch (NumberFormatException e1)
				{
					MemoryViewUtil.openError(DebugUIMessages.getString(TableRenderingCellModifier.TITLE), 
						DebugUIMessages.getString(TableRenderingCellModifier.DATA_IS_INVALID), null);
					
					fTableCursor.setSelection(row, col);
					handleCursorMoved();
			
					removeListeners(text);
				}
			}
		});
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
		
		TableItem tableItem = fTableViewer.getTable().getItem(row);

		Object property = fTableViewer.getColumnProperties()[col];
		fTableViewer.getCellModifier().modify(tableItem, (String)property, newValue);
	}
	
	public void becomesHidden() {
		
		if (isVisible() == false)
			return;

		super.becomesHidden();
		
		if (getMemoryBlock() instanceof IMemoryBlockExtension)
		{	
			updateRenderingLabel(false);
		}
			
		// once the view tab is disabled, all deltas information becomes invalid.
		// reset changed information and recompute if data has really changed when
		// user revisits the same tab.	
		fContentProvider.resetDeltas();
		
	}
	public void becomesVisible() {
		
		// do not do anything if already visible
		if (isVisible() == true)
			return;
		
		super.becomesVisible();
		
		IMemoryBlock mem = getMemoryBlock();
		
		BigInteger oldBase = fContentProvider.getContentBaseAddress();

		refresh();
		
		if (mem instanceof IMemoryBlockExtension)
		{
			BigInteger baseAddress = ((IMemoryBlockExtension)mem).getBigBaseAddress();
			
			if (baseAddress == null)
			{
				if (fSelectedAddress != null)
					baseAddress = fSelectedAddress;
				else
					baseAddress = new BigInteger("0"); //$NON-NLS-1$
			}
			
			Object[] connected = ((IMemoryBlockExtension)mem).getConnections();				
			
			// if the base address has changed, update cursor
			// and this is the first time this memory block is enabled
			if (!baseAddress.equals(oldBase) && connected.length == 1)
			{
				fSelectedAddress = baseAddress;
				setCursorAtAddress(fSelectedAddress);
				
				updateSyncTopAddress();
				updateSyncSelectedAddress();
			}
			else
			{
				// otherwise, take synchronized settings
				synchronize();
			}
		}
		else
		{
			synchronize();
		}
		updateRenderingLabel(true);
	}
	
	/**
	 * Reset this memory rendering.
	 * Cursor will be moved to the base address of the memory block.
	 * The table will be positioned back to have the base address
	 * at the top.
	 */
	public void reset()
	{
		BigInteger baseAddress;
	
		if (getMemoryBlock() instanceof IMemoryBlockExtension)
		{
			baseAddress = ((IMemoryBlockExtension)getMemoryBlock()).getBigBaseAddress();
		}
		else
		{
			baseAddress = BigInteger.valueOf(getMemoryBlock().getStartAddress());
		}
		try {
			goToAddress(baseAddress);
			topVisibleAddressChanged(baseAddress);
		} catch (DebugException e) {
			MemoryViewUtil.openError(DebugUIMessages.getString("AbstractTableRendering.12"), DebugUIMessages.getString("AbstractTableRendering.13"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * @return the current selected address in the rendering.
	 */
	public BigInteger getSelectedAddress() {
		return fSelectedAddress;
	}

	/**
	 * @return the content at the selected address.
	 */
	public String getSelectedContent() {

		if (isAddressOutOfRange(fSelectedAddress))
			return ""; //$NON-NLS-1$
		
		int col = fTableCursor.getColumn();
		TableItem rowItem = fTableCursor.getRow();
		int row = fTableViewer.getTable().indexOf(rowItem);
		
		// check precondition
		if (col == 0 || col > getBytesPerLine()/getBytesPerColumn())
		{
			return ""; //$NON-NLS-1$
		}
				
		TableItem tableItem = getTableViewer().getTable().getItem(row);
		
		return tableItem.getText(col);	
	}
	
	/**
	 * @return number of characters a byte will converted to.  Return
	 * -1 if this value is unknown.
	 */
	public int getNumCharsPerByte()
	{
		return -1;
	}
	
	private int getMinTableItemHeight(Table table){
		
		// Hack to get around Linux GTK problem.
		// On Linux GTK, table items have variable item height as
		// carriage returns are actually shown in a cell.  Some rows will be
		// taller than others.  When calculating number of visible lines, we
		// need to find the smallest table item height.  Otherwise, the rendering
		// underestimates the number of visible lines.  As a result the rendering
		// will not be able to get more memory as needed.
		if (MemoryViewUtil.isLinuxGTK())
		{
			// check each of the items and find the minimum
			TableItem[] items = table.getItems();
			int minHeight = table.getItemHeight();
			for (int i=0; i<items.length; i++)
			{
				minHeight = Math.min(items[i].getBounds(0).height, minHeight);
			}
			
			return minHeight;
				
		}
		return table.getItemHeight();
	}
	
	/**
	 * This is called by the label provider for <code>AbstractTableRendering</code> Implementor can
	 * reuse a memory view tab and presents data in a different format.
	 * 
	 * @param dataType -
	 *            type of data the bytes hold
	 * @param address -
	 *            addres where the bytes belong to
	 * @param data -
	 *            the bytes
	 * @return a string to represent the memory. Do not return null. Return a
	 *         string to pad the cell if the memory cannot be converted
	 *         successfully.
	 */
	abstract public String getString(String dataType, BigInteger address, MemoryByte[] data);
	
	/**
	 * This is called by the cell modifier from an AbstractTableRendering.
	 * Implementor will convert the string value to an array of bytes.  The bytes will
	 * be passed to the debug adapter for memory block modification.
	 * Return null if the byte cannot be formatted properly.
	 * @param dataType - type of data the string represents
	 * @param address - address where the bytes belong to
	 * @param currentValues - current values of the data in bytes format
	 * @param newValue - the string to be converted to bytes
	 * @return the bytes to be passed to debug adapter for modification.
	 */
	abstract public byte[] getBytes(String dataType, BigInteger address, MemoryByte[] currentValues, String newValue);

}	

