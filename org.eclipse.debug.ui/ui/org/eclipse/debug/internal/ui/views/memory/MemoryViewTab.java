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
import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock;
import org.eclipse.debug.internal.core.memory.IMemoryRendering;
import org.eclipse.debug.internal.core.memory.MemoryBlockManager;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.LazyModelPresentation;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.0
 */
public class MemoryViewTab extends AbstractMemoryViewTab implements SelectionListener, ControlListener, KeyListener, ITableMemoryViewTab, ISynchronizedMemoryBlockView{	
	
	private static final String PREFIX = "MemoryViewTab."; //$NON-NLS-1$
	private static final String ADDRESS = PREFIX + "Address"; //$NON-NLS-1$
	private static final String ERROR = PREFIX + "Error"; //$NON-NLS-1$
	private static final String FORMAT_IS_INVALID = PREFIX + "Format_is_invalid"; //$NON-NLS-1$
	private static final String ADDRESS_IS_OUT_OF_RANGE = PREFIX + "Address_is_out_of_range"; //$NON-NLS-1$
	private static final String COLUMN_SIZE = PREFIX + "Column_size"; //$NON-NLS-1$
	//private static final String ADD_RENDERING = PREFIX + "Add_rendering"; //$NON-NLS-1$
	private static final String UNABLE_TO_GET_BASE_ADDRESS = PREFIX +"Unable_to_retrieve_base_address"; //$NON-NLS-1$
	private static final String UNKNOWN = PREFIX + "Unknown"; //$NON-NLS-1$
	
	// menu group names
	private static final String MEMORY_ACTIONS_GROUP = IDebugUIConstants.PLUGIN_ID + ".MemoryViewActionsGroup"; //$NON-NLS-1$
	private static final String MEMORY_ACTIONS_FORMAT_GROUP = IDebugUIConstants.PLUGIN_ID + ".MemoryViewActionsGroup.format"; //$NON-NLS-1$
	//private static final String MEMORY_ACTIONS_RENDERING_GROUP = IDebugUIConstants.PLUGIN_ID + ".MemoryViewActionsGroup.rendering"; //$NON-NLS-1$
	
	private MemoryViewContentProvider contentProvider;
	private TableViewer fTableViewer = null;
	private boolean fEnabled;
	private ViewTabCursorManager fCursorManager;

	private IMemoryBlockModelPresentation fMemoryBlockPresentation;
	private boolean fNoPresentation = false;
	
	public int TABLE_PREBUFFER = 20;
	public int TABLE_POSTBUFFER = 20;
	public int TABLE_DEFAULTBUFFER = 20;
	
	private TextViewer fTextViewer = null;
	private boolean errorOccurred = false;
	
	protected  BigInteger fSelectedAddress = null;
	
	private boolean fTabCreated = false;
	
	private CellEditor fEditors[];
	private ICellModifier fCellModifier;
	
	private CopyViewTabToClipboardAction fCopyToClipboardAction;
	private GoToAddressAction fGoToAddressAction;
	private ResetMemoryBlockAction fResetMemoryBlockAction;
	private PrintViewTabAction fPrintViewTabAction;
	private Action[] fFormatColumnActions;
	private ReformatAction fReformatAction;
	
	private boolean fIsDisposed = false;
	
	private int fBytePerLine;								// number of bytes per line: 16
	private int fColumnSize;								// number of bytes per column:  1,2,4,8
	
	// font change listener
	private FontChangeListener fFontChangeListener;
	private TabFolderDisposeListener fTabFolderDisposeListener;

	private static final int[] ignoreEvents =
	{
		SWT.ARROW_UP,
		SWT.ARROW_DOWN,
		SWT.ARROW_LEFT,
		SWT.ARROW_RIGHT,
		SWT.PAGE_UP,
		SWT.PAGE_DOWN,
		SWT.HOME,
		SWT.END,
		SWT.INSERT,
		SWT.F1,
		SWT.F2,
		SWT.F3,
		SWT.F4,
		SWT.F5,
		SWT.F6,
		SWT.F7,
		SWT.F8,
		SWT.F9,
		SWT.F10,
		SWT.F11,
		SWT.F12,
		SWT.F13,
		SWT.F14,
		SWT.F15,
		SWT.HELP,
		SWT.CAPS_LOCK,
		SWT.NUM_LOCK,
		SWT.SCROLL_LOCK,
		SWT.PAUSE,
		SWT.BREAK,
		SWT.PRINT_SCREEN,
		SWT.ESC,
		SWT.CTRL,
		SWT.ALT
	};
	
	private final class TabFolderDisposeListener implements DisposeListener
	{
		MemoryViewTab fViewTab;
		TabFolderDisposeListener(MemoryViewTab viewTab)
		{
			fViewTab = viewTab;
		}
		
		public void widgetDisposed(DisposeEvent e)
		{
			if (!fViewTab.fIsDisposed)
			{
				// remove listeners
				JFaceResources.getFontRegistry().removeListener(fFontChangeListener);
				removeReferenceFromSynchronizer();
				getMemoryBlockViewSynchronizer().removeView(fViewTab);				
			}
		}
	}

	class FontChangeListener implements IPropertyChangeListener
	{
		/* (non-Javadoc)
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event)
		{
			// if memory view table font has changed
			if (event.getProperty().equals(IInternalDebugUIConstants.FONT_NAME))
			{
				if (!fIsDisposed)
				{			
					Font memoryViewFont = JFaceResources.getFont(IInternalDebugUIConstants.FONT_NAME);
					setFont(memoryViewFont);					
				}
			}
		}	
	}
	
	// **  Referring to internal class:  DelegatingModelPresentation and LazyModelPresentation
	// **  This should be ok when Memory View is contributed to Eclipse platform?
	class MemoryViewDelegatingModelPresentation extends DelegatingModelPresentation
	{
		
		MemoryViewDelegatingModelPresentation()
		{
			IExtensionPoint point= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.ID_DEBUG_MODEL_PRESENTATION);
			if (point != null) {
				IExtension[] extensions= point.getExtensions();
				for (int i= 0; i < extensions.length; i++) {
					IExtension extension= extensions[i];
					IConfigurationElement[] configElements= extension.getConfigurationElements();
					for (int j= 0; j < configElements.length; j++) {
						IConfigurationElement elt= configElements[j];
						String id= elt.getAttribute("id"); //$NON-NLS-1$
						if (id != null) {
							IDebugModelPresentation lp= new MemoryViewLazyModelPresentation(elt);
							getLabelProviders().put(id, lp);
						}
					}
				}
			}			
		}

	}
	
	class MemoryViewLazyModelPresentation extends LazyModelPresentation implements IMemoryBlockModelPresentation
	{

		MemoryViewLazyModelPresentation(IConfigurationElement element)
		{
			super(element);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.IMemoryBlockModelPresentation#getTabLabel(org.eclipse.debug.core.model.IMemoryBlock)
		 */
		public String getTabLabel(IMemoryBlock blk, String renderingId)
		{
			IDebugModelPresentation presentation = getPresentation();
			
			if (presentation instanceof IMemoryBlockModelPresentation)
			{
				return ((IMemoryBlockModelPresentation)presentation).getTabLabel(blk, getRenderingId()); 
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.IMemoryBlockModelPresentation#getColumnLabels(org.eclipse.debug.core.model.IMemoryBlock, int, int)
		 */
		public String[] getColumnLabels(IMemoryBlock blk, int bytesPerLine, int columnSize)
		{
			IDebugModelPresentation presentation = getPresentation();
			
			if (presentation instanceof IMemoryBlockModelPresentation)
			{
				return ((IMemoryBlockModelPresentation)presentation).getColumnLabels(blk, bytesPerLine, columnSize); 
			}
			return new String[0];
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.IMemoryBlockModelPresentation#getAddressPresentation(org.eclipse.debug.core.model.IMemoryBlock, java.math.BigInteger)
		 */
		public String getAddressPresentation(IMemoryBlock blk, BigInteger address)
		{
			IDebugModelPresentation presentation = getPresentation();
			
			if (presentation instanceof IMemoryBlockModelPresentation)
			{
				return ((IMemoryBlockModelPresentation)presentation).getAddressPresentation(blk, address); 
			}
			return null;
		}
	}

	public MemoryViewTab(IMemoryBlock newMemory, TabItem newTab, MenuManager menuMgr, IMemoryRendering rendering, AbstractMemoryRenderer renderer) {
		super(newMemory, newTab, menuMgr, rendering);
			
		setTabName(newMemory, true);
		
		fTabItem.setControl(createFolderPage(renderer));
		
		if (!(newMemory instanceof IExtendedMemoryBlock))
		{		
			// If not extended memory block, do not create any buffer
			// no scrolling
			TABLE_PREBUFFER=0;
			TABLE_POSTBUFFER=0;
			TABLE_DEFAULTBUFFER=0;
		}

		if (fTableViewer != null)
		{	
			fTableViewer.getTable().setTopIndex(TABLE_PREBUFFER);
		}
		
		addViewTabToSynchronizer();
		
		// otherwise, this is a totally new synchronize info

		fEnabled = true;
		fTabCreated = true;
		
		//synchronize
		synchronize();		
		
		createActions();
		
		// Need to resize column after content is filled in
		// Pack function does not work unless content is not filled in
		// since the table is not able to compute the preferred size.
		packColumns();
		
		// add listeners in the end to make sure that the resize event
		// does not affect synchronization
		if (fTableViewer != null){
			fTableViewer.getTable().addSelectionListener(this);
			fTabFolderDisposeListener = new TabFolderDisposeListener(this);
			fTabItem.addDisposeListener(fTabFolderDisposeListener);
		}
		
		if (fMemoryBlock instanceof IExtendedMemoryBlock)
		{
			if(((IExtendedMemoryBlock)fMemoryBlock).getBigBaseAddress() == null)
			{
				DebugException e = new DebugException(DebugUIPlugin.newErrorStatus(DebugUIMessages.getString(UNABLE_TO_GET_BASE_ADDRESS), null));
				displayError(e);				
			}
		}
	}
	
	private void addViewTabToSynchronizer()
	{
		getMemoryBlockViewSynchronizer().addView(this, null);
	
		// check if there is already synchronization info available
		Object selectedAddress =getSynchronizedProperty( IMemoryViewConstants.PROPERTY_SELECTED_ADDRESS);
		Object size =getSynchronizedProperty( IMemoryViewConstants.PROPERTY_COL_SIZE);
		Object topAddress =getSynchronizedProperty( IMemoryViewConstants.PROPERTY_TOP_ADDRESS);
		
		// if info is available, some other view tab has already been created
		// do not overwirte info int he synchronizer if that's the case
		if (selectedAddress == null)
		{
			updateSyncSelectedAddress(true);
		}	
		
		if (size == null)
		{
			updateSyncColSize();	
		}
		if (topAddress == null)
		{
			updateSyncTopAddress(true);
		}
		
	}
	
	/**
	 * update selected address in synchronizer if update is true.
	 */
	private void updateSyncSelectedAddress(boolean update) {
		
		if (update)
			getMemoryBlockViewSynchronizer().setSynchronizedProperty(getMemoryBlock(), IMemoryViewConstants.PROPERTY_SELECTED_ADDRESS, fSelectedAddress);
	}

	/**
	 * update column size in synchronizer
	 */
	private void updateSyncColSize() {
		getMemoryBlockViewSynchronizer().setSynchronizedProperty(getMemoryBlock(), IMemoryViewConstants.PROPERTY_COL_SIZE, new Integer(fColumnSize));
	}
	
	/**
	 * update top visible address in synchronizer
	 */
	protected void updateSyncTopAddress(boolean updateToSynchronizer) {
		
		if (updateToSynchronizer)
		{
			getMemoryBlockViewSynchronizer().setSynchronizedProperty(getMemoryBlock(), IMemoryViewConstants.PROPERTY_TOP_ADDRESS, getTopVisibleAddress());
		}
	}

	protected void setTabName(IMemoryBlock newMemory, boolean showAddress)
	{
		String tabName = null;
		
		if (getMemoryBlockPresentation() != null)
			tabName = getMemoryBlockPresentation().getTabLabel(newMemory, getRenderingId());
		
		if (tabName == null)
		{
		
			tabName = ""; //$NON-NLS-1$
			try {			
				if (newMemory instanceof IExtendedMemoryBlock)
				{
					tabName = ((IExtendedMemoryBlock)newMemory).getExpression();
					
					if (tabName.startsWith("&")) //$NON-NLS-1$
						tabName = "&" + tabName; //$NON-NLS-1$
					
					if (tabName == null)
					{
						tabName = DebugUIMessages.getString(UNKNOWN);
					}
					
					if (showAddress && ((IExtendedMemoryBlock)newMemory).getBigBaseAddress() != null)
					{	
						tabName += " : 0x"; //$NON-NLS-1$
						tabName += ((IExtendedMemoryBlock)newMemory).getBigBaseAddress().toString(16);
					}
				}
				else
				{
					long address = newMemory.getStartAddress();
					tabName = Long.toHexString(address);
				}
			} catch (DebugException e) {
				tabName = DebugUIMessages.getString(UNKNOWN);					
				DebugUIPlugin.log(e.getStatus());
			}
			
			String preName = MemoryBlockManager.getMemoryRenderingManager().getRenderingInfo(getRenderingId()).getName();
			
			if (preName != null)
				tabName += " <" + preName + ">"; //$NON-NLS-1$ //$NON-NLS-2$

		}
		fTabItem.setText(tabName);	
	}


	/**
	 * Create actions for the view tab
	 */
	protected void createActions() {
		fCopyToClipboardAction = new CopyViewTabToClipboardContextAction(this);
		fGoToAddressAction = new GoToAddressAction(this);
		fResetMemoryBlockAction = new ResetMemoryBlockContextAction(this);
		fPrintViewTabAction = new PrintViewTabContextAction(this);
		
		fFormatColumnActions = new Action[6];
		fFormatColumnActions[0] =  new FormatColumnAction(1, this);
		fFormatColumnActions[1] =  new FormatColumnAction(2, this);
		fFormatColumnActions[2] =  new FormatColumnAction(4, this);
		fFormatColumnActions[3] =  new FormatColumnAction(8, this);
		fFormatColumnActions[4] =  new FormatColumnAction(16, this);
		fFormatColumnActions[5] =  new SetColumnSizeDefaultAction(this);
		
		fReformatAction = new ReformatAction(this);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {
	
		menu.add(new Separator(MEMORY_ACTIONS_GROUP));
		menu.add(fResetMemoryBlockAction);
		menu.add(fGoToAddressAction);
	
		menu.add(new Separator());
		
		if (fFormatColumnActions.length > 0)
		{
			// Format view tab actions
			IMenuManager formatMenu = new MenuManager(DebugUIMessages.getString(COLUMN_SIZE), 
				MEMORY_ACTIONS_FORMAT_GROUP);
				
			menu.appendToGroup(MEMORY_ACTIONS_GROUP, formatMenu);
		
			for (int i=0; i<fFormatColumnActions.length; i++)
			{
				formatMenu.add(fFormatColumnActions[i]);	
		
				// add check mark to the action to reflect current format of the view tab
				if (fFormatColumnActions[i] instanceof FormatColumnAction)
				{
					if (((FormatColumnAction)fFormatColumnActions[i]).getColumnSize() == getColumnSize())
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
		menu.add(fCopyToClipboardAction);
		menu.add(fPrintViewTabAction);
	}

	private Control createFolderPage(AbstractMemoryRenderer renderer) {
		
		contentProvider = new MemoryViewContentProvider(fMemoryBlock, fTabItem);
		fTableViewer= new TableViewer(fTabItem.getParent(),  SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.HIDE_SELECTION | SWT.BORDER);
		fTableViewer.setContentProvider(contentProvider);		
		
		if (renderer != null)
		{	
			renderer.setRenderingId(getRenderingId());
			MemoryViewTabLabelProvider labelProvider = new MemoryViewTabLabelProvider(this, renderer);
			fTableViewer.setLabelProvider(labelProvider);
			((AbstractTableViewTabLabelProvider)labelProvider).setViewTab(this);
		}
		else
		{	
			renderer = new EmptyRenderer();
			renderer.setRenderingId(getRenderingId());
			renderer.setViewTab(this);

			MemoryViewTabLabelProvider labelProvider = new MemoryViewTabLabelProvider(this, renderer);
			fTableViewer.setLabelProvider(labelProvider);

			DebugUIPlugin.log(DebugUIPlugin.newErrorStatus("Renderer property is not defined for: " + getRenderingId(), null)); //$NON-NLS-1$
		}
		
		contentProvider.setViewer(fTableViewer);
		
		ScrollBar scroll = ((Table)fTableViewer.getControl()).getVerticalBar();
		scroll.addSelectionListener(this);
		scroll.setMinimum(-100);
		scroll.setMaximum(200);
		
		fTableViewer.getControl().addControlListener(this);
		fTableViewer.getControl().addKeyListener(this);	

		fTableViewer.getTable().setHeaderVisible(true);
		fTableViewer.getTable().setLinesVisible(true);
		
		int bytePerLine = IInternalDebugUIConstants.BYTES_PER_LINE;
		
		// get default column size from preference store
		IPreferenceStore prefStore = DebugUIPlugin.getDefault().getPreferenceStore();
		int columnSize = prefStore.getInt(IDebugPreferenceConstants.PREF_COLUMN_SIZE);
		
		// check synchronized col size
		Integer colSize = (Integer)getSynchronizedProperty(IMemoryViewConstants.PROPERTY_COL_SIZE);
		
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
			DebugException e = new DebugException(DebugUIPlugin.newErrorStatus(DebugUIMessages.getString(FORMAT_IS_INVALID), null));
			displayError(e);
			return fTextViewer.getControl();
		}
		
		fTableViewer.setInput(fMemoryBlock);
		
		fCellModifier = new MemoryViewCellModifier(this);
		fTableViewer.setCellModifier(fCellModifier);

		// set to a non-proportional font
		fTableViewer.getTable().setFont(JFaceResources.getFont(IInternalDebugUIConstants.FONT_NAME));
		
		int row = 0;
		int col = 1;
		
		// set up cursor manager
		// manager sets up initial position of the cursor
		fCursorManager = new ViewTabCursorManager(this, row, col, fMenuMgr);
		
		if (fMemoryBlock instanceof IExtendedMemoryBlock)
		{
			BigInteger address = ((IExtendedMemoryBlock)fMemoryBlock).getBigBaseAddress();
			
			if (address == null)
			{
				address = new BigInteger("0"); //$NON-NLS-1$
			}
			
			BigInteger syncAddress = (BigInteger)getSynchronizedProperty(IMemoryViewConstants.PROPERTY_SELECTED_ADDRESS);
			
//			set initial selected address
			if (syncAddress != null)
			{
				setSelectedAddress(syncAddress, false);
			}
			else
			{
				setSelectedAddress(address, true);
			}
			updateCursorPosition();
		}
		else
		{
			long address = fMemoryBlock.getStartAddress();	
			BigInteger syncAddress = (BigInteger)getSynchronizedProperty(IMemoryViewConstants.PROPERTY_SELECTED_ADDRESS);
			
			if (syncAddress != null)
			{
//				set initial selected address
				setSelectedAddress(syncAddress, false);
			}
			else
			{
				setSelectedAddress(BigInteger.valueOf(address), true);
			}
			updateCursorPosition();
		}
		
		// add font change listener and update font when the font has been changed
		fFontChangeListener = new FontChangeListener();
		JFaceResources.getFontRegistry().addListener(fFontChangeListener);

		// finish initialization and return text viewer as the control
		if (errorOccurred)
		{
			return fTextViewer.getControl();
		}
		
		return fTableViewer.getControl();
	}
	

	/**
	 * Format view tab based on parameters.
	 * @param bytesPerLine - number of bytes per line, possible values: 16
	 * @param columnSize - number of bytes per column, possible values: 1, 2, 4, 8
	 * @return true if format is successful, false, otherwise
	 */
	public boolean format(int bytesPerLine, int columnSize)
	{			
		// check parameter, bytesPerLine be 16
		if (bytesPerLine != IInternalDebugUIConstants.BYTES_PER_LINE)
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
		
		// if the tab is already created and is being reformated
		if (fTabCreated)
		{
			getTopVisibleAddress();
			
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
		column0.setText(DebugUIMessages.getString(ADDRESS));
		
		// create new byte columns
		TableColumn [] byteColumns = new TableColumn[bytesPerLine/columnSize];		
		
		String[] columnLabels = new String[0];
		if (getMemoryBlockPresentation() != null)
			columnLabels = getMemoryBlockPresentation().getColumnLabels(getMemoryBlock(), bytesPerLine, columnSize);
		
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
				if (getColumnSize() >= 4)
				{
					column.setText(Integer.toHexString(i*columnSize).toUpperCase() + 
						" - " + Integer.toHexString(i*columnSize+columnSize-1).toUpperCase()); //$NON-NLS-1$
				}
				else
				{
					column.setText(Integer.toHexString(i*columnSize).toUpperCase());
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
		columnProperties[0] = MemoryViewLine.P_ADDRESS;

		// use column beginning offset to the row address as properties
		for (int i=1; i<columnProperties.length-1; i++)
		{
			columnProperties[i] = Integer.toHexString((i-1)*columnSize);
		}
		
		// Empty column for cursor navigation
		columnProperties[columnProperties.length-1] = " "; //$NON-NLS-1$
		
		fTableViewer.setColumnProperties(columnProperties);		
		
		// create and set cell editors
		fTableViewer.setCellEditors(getCellEditors());	
		
		if (fTabCreated)
		{
			refreshTableViewer();	

			// after refresh, make sure cursor position is up-to-date
			if (isAddressVisible(fSelectedAddress))
				updateCursorPosition();
		}
		
		packColumns();
		
		updateSyncColSize();
		
		return true;
	}
	
	/**
	 * 
	 */
	private void refreshTableViewer() {
		
		int i = fTableViewer.getTable().getTopIndex();
		
		// refresh if the view is already created
		fTableViewer.refresh();
		
		// if top index has changed, restore it
		if (i != fTableViewer.getTable().getTopIndex())
			fTableViewer.getTable().setTopIndex(i);
	}

	private void setColumnHeadings()
	{
		String[] columnLabels = new String[0];

		if (getMemoryBlockPresentation() != null)
			columnLabels = getMemoryBlockPresentation().getColumnLabels(getMemoryBlock(), fBytePerLine, fColumnSize);		
		
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
					columns[i].setText(Integer.toHexString(i*fColumnSize).toUpperCase() + 
							" - " + Integer.toHexString(i*fColumnSize+fColumnSize-1).toUpperCase()); //$NON-NLS-1$
				}
				else
				{
					columns[i].setText(Integer.toHexString(i*fColumnSize).toUpperCase());
				}
			}
		}
	}
	
	/**
	 * Resize column to the preferred size
	 */
	public void packColumns() {
		// pack columns
		Table table = fTableViewer.getTable();
		TableColumn[] columns = table.getColumns();
		
		for (int i=0 ;i<columns.length-1; i++)
		{	
			columns[i].pack();
		}
		
		if (fCursorManager != null)
		{
			if (isAddressVisible(fSelectedAddress))
				fCursorManager.redrawCursors();
		}
	}

	/**
	 * @return tab item for the view tab
	 */
	protected TabItem getTab()
	{
		return fTabItem;
	}
	
	/**
	 * Force focus on th ecursor if the selected address is not out of range
	 * Cursor cannot be shown if it's out of range.  Otherwise, it messes up
	 * the top index of the table and affects scrolling.
	 */
	protected void setCursorFocus()
	{
		if (!isAddressOutOfRange(fSelectedAddress) && fCursorManager != null)
			fCursorManager.setCursorFocus();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#getMemoryBlock()
	 */
	public IMemoryBlock getMemoryBlock()
	{
		IMemoryBlock mem = fMemoryBlock;
		return mem;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		try {
			fIsDisposed = true;
			
			// clean up listeners
			if (fTableViewer != null)
			{
				fTableViewer.getControl().removeControlListener(this);
				fTableViewer.getControl().removeKeyListener(this);
				fTableViewer.getTable().removeSelectionListener(this);
			}
			
			if (contentProvider != null)
				contentProvider.dispose();
			
			ScrollBar scroll = ((Table)fTableViewer.getControl()).getVerticalBar();
			
			if (scroll != null)
				scroll.removeSelectionListener(this);
			
			// dispose cursor
			if (fCursorManager != null)
				fCursorManager.dispose();

			// remove selection listener for tab folder
			fTabItem.removeDisposeListener(fTabFolderDisposeListener);
			fTabItem.dispose();
			
			fTextViewer = null;
			fTableViewer = null;
			
			// clean up cell editors
			for (int i=0; i<fEditors.length; i++)
			{
				fEditors[i].dispose();
			}
			
			// remove font change listener when the view tab is disposed
			JFaceResources.getFontRegistry().removeListener(fFontChangeListener);
			
			// remove the view tab from the synchronizer
			getMemoryBlockViewSynchronizer().removeView(this);
			
			super.dispose();

		} catch (Exception e) {}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent event) {

		if (event.getSource() instanceof ScrollBar)
		{
			handleScrollBarSelection(event);
		}
	}
	
	/**
	 * Based on cursor position, update table selection.
	 * If a lead cursor is not available, the cursor is not visible.
	 * Update will not be performed if the cursor is not visible.
	 */
	protected void updateTableSelection()
	{
		// do not update selection if address is out of range
		// otherwise, screws up top index
		if (isAddressOutOfRange(fSelectedAddress))
			return;
		
		int index = findAddressIndex(getTopVisibleAddress());

		// update table selection
		fTableViewer.getTable().setSelection(fCursorManager.fRow);
		
		// if top index has changed, restore
		if (fTableViewer.getTable().getTopIndex() != index)
			fTableViewer.getTable().setTopIndex(index);
	}
	
	/**
	 * Calculate and set selected address based on provided row and column
	 */
	protected void updateSelectedAddress(TableItem row, int col)
	{
	
		// get row address
		String temp = ((MemoryViewLine)row.getData()).getAddress();
		BigInteger rowAddress = new BigInteger(temp, 16);
		
		int offset;
		if (col > 0)
		{	
			// 	get address offset
			offset = (col-1) * getColumnSize();
		}
		else
		{
			offset = 0;
		}
		
		// update selected address
		setSelectedAddress(rowAddress.add(BigInteger.valueOf(offset)), true);		
	}


	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		 
		
	}


	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
	 */
	public void controlMoved(ControlEvent e) {
		 
		
	}


	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
	 */
	public void controlResized(ControlEvent e) {
		//this method gets called many times as the user drags the window to a new size
		//TODO: only refresh the data at the end of the resize, if possible
		
		// do not handle resize if the tab is not yet created completely
		if (fTabCreated)		
			resizeTable();
		
	}
	
	/**
	 * Handles key events in viewer.
	 */
	protected void handleKeyPressed(KeyEvent evt) {

		final KeyEvent event = evt;
		
		// Must run on UI Thread asynchronously
		// Otherwise, another event could have been recevied before the reload is completed
		Display.getDefault().syncExec(new Runnable()
		{			
			public void run()
			{
				if (event.stateMask != 0)
				{
					return;
				}
				
				if (event.getSource() instanceof Text)
						return;
				
				// allow edit if user hits return
				if (event.character == '\r' && event.getSource() instanceof TableCursor)
				{
					fCursorManager.activateCellEditor(null);
					return;
				}
				
				try
				{	
					switch (event.keyCode)
					{	
						case SWT.HOME :
						case SWT.PAGE_UP :
						case SWT.ARROW_UP :
						case SWT.ARROW_LEFT:
						case SWT.END :
						case SWT.PAGE_DOWN :
						case SWT.ARROW_DOWN :
						case SWT.ARROW_RIGHT:
							// If blocking an extended memory block,
							// check to see if additional memory needs to be obtained.
							if (fMemoryBlock instanceof IExtendedMemoryBlock)
							{
								// User could have used scroll bar to scroll away
								// from the highlighted address.
								// When user hits arrow keys or page up/down keys
								// we should go back to the selected address and moves the cursor
								// based on the key pressed.
								if (isAddressOutOfRange(fSelectedAddress))
								{
									reloadTable(fSelectedAddress, false);
									
									updateSyncTopAddress(true);
									updateSyncSelectedAddress(true);
									
									fCursorManager.setCursorFocus();
									break;
								}
								//if we are approaching the limits of the currently loaded memory, reload the table
								if (needMoreLines())
								{
									BigInteger topAddress = getTopVisibleAddress();
									//if we're near 0, just go there immediately (hard stop at 0, don't try to scroll/wrap)
									if (topAddress.compareTo(BigInteger.valueOf(96)) <= 0)
									{
										if (topAddress.equals(BigInteger.valueOf(0)))
										{
											// do not reload if we are already at zero
											break;
										}
										reloadTable(BigInteger.valueOf(0), false);
										fCursorManager.setCursorFocus();
									}
									else
									{
										//otherwise, just load the next portion of the memory
										reloadTable(topAddress, false);
										fCursorManager.setCursorFocus();
									}
								}
								else if (!isAddressVisible(fSelectedAddress))
								{
									// address is in range, but not visible
									// just go to the address and make sure
									// that the cursor is in focus
									
									goToAddress(fSelectedAddress);
									fCursorManager.setCursorFocus();
									updateSyncTopAddress(true);
									
								}
								else
								{
									// in place of the commented lines
									updateCursorPosition();
									fCursorManager.setCursorFocus();
									// since cursor is going to be visible
									// synchronization event will be fired by the cursor
									// when it is selected
								}
						}

							break;
						default :
							
							// if it's a valid key for edit
							if (isValidEditEvent(event.keyCode))
							{	
								// activate edit as soon as user types something at the cursor
								if (event.getSource() instanceof TableCursor)
								{
									String initialValue = String.valueOf(event.character);
									fCursorManager.activateCellEditor(initialValue);
								}
							}
							break;									
					}
				}
				catch (DebugException e)
				{
					displayError(e);
					DebugUIPlugin.log(e.getStatus());
				}
			}
		});
	}
		
	/**
	 * @return top visible address of this view tab
	 */
	public BigInteger getTopVisibleAddress() {
		
		if (fTableViewer == null)
			return BigInteger.valueOf(0);

		Table table = fTableViewer.getTable();
		int topIndex = table.getTopIndex();

		if (topIndex < 1) { topIndex = 0; }

		if (table.getItemCount() > topIndex) 
		{
			MemoryViewLine topItem = (MemoryViewLine)table.getItem(topIndex).getData();
			
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

	/**
	 * Reload table at the topAddress.
	 * Delta will be re-computed if updateDelta is true.
	 * @param topAddress
	 * @param updateDelta
	 * @throws DebugException
	 */
	synchronized protected void reloadTable(BigInteger topAddress, boolean updateDelta) throws DebugException{
		
		if (fTableViewer == null)
			return;
			
		Table table = (Table)fTableViewer.getControl();	

		// Calculate top buffer address
		// This is where we will start asking for memory from debug adapter.
		BigInteger topBufferAddress = topAddress;
		if (topBufferAddress.compareTo(BigInteger.valueOf(32)) <= 0) {
			TABLE_PREBUFFER = 0;
		} else {
			TABLE_PREBUFFER = topBufferAddress.divide(BigInteger.valueOf(32)).min(BigInteger.valueOf(TABLE_DEFAULTBUFFER)).intValue();
		}

		topBufferAddress = topAddress.subtract(BigInteger.valueOf(getBytesPerLine()*TABLE_PREBUFFER));

		// calculate number of lines needed
		long numLines = 0;
		if (fMemoryBlock instanceof IExtendedMemoryBlock)
		{
			// number of lines is number of visible lines + buffered lines
			numLines = getNumberOfVisibleLines()+TABLE_PREBUFFER+TABLE_POSTBUFFER;
		}


		// tell content provider to get memory and refresh
		contentProvider.getMemoryToFitTable(topBufferAddress, numLines, updateDelta);
		contentProvider.forceRefresh();
		

		if (fMemoryBlock instanceof IExtendedMemoryBlock)
		{
			int topIdx = findAddressIndex(topAddress);
			
			if (topIdx != -1)
			{
				table.setTopIndex(topIdx);
			}
			
			// TODO:  Revisit this part again
			// if allow cursor update when the cursor is
			// not visible, causes flashing on the screen
			// if not updated... then cursor may not
			// show properly (table selection not hidden)
			// if selected address is not out of range
			// restore cursor
			if (isAddressVisible(fSelectedAddress) && findAddressIndex(fSelectedAddress) != -1)
			{
				getTopVisibleAddress();
				getTopVisibleAddress().add(BigInteger.valueOf(getBytesPerLine()*getNumberOfVisibleLines()));

				// if the cursor is not visible but in buffered range
				// updating and showing the cursor will move the top index of the table
				updateCursorPosition();
				
				int newIdx = findAddressIndex(getTopVisibleAddress());
				
				if (newIdx != topIdx  && topIdx != -1)
				{	
					table.setTopIndex(topIdx);
				}
							
				if (isAddressVisible(fSelectedAddress))
				{
					fCursorManager.showCursor();	
				}
				else
				{
					fCursorManager.hideCursor();
				}
			}
			else
			{
				fCursorManager.hideCursor();
			}
		}		
		
		// try to display the table every time it's reloaded
		displayTable();
	}
	
	private int findAddressIndex(BigInteger address)
	{
		TableItem items[] = fTableViewer.getTable().getItems();
	
		for (int i=0; i<items.length; i++){
			
			// Again, when the table resizes, the table may have a null item
			// at then end.  This is to handle that.
			if (items[i] != null)
			{	
				MemoryViewLine line = (MemoryViewLine)items[i].getData();
				BigInteger lineAddress = new BigInteger(line.getAddress(), 16);
				BigInteger endLineAddress = lineAddress.add(BigInteger.valueOf(getBytesPerLine()));
				
				if (lineAddress.compareTo(address) <= 0 && endLineAddress.compareTo(address) > 0)
				{	
					return i;
				}
			}
		}
		
		return -1;
	}
	
	/**
	 * Update cursor position based on selected address.
	 * @return true if cursor is visible, false otherwise
	 */
	private boolean updateCursorPosition()
	{			
		// selected address is out of range, simply return false
		if (fSelectedAddress.compareTo(contentProvider.getBufferTopAddress()) < 0)
			return false;
		
		// calculate selected row address
		int numOfRows = fSelectedAddress.subtract(contentProvider.getBufferTopAddress()).intValue()/getBytesPerLine();
		BigInteger rowAddress = contentProvider.getBufferTopAddress().add(BigInteger.valueOf(numOfRows * getBytesPerLine()));

		// try to find the row of the selected address
		int row = findAddressIndex(fSelectedAddress);
			
		if (row == -1)
		{
			return false;
		}
		
		// calculate offset to the row address
		BigInteger offset = fSelectedAddress.subtract(rowAddress);
		
		// locate column
		int col = ((offset.intValue()/getColumnSize())+1);
		
		// setting cursor selection or table selection changes
		// the top index of the table... and may mess up top index in the talbe
		// save up old top index
		int oldTop = fTableViewer.getTable().getTopIndex();
		
		// update cursor position and table selection
		fCursorManager.updateCursorPosition(row, col, isAddressVisible(fSelectedAddress));
		updateTableSelection();

		// reset top index to make sure the table is not moved
		fTableViewer.getTable().setTopIndex(oldTop);
		
		if (isAddressVisible(fSelectedAddress))
		{	
			fCursorManager.showCursor();
			fTableViewer.getTable().deselectAll();
		}
		else
			fCursorManager.hideCursor();
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ITableMemoryViewTab#getNumberOfVisibleLines()
	 */
	public int getNumberOfVisibleLines()
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
		int lineHeight = table.getItemHeight();
		
		int numberOfLines = height/lineHeight;
	
		return numberOfLines;		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#refresh()
	 */
	public void refresh()
	{
		try {
			
			// refresh at start address of this memory block
			// address may change if expression is evaluated to a different value
			IMemoryBlock mem = fMemoryBlock;
			BigInteger address;
			
			if (mem instanceof IExtendedMemoryBlock)
			{
				address = ((IExtendedMemoryBlock)mem).getBigBaseAddress();
				
				if (address == null)
				{	
					DebugException e = new DebugException(DebugUIPlugin.newErrorStatus(DebugUIMessages.getString(UNABLE_TO_GET_BASE_ADDRESS), null));
					displayError(e);
					return;
				}
				
				setTabName(mem, true);
				
				// base address has changed
				if (address.compareTo(contentProvider.getContentBaseAddress()) != 0)
				{
					// get to new address
					reloadTable(address, true);
				}
				else
				{
					// reload at top of table
//					address = contentProvider.getBufferTopAddress().add(BigInteger.valueOf(getBytesPerLine()*TABLE_PREBUFFER));
					address = getTopVisibleAddress();
					reloadTable(address, true);
				}				
			}
			else
			{
				address = BigInteger.valueOf(mem.getStartAddress());
				reloadTable(address, true);
			}
			
			if (isAddressVisible(fSelectedAddress))
			{	
				// redraw cursors if cursor is visible					
				getCursorManager().redrawCursors();
			}
					
		} catch (DebugException e) {
			displayError(e);
			DebugUIPlugin.log(e.getStatus());
		}
	}
	
	/**
	 * Handle resize of the table.
	 */
	private void resizeTable() {
		
		if (!(fMemoryBlock instanceof IExtendedMemoryBlock))
			return;
		
		if (!isEnabled())
			return;
			
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				boolean reloaded = false;
				
				// this code is is running on the UI thread with a delay
				// The view tab may have been disposed when this actually gets executed.
				if (fTableViewer == null)
					return;
				
				Table table = fTableViewer.getTable();
				
				// make sure table is still valid
				if (table.isDisposed())
					return;
				
				int topIndex = table.getTopIndex();
				if (topIndex < 0)
				{
					return;
				}
				BigInteger oldTopAddress = getTopVisibleAddress();
				if (oldTopAddress.compareTo(BigInteger.valueOf(32)) <= 0)
				{
					TABLE_PREBUFFER = 0;
				}
				else
				{
					TABLE_PREBUFFER =
						oldTopAddress.divide(BigInteger.valueOf(32)).min(BigInteger.valueOf(TABLE_DEFAULTBUFFER)).intValue();
				}
				
				// check pre-condition before we can check on number of lines left in the table
				if (table.getItemCount() > topIndex)
				{
					try
					{
						//if new window size exceeds the number of lines available in the table, reload the table
						if (needMoreLines())
						{
							reloadTable(oldTopAddress, false);
							reloaded = true;
						}
						if (oldTopAddress.compareTo(BigInteger.valueOf(96)) <= 0)
						{
							reloadTable(BigInteger.valueOf(0), false);
							reloaded = true;
						}
					}
					catch (DebugException e)
					{
						displayError(e);
						DebugUIPlugin.log(e.getStatus());
					}
				}
				
				if (!reloaded){
					// if not reload, still need to update the cursor position
					// since the position may change
					
					updateCursorPosition();
					fTableViewer.getTable().deselectAll();
					
					if (!getTopVisibleAddress().equals(oldTopAddress))
					{	
						int i = findAddressIndex(oldTopAddress);
						
						if (i != -1)
							fTableViewer.getTable().setTopIndex(i);
					}
				}
				
				updateSyncTopAddress(true);
			}
		});
	}
	
	/**
	 * Handle scrollling and reload table if necessary
	 * @param event
	 */
	private void handleScrollBarSelection(SelectionEvent event)
	{	
		if (!(fMemoryBlock instanceof IExtendedMemoryBlock))
		{
			// if not instance of extended memory block
			// just get current top visible address and fire event
			
			updateSyncTopAddress(true);
	
		}			
		
		final SelectionEvent evt = event;
		
		// Must run on UI Thread asynchronously
		// Otherwise, another event could have been recevied before the reload is completed
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				try
				{	
					switch (evt.detail)
					{
						case 0 : //the end of a drag
						case SWT.END :
						case SWT.PAGE_DOWN :
						case SWT.ARROW_DOWN :
						case SWT.HOME :
						case SWT.PAGE_UP :
						case SWT.ARROW_UP :
							if (fMemoryBlock instanceof IExtendedMemoryBlock)
							{
								updateSyncTopAddress(true);
								//if we are approaching the limits of the currently loaded memory, reload the table
								if (needMoreLines())
								{
									BigInteger topAddress = getTopVisibleAddress();
									//if we're near 0, just go there immediately (hard stop at 0, don't try to scroll/wrap)
									if (topAddress.compareTo(BigInteger.valueOf(96)) <= 0)
									{
										if (topAddress.equals(BigInteger.valueOf(0)))
										{
											// do not reload if we are already at zero
											break;
										}
										reloadTable(BigInteger.valueOf(0), false);
									}
									else
									{

										//otherwise, just load the next portion of the memory
										reloadTable(topAddress, false);
									}
								}
							}
							if (isAddressVisible(fSelectedAddress))
							{
								updateCursorPosition();
								fCursorManager.setCursorFocus();
							}
							break;
						default:
							break;
					}
				}
				catch (DebugException e)
				{
					displayError(e);
					DebugUIPlugin.log(e.getStatus());
				}
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#setEnabled(boolean)
	 */
	public void setEnabled(boolean enable)
	{	
		super.setEnabled(enable);
		
		fEnabled = enable;
		IMemoryBlock mem = fMemoryBlock;
		
		if (fEnabled)
		{
			BigInteger oldBase = contentProvider.getContentBaseAddress();

			// debug adapter may ignore the enable request
			// some adapter does not block memory and may not do anything
			// with the enable/disable request
			// As a result, we need to force a refresh
			// and to make sure content is updated
			refresh();
			
			if (mem instanceof IExtendedMemoryBlock)
			{
				BigInteger baseAddress = ((IExtendedMemoryBlock)mem).getBigBaseAddress();
				
				if (baseAddress == null)
				{
					if (fSelectedAddress != null)
						baseAddress = fSelectedAddress;
					else
						baseAddress = new BigInteger("0"); //$NON-NLS-1$
				}
				
				ArrayList references = (ArrayList)getSynchronizedProperty(IMemoryViewConstants.PROPERTY_ENABLED_REFERENCES);				
				
				// if the base address has changed, update cursor
				// and this is the first time this memory block is enabled
				if (!baseAddress.equals(oldBase) && references.size() == 1)
				{
					setSelectedAddress(baseAddress, true);
					updateCursorPosition();
					
					updateSyncTopAddress(true);
					updateSyncSelectedAddress(true);
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
		}
		else
		{
			if (mem instanceof IExtendedMemoryBlock)
			{	
				setTabName(mem, false);
			}
				
			// once the view tab is disabled, all deltas information becomes invalid.
			// reset changed information and recompute if data has really changed when
			// user revisits the same tab.	
			contentProvider.resetDeltas();
		}
	}
	public boolean isEnabled()
	{
		return fEnabled;
	}
	
	/**
	 * Display an error in the view tab.
	 * Make use of the text viewer instead of the table viewer.
	 * @param e
	 */
	protected void displayError(DebugException e)
	{
		StyledText styleText = null;
		errorOccurred = true;

		if (fTextViewer == null)
		{
			// create text viewer
			fTextViewer = new TextViewer(fTabItem.getParent(), SWT.NONE);	
			fTabItem.setControl(fTextViewer.getControl());
			fTextViewer.setDocument(new Document());
			styleText = fTextViewer.getTextWidget();
			styleText.setEditable(false);
			styleText.setEnabled(false);
		}
		else if (fTextViewer.getControl() != fTabItem.getControl())
		{	
			// switch to text viewer
			fTabItem.setControl(fTextViewer.getControl());
		}
		
		styleText = fTextViewer.getTextWidget();
		
		if (styleText != null)
			styleText.setText(DebugUIMessages.getString(ERROR) + e);	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#isDisplayingError()
	 */
	public boolean isDisplayingError()
	{	
		if(fTextViewer == null)
			return false;
		
		if (fTabItem.getControl() == fTextViewer.getControl()) {
			return true;
		}
		return false;
	}
	
	public void displayTable()
	{
		
		if (fTableViewer!= null && fTabItem.getControl() != fTableViewer.getControl())
		{	
			errorOccurred = false;
			fTabItem.setControl(fTableViewer.getControl());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent e)
	{
		handleKeyPressed(e);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyReleased(KeyEvent e)
	{	
	}

	/**
	 * @return cell editors for the table
	 */
	private CellEditor[] getCellEditors() {
		Table table = fTableViewer.getTable();
		fEditors = new CellEditor[table.getColumnCount()];
		
		for (int i=0; i<fEditors.length; i++)
		{
			fEditors[i] = new TextCellEditor(table);
		}
		
		// combine the listener/validator interfaces so we can handle
		// "editing" an address, which really skips the table to that address
//		class CellValidatorListener implements  ICellEditorValidator {
//			TextCellEditor textEditor;
//			boolean isAddressValidator;
//
//			public CellValidatorListener(CellEditor cellEditor, boolean isAddress) {
//				textEditor = (TextCellEditor)cellEditor;
//				isAddressValidator = isAddress;
//			}
//
//			public String isValid(Object value) {
//				if ((value instanceof String)) {
//					if (((String)value).length() == 0)
//						return null;  //allow empty strings so hitting "delete" doesn't immediately pop up an error
//
//					// make sure the character is 0-9ABCDEF only
//					try {
//						if (!isAddressValidator) {	// don't validate address to allow input of variable in the address field
//							BigInteger bigInt = new BigInteger((String)value, 16);
//						}
//					} catch (NumberFormatException ne) {
//						return "not valid";
//					}
//				}
//				return null;
//			}
//
//		}
//
//		//"editing" an address skips the table to that address
//		
//		for (int i=0; i<fEditors.length; i++)
//		{
//			fEditors[i].setValidator(new CellValidatorListener(fEditors[i], true));
//		}
		
		return fEditors;
	}	

	public TableViewer getTableViewer()
	{
		return fTableViewer;
	}
	
	protected ViewTabCursorManager getCursorManager()
	{
		return fCursorManager; 
	}
	
	/**
	 * This function must be made synchronized.
	 * Otherwise, another thread could modify the selected address while it is being updated.
	 * It is the case when user scrolls up/down the table using arrow key.
	 * When the table reaches its limit, it is being reloaded.  However, cursor receives a selection
	 * event and update the selected address at the same time.  It messes up the selected address
	 * and causes cursor selection to behave unexpectedly.
	 * @param address
	 */
	synchronized protected void setSelectedAddress(BigInteger address, boolean updateSynchronizer)
	{
		fSelectedAddress = address;
		
		updateSyncSelectedAddress(updateSynchronizer);
	}

	/**
	 * Return the offset from the base address of the memory block.
	 * @param memory
	 * @param lineAddress
	 * @param lineOffset
	 * @return
	 * TODO: this method is never called
	 */
	protected long getOffset(IMemoryBlock memory, String lineAddress, int lineOffset) {
		
		BigInteger lineAddr = new BigInteger(lineAddress, 16);
		BigInteger memoryAddr;
		
		if (memory instanceof IExtendedMemoryBlock)
		{
			memoryAddr = ((IExtendedMemoryBlock)memory).getBigBaseAddress();
		}
		else
		{
			memoryAddr = BigInteger.valueOf(memory.getStartAddress());
		}
		
		if (memoryAddr == null)
			memoryAddr = new BigInteger("0"); //$NON-NLS-1$
		
		long offset = lineAddr.subtract(memoryAddr).longValue();
		
		return offset + lineOffset;
	}
	
	/**
	 * Reset this view tab to the base address of the memory block
	 */
	public void resetAtBaseAddress() throws DebugException
	{
		try
		{	
			IMemoryBlock mem = getMemoryBlock();
			if (mem instanceof IExtendedMemoryBlock)
			{
				// if text editor is activated, removes its focus and commit
				// any changes made
				setCursorFocus();
					
				// reload table at base address	
				BigInteger address = ((IExtendedMemoryBlock)mem).getBigBaseAddress();
				
				if (address == null)
				{
					// unable to get the base address
					// pop up error message an do nothing
					Shell shell = DebugUIPlugin.getShell();
					MessageDialog.openError(shell, DebugUIMessages.getString("DebugUITools.Error_1"), DebugUIMessages.getString(UNABLE_TO_GET_BASE_ADDRESS)); //$NON-NLS-1$
					return;
				}
				
				setSelectedAddress(address, true);
				reloadTable(address, false);		
				
				// make sure cursor has focus when the user chooses to reset
				setCursorFocus();
			}
			else
			{
				// go to top of the table 
				BigInteger address = BigInteger.valueOf(mem.getStartAddress());
				setSelectedAddress(address, true);
				getTableViewer().getTable().setTopIndex(0);
				updateCursorPosition();
				updateTableSelection();
				setCursorFocus();
			}
			
			updateSyncTopAddress(true);
		}
		catch (DebugException e)
		{
			throw e;
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#goToAddress(java.math.BigInteger)
	 */
	public void goToAddress(BigInteger address) throws DebugException
	{
		goToAddress(address, true);
		fCursorManager.setCursorFocus();
	}

	/**
	 * @param address
	 * @throws DebugException
	 */
	private void goToAddress(BigInteger address, boolean updateSynchronizer) throws DebugException {
		try
		{	
			// if address is within the range, highlight			
			if (!isAddressOutOfRange(address))
			{
				// Defer update so that top visible address is updated before
				// the selected address
				// This is to ensure that the other view tabs get the top
				// visible address change events first in case the selected
				// address is not already visible.
				// If this is not done, the other view tab may not show selected address.
				setSelectedAddress(address, false);
				updateCursorPosition();				
				updateTableSelection();
				
				// force the cursor to be shown
				if (!isAddressVisible(fSelectedAddress))
				{	
					int i = findAddressIndex(fSelectedAddress);
					
					fTableViewer.getTable().showItem(fTableViewer.getTable().getItem(i));
					getCursorManager().showCursor();
					
					updateSyncTopAddress(updateSynchronizer);
				}
				
				// update selected address in synchronizer
				updateSyncSelectedAddress(updateSynchronizer);
			}
			else
			{
				// if not extended memory block
				// do not allow user to go to an address that's out of range
				if (!(fMemoryBlock instanceof IExtendedMemoryBlock))
				{
					Status stat = new Status(
					 IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(),
					 DebugException.NOT_SUPPORTED, DebugUIMessages.getString(ADDRESS_IS_OUT_OF_RANGE), null 
					);
					DebugException e = new DebugException(stat);
					throw e;
				}
				
				setSelectedAddress(address, updateSynchronizer);
				
				//otherwise, reload at the address
				reloadTable(address, false);
				updateSyncTopAddress(updateSynchronizer);
			}
		}
		catch (DebugException e)
		{
			throw e;
		}
	}

	/**
	 * @return
	 */
	public int getColumnSize()
	{
		return fColumnSize;
	}

	/**
	 * @return
	 */
	public int getBytesPerLine()
	{
		return fBytePerLine;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#setFont(org.eclipse.swt.graphics.Font)
	 */
	public void setFont(Font font)
	{	
		int oldIdx = fTableViewer.getTable().getTopIndex();
		
		// BUG in table, if font is changed when table is not starting
		// from the top, causes table gridline to be misaligned.
		fTableViewer.getTable().setTopIndex(0);
		
		// set font
		fTableViewer.getTable().setFont(font);
		fCursorManager.setFont(font);
		
		fTableViewer.getTable().setTopIndex(oldIdx);
		
		packColumns();
		
		// update table cursor and force redraw
		updateCursorPosition();
	}
	
	/**
	 * @return memory block presentation to allow for customization
	 */
	protected IMemoryBlockModelPresentation getMemoryBlockPresentation()
	{
		// only try to create a model presentation once
		if (fMemoryBlockPresentation == null && !fNoPresentation)
		{
			//	create model presentation for memory block
			 DelegatingModelPresentation presentation = new MemoryViewDelegatingModelPresentation();
			 String id = fMemoryBlock.getModelIdentifier();
			 fMemoryBlockPresentation = (MemoryViewLazyModelPresentation)presentation.getPresentation(id);
			 
			 // if a memory block presentation cannot be retrieved
			 if (fMemoryBlockPresentation == null)
			 	fNoPresentation = true;
		}
		return fMemoryBlockPresentation; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#setTabLabel(java.lang.String)
	 */
	public void setTabLabel(String label)
	{
		if (label != null)
			fTabItem.setText(label);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#getTabLabel()
	 */
	public String getTabLabel()
	{
		if (fTabItem != null) {
			return fTabItem.getText();
		}
		return null;	
	}

	/**
	 * Handle column size changed event from synchronizer
	 * @param newColumnSize
	 */
	private void columnSizeChanged(final int newColumnSize)
	{	
//		ignore event if view tab is disabled	
		if (!isEnabled())
			return;
		
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				format(16, newColumnSize);				
			}
		});
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISynchronizedMemoryBlockView#scrollBarSelectionChanged(int)
	 */
	public void scrollBarSelectionChanged(int newSelection)
	{
		 
		
	}

	/**
	 * Handle selected address change event from synchronizer
	 * @param address
	 */
	private void selectedAddressChanged(final BigInteger address)
	{
		// ignore event if view tab is disabled
		if (!isEnabled())
		{
			return;
		}
		
		try
		{
			if (!fSelectedAddress.equals(address))
			{	
				if (getMemoryBlock() instanceof IExtendedMemoryBlock)
				{
					goToAddress(address, false);
				}
				else
				{
					if (!isAddressOutOfRange(address))
					{
						goToAddress(address, false);
					}
				}
			} 	
	
		}
		catch (DebugException e)
		{
			displayError(e);
		}
	}
	
	/**
	 * Handle top visible address change event from synchronizer
	 * @param address
	 */
	private void topVisibleAddressChanged(final BigInteger address)
	{
		try
		{
			// do not handle event if view tab is disabled
			if (!isEnabled())
				return;
			
			if (!address.equals(getTopVisibleAddress()))
			{
				if (getMemoryBlock() instanceof IExtendedMemoryBlock)
				{
				
					if (!isAddressOutOfRange(address))
					{
						int index = -1;
						// within buffer range, just set top index
						Table table = getTableViewer().getTable();
						for (int i = 0; i < table.getItemCount(); i++)
						{
							MemoryViewLine line = (MemoryViewLine) table.getItem(i).getData();
							if (line != null)
							{
								BigInteger lineAddress = new BigInteger(line.getAddress(), 16);
								if (lineAddress.equals(address))
								{
									index = i;
									break;
								}
							}
						}
						if (index >= 3 && table.getItemCount() - (index+getNumberOfVisibleLines()) >= 3)
						{
							// update cursor position
							table.setTopIndex(index);
							
							if (!isAddressVisible(fSelectedAddress))
							{
								fCursorManager.hideCursor();
							}
							else
							{
								updateCursorPosition();
								updateTableSelection();
								table.setTopIndex(index);
								
								// BUG 64831:  to get around SWT problem with
								// the table cursor not painted properly after
								// table.setTopIndex is called
								fCursorManager.getLeadCursor().setVisible(false);
								fCursorManager.getLeadCursor().setVisible(true);
							}
						}
						else
						{	
							// approaching limit, reload table
							reloadTable(address, false);	
						}
					}
					else
					{	
						// approaching limit, reload table
						reloadTable(address, false);
					}
				}
				else
				{
					// IMemoryBlock support
					int index = -1;
					// within buffer range, just set top index
					Table table = getTableViewer().getTable();
					for (int i = 0; i < table.getItemCount(); i++)
					{
						MemoryViewLine line = (MemoryViewLine) table.getItem(i).getData();
						if (line != null)
						{
							BigInteger lineAddress = new BigInteger(line.getAddress(), 16);
							if (lineAddress.equals(address))
							{
								index = i;
								break;
							}
						}
					}
					
					if (index >= 0)
					{
						table.setTopIndex(index);
								
						if (!isAddressVisible(fSelectedAddress))
						{
							fCursorManager.hideCursor();
						}
						else
						{
							updateCursorPosition();
							updateTableSelection();
							table.setTopIndex(index);
							
							// BUG 64831:  to get around SWT problem with
							// the table cursor not painted properly after
							// table.setTopIndex is called
							fCursorManager.getLeadCursor().setVisible(false);
							fCursorManager.getLeadCursor().setVisible(true);
						}
					}
				}
			}
		}
		catch (DebugException e)
		{
			displayError(e);
		}
	}
	
	/**
	 * Check if address provided is out of buffered range
	 * @param address
	 * @return
	 */
	private boolean isAddressOutOfRange(BigInteger address)
	{
		return contentProvider.isAddressOutOfRange(address);
	}
	
	/**
	 * Check if address is visible
	 * @param address
	 * @return
	 */
	protected boolean isAddressVisible(BigInteger address)
	{
		// if view tab is not yet created 
		// cursor should always be visible
		if (!fTabCreated)
			return true;
		
		BigInteger topVisible = getTopVisibleAddress();
		BigInteger lastVisible = getTopVisibleAddress().add(BigInteger.valueOf((getNumberOfVisibleLines()) * getBytesPerLine() + getBytesPerLine()));
		
		if (topVisible.compareTo(address) <= 0 && lastVisible.compareTo(address) > 0)
		{
			return true;
		}
		return false;
	}

	/**
	 * Get properties from synchronizer and synchronize settings
	 */
	private void synchronize()
	{
		Integer columnSize = (Integer) getSynchronizedProperty(IMemoryViewConstants.PROPERTY_COL_SIZE);
		BigInteger selectedAddress = (BigInteger)getSynchronizedProperty(IMemoryViewConstants.PROPERTY_SELECTED_ADDRESS);
		BigInteger topAddress = (BigInteger)getSynchronizedProperty(IMemoryViewConstants.PROPERTY_TOP_ADDRESS);
		
		if (columnSize != null)
		{
			int colSize = columnSize.intValue();	
			
			if (colSize > 0 && colSize != fColumnSize)
			{
				columnSizeChanged(colSize);
			}
		}
		
		if (topAddress != null)
		{
			if (!topAddress.equals(getTopVisibleAddress()))
			{
				if (!fSelectedAddress.equals(selectedAddress))
				{
					setSelectedAddress(selectedAddress, true);
				}
				
				topVisibleAddressChanged(topAddress);
			}
		}		
		
		if (selectedAddress != null)
		{
			if (selectedAddress.compareTo(fSelectedAddress) != 0)
			{
				selectedAddressChanged(selectedAddress);
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISynchronizedMemoryBlockView#propertyChanged(java.lang.String, java.lang.Object)
	 */
	public void propertyChanged(String propertyName, Object value)
	{	
		if (isDisplayingError())
			return;
		
		if (propertyName.equals(IMemoryViewConstants.PROPERTY_SELECTED_ADDRESS) && value instanceof BigInteger)
		{
			try {
				if (needMoreLines())
				{
					reloadTable(getTopVisibleAddress(), false);
				}
			} catch (DebugException e) {
				displayError(e);
			}
			
			selectedAddressChanged((BigInteger)value);
		}
		else if (propertyName.equals(IMemoryViewConstants.PROPERTY_COL_SIZE) && value instanceof Integer)
		{
			columnSizeChanged(((Integer)value).intValue());
		}
		else if (propertyName.equals(IMemoryViewConstants.PROPERTY_TOP_ADDRESS) && value instanceof BigInteger)
		{
			try {
				if (needMoreLines())
				{
					reloadTable(getTopVisibleAddress(), false);
				}
			} catch (DebugException e) {
				displayError(e);
			}
			topVisibleAddressChanged((BigInteger)value);
			return;
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISynchronizedMemoryBlockView#getProperty(java.lang.String)
	 */
	public Object getProperty(String propertyId)
	{
		if (propertyId.equals(IMemoryViewConstants.PROPERTY_SELECTED_ADDRESS))
		{
			return fSelectedAddress;
		}
		else if (propertyId.equals(IMemoryViewConstants.PROPERTY_COL_SIZE))
		{
			return new Integer(fColumnSize);
		}
		else if (propertyId.equals(IMemoryViewConstants.PROPERTY_TOP_ADDRESS))
		{
			return getTopVisibleAddress();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#getSelectedAddress()
	 */
	public BigInteger getSelectedAddress() {
		return fSelectedAddress;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#getSelectedContent()
	 */
	public String getSelectedContent() {

		// check precondition
		if (fCursorManager.fCol == 0 || fCursorManager.fCol > getBytesPerLine()/getColumnSize())
		{
			return ""; //$NON-NLS-1$
		}
				
		TableItem tableItem = getTableViewer().getTable().getItem(fCursorManager.fRow);
		
		return tableItem.getText(fCursorManager.fCol);	
	}
	
	/**
	 * Update labels in the view tab
	 */
	protected void updateLabels()
	{
		// update tab labels
		setTabName(getMemoryBlock(), true);
		
		if (fTableViewer != null)
		{
			// update column labels
			setColumnHeadings();
			
			refreshTableViewer();
		}
	}

	protected boolean needMoreLines()
	{
		if (getMemoryBlock() instanceof IExtendedMemoryBlock)
		{		
			Table table = fTableViewer.getTable();
			TableItem firstItem = table.getItem(0);
			TableItem lastItem = table.getItem(table.getItemCount()-1);
			
			if (firstItem == null || lastItem == null)
				return true;
			
			MemoryViewLine first = (MemoryViewLine)firstItem.getData();
			MemoryViewLine last = (MemoryViewLine) lastItem.getData();
			
			if (first == null ||last == null)
			{
				// For some reason, the table does not return the correct number
				// of table items in table.getItemCount(), causing last to be null.
				// This check is to ensure that we don't get a null pointer exception.
				return true;
			}
			
			BigInteger startAddress = new BigInteger(first.getAddress(), 16);
			BigInteger lastAddress = new BigInteger(last.getAddress(), 16);
			lastAddress = lastAddress.add(BigInteger.valueOf(getBytesPerLine()));
			
			BigInteger topVisibleAddress = getTopVisibleAddress();
			long numVisibleLines = getNumberOfVisibleLines();
			long numOfBytes = numVisibleLines * getBytesPerLine();
			
			BigInteger lastVisibleAddrss = topVisibleAddress.add(BigInteger.valueOf(numOfBytes));
			
			// if there are only 3 lines left at the top, refresh
			BigInteger numTopLine = topVisibleAddress.subtract(startAddress).divide(BigInteger.valueOf(getBytesPerLine()));
			if (numTopLine.compareTo(BigInteger.valueOf(3)) <= 0)
				return true;
			
			// if there are only 3 lines left at the bottom, refresh
			BigInteger numBottomLine = lastAddress.subtract(lastVisibleAddrss).divide(BigInteger.valueOf(getBytesPerLine()));
			if (numBottomLine.compareTo(BigInteger.valueOf(3)) <= 0)
			{
				return true;
			}
			
			return false;
		}
		
		return false;
	}
	
	private Object getSynchronizedProperty(String propertyId)
	{
		return getMemoryBlockViewSynchronizer().getSynchronizedProperty(getMemoryBlock(), propertyId);	
	}
	
	/**
	 * Checks to see if the event is valid for activating
	 * cell editing in a view tab
	 * @param event
	 * @return
	 */
	public boolean isValidEditEvent(int event) {
		for (int i = 0; i < MemoryViewTab.ignoreEvents.length; i++) {
			if (event == MemoryViewTab.ignoreEvents[i])
				return false;
		}
		return true;
	}
	
	private IMemoryBlockViewSynchronizer getMemoryBlockViewSynchronizer() {
		return DebugUIPlugin.getDefault().getMemoryBlockViewSynchronizer();
	}
}	

