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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.memory.IPersistableDebugElement;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.AbstractTableRendering;
import org.eclipse.debug.ui.memory.IMemoryBlockTablePresentation;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingSynchronizationService;
import org.eclipse.debug.ui.memory.IResettableMemoryRendering;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.progress.UIJob;

//TODO:  different representation in a rendering
//TODO:  allow IMemoryBlock to contribute decorations
//TODO:  show memory tab is busy updating
//TODO:  pluggable update policy
//TODO:  format sometimes not handled properly when a rendering becomes visible
//TODO:  when the base address is changed, sync top index and load address not updated properly
//TODO:  linux - cannot resize columns to preferred size
//TODO:  synchonization for page up and page down, not woring quite properly
public abstract class AbstractAsyncTableRendering extends AbstractBaseTableRendering implements IPropertyChangeListener, IResettableMemoryRendering {

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
				setText(DebugUIMessages.ShowAddressColumnAction_0); 
			} else {
				setText(DebugUIMessages.ShowAddressColumnAction_1); 
			}
		}
	}
	
	private class NextPageAction extends Action
	{
		private NextPageAction()
		{
			super();
			setText(DebugUIMessages.AbstractTableRendering_4);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugUIConstants.PLUGIN_ID + ".NextPageAction_context"); //$NON-NLS-1$ 
		}

		public void run() {
			BigInteger address = fContentInput.getLoadAddress();
			address = address.add(BigInteger.valueOf(getPageSizeInUnits()));
			handlePageStartAddressChanged(address);
		}
	}
	
	private class PrevPageAction extends Action
	{
		private PrevPageAction()
		{
			super();
			setText(DebugUIMessages.AbstractTableRendering_6);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugUIConstants.PLUGIN_ID + ".PrevPageAction_context"); //$NON-NLS-1$
		}

		public void run() {
			BigInteger address = fContentInput.getLoadAddress();
			address = address.subtract(BigInteger.valueOf(getPageSizeInUnits()));
			handlePageStartAddressChanged(address);
		}
	}
	
	private PageBook fPageBook;
	private AsyncTableRenderingViewer fTableViewer;
	private TextViewer fTextViewer;
	private Shell fToolTipShell;
	private TableRenderingPresentationContext fPresentationContext;
	private int fAddressableSize;
	private TableRenderingContentInput fContentInput;
	private int fBytePerLine;
	private int fColumnSize;
	private boolean fShowMessage = false;
	private String fLabel;
	private IWorkbenchAdapter fWorkbenchAdapter;
	private int fPageSize;
	
	// actions
	private GoToAddressAction fGoToAddressAction;
	private PrintTableRenderingAction fPrintViewTabAction;
	private CopyTableRenderingToClipboardAction fCopyToClipboardAction;
	private FormatTableRenderingAction fFormatRenderingAction;
	private ReformatAction fReformatAction;
	private ToggleAddressColumnAction fToggleAddressColumnAction;
	private ResetToBaseAddressAction fResetMemoryBlockAction;
	private PropertyDialogAction fPropertiesDialogAction;
	private NextPageAction fNextAction;
	private PrevPageAction fPrevAction;
	
	private boolean fIsCreated = false;
	private boolean fIsDisposed = false;
	private boolean fIsShowAddressColumn = true;
	
	private ISelectionChangedListener fViewerSelectionChangedListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			updateSyncTopAddress(getTopVisibleAddress());
			updateSyncSelectedAddress(getSelectedAddress());
		}
	};
	
	private SelectionAdapter fScrollBarSelectionListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			updateSyncTopAddress(getTopVisibleAddress());
		}
	};
	
	private IModelChangedListener fModelChangedListener = new IModelChangedListener() {
		public void modelChanged(IModelDelta delta) {
			if (delta.getElement() == getMemoryBlock())
			{
				showTable();
				updateRenderingLabel(isVisible());
			}
		}};
	
	private IVirtualContentListener fViewerListener = new IVirtualContentListener() {

		public void handledAtBufferStart() {
			if (getMemoryBlock() instanceof IMemoryBlockExtension)
			{
				if (isDynamicLoad())
				{
					BigInteger address = getTopVisibleAddress();
					if (address != null && !isAtTopLimit())
						reloadTable(address);
				}
			}
		}

		public void handleAtBufferEnd() {
			if (getMemoryBlock() instanceof IMemoryBlockExtension)
			{
				if (isDynamicLoad())
				{
					BigInteger address = getTopVisibleAddress();
					if (address != null && !isAtBottomLimit())
						reloadTable(address);
				}
			}
		}

		public int getThreshold() {
			return 3;
		}};
		
	private IPresentationErrorListener fPresentationErrorListener = new IPresentationErrorListener() {
		public void handlePresentationFailure(IAsynchronousRequestMonitor monitor, IStatus status) {
			showMessage(status.getMessage());
		}};
	
	public AbstractAsyncTableRendering(String renderingId) {
		super(renderingId);
	}

	public void resetRendering() throws DebugException {
		BigInteger baseAddress = fContentInput.getContentBaseAddress();
		goToAddress(baseAddress);
		fTableViewer.setTopIndex(baseAddress);
		fTableViewer.setSelection(baseAddress);
	}

	public Control createControl(Composite parent) {

		fPageBook = new PageBook(parent, SWT.NONE);
		createMessagePage(fPageBook);
		createTableViewer(fPageBook);
		addListeners();
		return fPageBook;
	}
	
	private void createMessagePage(Composite parent)
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
	
	private void createTableViewer(final Composite parent)
	{
		Job job = new Job("Create Table Viewer") { //$NON-NLS-1$

			protected IStatus run(IProgressMonitor monitor) {
				
				showMessage(DebugUIMessages.AbstractAsyncTableRendering_0);
				
				// gather info from memory block
				initAddressableSize();					
				final BigInteger topVisibleAddress = getInitialTopVisibleAddress();
				BigInteger mbBaseAddress = null;
				try {
					mbBaseAddress = getMemoryBlockBaseAddress();
				} catch (DebugException e) {
					showMessage(e.getMessage());
				}
				
				final BigInteger finalMbBaseAddress = mbBaseAddress;
				final BigInteger initialSelectedAddress = getInitialSelectedAddress();
		
				// batch update on UI thread
				UIJob uiJob = new UIJob("Create Table Viewer UI Job"){ //$NON-NLS-1$

					public IStatus runInUIThread(IProgressMonitor progressMonitor) {
						
						fTableViewer = new AsyncTableRenderingViewer(AbstractAsyncTableRendering.this, parent, SWT.VIRTUAL | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.HIDE_SELECTION | SWT.BORDER);
						fPresentationContext = new TableRenderingPresentationContext(getMemoryRenderingContainer().getMemoryRenderingSite().getSite().getPart());
						fPresentationContext.setContainerId(getMemoryRenderingContainer().getId());
						fPresentationContext.setRendering(AbstractAsyncTableRendering.this);
						fTableViewer.setContext(fPresentationContext);
						
						// must call this after the context is created as the info is stored in the context
						getDynamicLoadFromPreference();
						getPageSizeFromPreference();
						
						int numberOfLines = getNumLinesToLoad();
						
						BigInteger baseAddress = finalMbBaseAddress;
						if (baseAddress == null)
							baseAddress = BigInteger.ZERO;
						fContentInput = new TableRenderingContentInput(AbstractAsyncTableRendering.this, 20, 20, 20, topVisibleAddress, numberOfLines, false, baseAddress);
						
						if (!(getMemoryBlock() instanceof IMemoryBlockExtension) || !isDynamicLoad())
						{		
							// If not extended memory block, do not create any buffer
							// no scrolling
							fContentInput.setPreBuffer(0);
							fContentInput.setPostBuffer(0);
							fContentInput.setDefaultBufferSize(0);
						} 
						
						fPresentationContext.setContentInput(fContentInput);
						
						setupInitialFormat();
						fTableViewer.setCellModifier(createCellModifier());
						fTableViewer.getTable().setHeaderVisible(true);
						fTableViewer.getTable().setLinesVisible(true);	
						fTableViewer.addPresentationErrorListener(fPresentationErrorListener);
						fTableViewer.setInput(getMemoryBlock());
						fTableViewer.resizeColumnsToPreferredSize();
						fTableViewer.setTopIndex(topVisibleAddress);
						
						fTableViewer.setSelection(initialSelectedAddress);

						// SET UP FONT		
						// set to a non-proportional font
						fTableViewer.getTable().setFont(JFaceResources.getFont(IInternalDebugUIConstants.FONT_NAME));
						
						if (finalMbBaseAddress != null)
							showTable();
						
						fTableViewer.addVirtualContentListener(fViewerListener);
						
						// create context menu
						// create pop up menu for the rendering
						createActions();
						createPopupMenu(fTableViewer.getControl());
						createPopupMenu(fTableViewer.getCursor());
						getPopupMenuManager().addMenuListener(new IMenuListener() {
							public void menuAboutToShow(IMenuManager mgr) {
								fillContextMenu(mgr);
								mgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
							}});
						
						fTableViewer.addSelectionChangedListener(fViewerSelectionChangedListener);
						fTableViewer.getTable().getVerticalBar().addSelectionListener(fScrollBarSelectionListener);
						
						createToolTip();
						
						return Status.OK_STATUS;
					}};
				uiJob.setSystem(true);
				uiJob.schedule();
				
				// now the rendering is successfully created
				fIsCreated = true;
					
				return Status.OK_STATUS;

			}};
			
		job.setSystem(true);
		job.schedule();
	}
	
	private BigInteger getInitialSelectedAddress() {
		// figure out selected address 
		BigInteger selectedAddress = (BigInteger) getSynchronizedProperty(AbstractTableRendering.PROPERTY_SELECTED_ADDRESS);
		if (selectedAddress == null)
		{
			if (getMemoryBlock() instanceof IMemoryBlockExtension) {
				try {
					selectedAddress = ((IMemoryBlockExtension) getMemoryBlock()).getBigBaseAddress();
				} catch (DebugException e) {
					selectedAddress = BigInteger.ZERO;
				}
				
				if (selectedAddress == null) {
					selectedAddress =BigInteger.ZERO;
				}

			} else {
				long address = getMemoryBlock().getStartAddress();
				selectedAddress = BigInteger.valueOf(address);
			}
		}
		return selectedAddress;
	}
	
	private void addListeners()
	{
		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		addRenderingToSyncService();
	}
	
	private void removeListeners()
	{
		DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		removeRenderingFromSyncService();
	}
	
	private void addRenderingToSyncService()
	{	
		IMemoryRenderingSynchronizationService syncService = getMemoryRenderingContainer().getMemoryRenderingSite().getSynchronizationService();
		
		if (syncService == null)
			return;
		
		syncService.addPropertyChangeListener(this, null);
	}
	
	private void removeRenderingFromSyncService()
	{
		IMemoryRenderingSynchronizationService syncService = getMemoryRenderingContainer().getMemoryRenderingSite().getSynchronizationService();
		
		if (syncService == null)
			return;
		
		syncService.removePropertyChangeListener(this);		
	}
	
	private void initAddressableSize()
	{
		// set up addressable size and figure out number of bytes required per line
		fAddressableSize = -1;
		try {
			if (getMemoryBlock() instanceof IMemoryBlockExtension)
				fAddressableSize = ((IMemoryBlockExtension)getMemoryBlock()).getAddressableSize();
			else
				fAddressableSize = 1;
		} catch (DebugException e1) {
			DebugUIPlugin.log(e1);
			// log error and default to 1
			fAddressableSize = 1;
			return;
			
		}
		if (fAddressableSize < 1)
		{
			DebugUIPlugin.logErrorMessage("Invalid addressable size"); //$NON-NLS-1$
			fAddressableSize = 1;
		}
	}
	
	private BigInteger getInitialTopVisibleAddress() {
		BigInteger topVisibleAddress = (BigInteger) getSynchronizedProperty(AbstractTableRendering.PROPERTY_TOP_ADDRESS);
		if (topVisibleAddress == null)
		{
			if (getMemoryBlock() instanceof IMemoryBlockExtension)
			{
				try {
					topVisibleAddress = ((IMemoryBlockExtension)getMemoryBlock()).getBigBaseAddress();
				} catch (DebugException e1) {
					topVisibleAddress = new BigInteger("0"); //$NON-NLS-1$
				}
			}
			else
			{
				topVisibleAddress = BigInteger.valueOf(getMemoryBlock().getStartAddress());
			}
		}
		return topVisibleAddress;
	}
	
	private void setupInitialFormat() {
		
		boolean validated = validateInitialFormat();
		
		if (!validated)
		{
			// pop up dialog to ask user for default values
			StringBuffer msgBuffer = new StringBuffer(DebugUIMessages.AbstractTableRendering_20);
			msgBuffer.append(" "); //$NON-NLS-1$
			msgBuffer.append(this.getLabel());
			msgBuffer.append("\n\n"); //$NON-NLS-1$
			msgBuffer.append(DebugUIMessages.AbstractTableRendering_16);
			msgBuffer.append("\n"); //$NON-NLS-1$
			msgBuffer.append(DebugUIMessages.AbstractTableRendering_18);
			msgBuffer.append("\n\n"); //$NON-NLS-1$
			
			int bytePerLine = fBytePerLine;
			int columnSize = fColumnSize;
			
			// initialize this value to populate the dialog properly
			fBytePerLine = getDefaultRowSize() / getAddressableSize();
			fColumnSize = getDefaultColumnSize() / getAddressableSize();

			FormatTableRenderingDialog dialog = new FormatTableRenderingDialog(this, DebugUIPlugin.getShell());
			dialog.openError(msgBuffer.toString());
			
			// restore to original value before formatting
			fBytePerLine = bytePerLine;
			fColumnSize = columnSize;
			
			bytePerLine = dialog.getRowSize() * getAddressableSize();
			columnSize = dialog.getColumnSize() * getAddressableSize();
			
			format(bytePerLine, columnSize);
		}
		else
		{
			// Row size is stored as number of addressable units in preference store
			int bytePerLine = getDefaultRowSize();
			// column size is now stored as number of addressable units
			int columnSize = getDefaultColumnSize();
			
			// format memory block with specified "bytesPerLine" and "columnSize"	
			boolean ok = format(bytePerLine, columnSize);
			
			if (!ok)
			{
				// this is to ensure that the rest of the rendering can be created
				// and we can recover from a format error
				format(bytePerLine, bytePerLine);
			}
		}
	}
	
	private boolean validateInitialFormat()
	{
		int rowSize = getDefaultRowSize();
		int columnSize = getDefaultColumnSize();
		
		if (rowSize < columnSize || rowSize % columnSize != 0 || rowSize == 0 || columnSize == 0)
		{
			return false;
		}
		return true;
	}

	public Control getControl() {

		return null;
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
		
		Object evtSrc = event.getSource();
		
		if (event.getProperty().equals(IDebugPreferenceConstants.PREF_TABLE_RENDERING_PAGE_SIZE)) {
			// always update page size, only refresh if the table is visible
			getPageSizeFromPreference();
		}
		
		// do not handle event if the rendering is displaying an error
		if (isDisplayingError())
			return;
		
		// do not handle property change event if the rendering is not visible
		if (!isVisible())
			return;
		
		if (event.getProperty().equals(IDebugUIConstants.PREF_PADDED_STR) || 
			event.getProperty().equals(IDebugUIConstants.PREF_CHANGED_DEBUG_ELEMENT_COLOR))
		{
			if (!fIsDisposed)
			{
				fTableViewer.refresh();
			}
			return;
		}
		
		if (event.getProperty().equals(IDebugPreferenceConstants.PREF_DYNAMIC_LOAD_MEM)) {
			handleDyanicLoadChanged();
			return;
		}
		
		if (event.getProperty().equals(IDebugPreferenceConstants.PREF_TABLE_RENDERING_PAGE_SIZE)) {
			if (!isDynamicLoad())
			{
				// only refresh if in non-autoload mode
				refresh();
			}
			return;
		}
		
		if (evtSrc == this)
			return;
		
		if (evtSrc instanceof IMemoryRendering)
		{
			IMemoryRendering rendering = (IMemoryRendering)evtSrc;
			IMemoryBlock memoryBlock = rendering.getMemoryBlock();
			
			// do not handle event from renderings displaying other memory blocks
			if (memoryBlock != getMemoryBlock())
				return;
		}
	
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
		else if (propertyName.equals(AbstractTableRendering.PROPERTY_ROW_SIZE) && value instanceof Integer)
		{
			rowSizeChanged(((Integer)value).intValue());
		}
		else if (propertyName.equals(AbstractTableRendering.PROPERTY_TOP_ADDRESS) && value instanceof BigInteger)
		{
			topVisibleAddressChanged((BigInteger)value);
		}
		else if (propertyName.equals(IInternalDebugUIConstants.PROPERTY_PAGE_START_ADDRESS) && value instanceof BigInteger)
		{
			handlePageStartAddressChanged((BigInteger)value);
		}
		
	}	
	
	private void topVisibleAddressChanged(final BigInteger address)
	{
		final Runnable runnable = new Runnable() {
			public void run() {
				if (fIsDisposed)
					return;
				
				int idx = fTableViewer.indexOf(address);
				if (idx >= 0)
				{
					fTableViewer.setTopIndex(address);
					fTableViewer.topIndexChanged();
				}
				else
				{
					reloadTable(address);
				}
				Object selection = fTableViewer.getSelectionKey();
				fTableViewer.setSelection(selection);
			}};
		runOnUIThread(runnable);
	}
	
	private void runOnUIThread(final Runnable runnable)
	{
		if (Display.getCurrent() != null)	
		{
			runnable.run();
		}
		else
		{
			UIJob job = new UIJob("Async Table Rendering UI Job"){ //$NON-NLS-1$
	
				public IStatus runInUIThread(IProgressMonitor monitor) {
					runnable.run();
					return Status.OK_STATUS;
				}};
			job.setSystem(true);
			job.schedule();
		}
	}
	
	private void selectedAddressChanged(BigInteger address)
	{
		fTableViewer.setSelection(address);
		
		// call this to make the table viewer to reload when needed
		fTableViewer.topIndexChanged();
	}
	
	private void setFont(Font font)
	{	
		// set font
		fTableViewer.getTable().setFont(font);
		fTableViewer.getCursor().setFont(font);		
	}
	
	private int getDefaultColumnSize() {
		
		// default to global preference store
		IPreferenceStore prefStore = DebugUITools.getPreferenceStore();
		int columnSize = prefStore.getInt(IDebugPreferenceConstants.PREF_COLUMN_SIZE);
		// actual column size is number of addressable units * size of the addressable unit
		columnSize = columnSize * getAddressableSize();
		
		// check synchronized col size
		Integer colSize = (Integer)getSynchronizedProperty(AbstractTableRendering.PROPERTY_COL_SIZE);
		if (colSize != null)
		{
			// column size is stored as actual number of bytes in synchronizer
			int syncColSize = colSize.intValue(); 
			if (syncColSize > 0)
			{
				columnSize = syncColSize;
			}	
		}
		else
		{
			IPersistableDebugElement elmt = (IPersistableDebugElement)getMemoryBlock().getAdapter(IPersistableDebugElement.class);
			int defaultColSize = -1;
			
			if (elmt != null)
			{
				if (elmt.supportsProperty(this, IDebugPreferenceConstants.PREF_COL_SIZE_BY_MODEL))
					defaultColSize = getDefaultFromPersistableElement(IDebugPreferenceConstants.PREF_COL_SIZE_BY_MODEL);
			}
			
			if (defaultColSize <= 0)
			{
				// if not provided, get default by model
				defaultColSize = getDefaultColumnSizeByModel(getMemoryBlock().getModelIdentifier());
			}
			
			if (defaultColSize > 0)
				columnSize = defaultColSize * getAddressableSize();
		}
		return columnSize;
	}

	private int getDefaultRowSize() {
		
		int rowSize = DebugUITools.getPreferenceStore().getInt(IDebugPreferenceConstants.PREF_ROW_SIZE);
		int bytePerLine = rowSize * getAddressableSize();
		
		// check synchronized row size
		Integer size = (Integer)getSynchronizedProperty(AbstractTableRendering.PROPERTY_ROW_SIZE);
		if (size != null)
		{
			// row size is stored as actual number of bytes in synchronizer
			int syncRowSize = size.intValue(); 
			if (syncRowSize > 0)
			{
				bytePerLine = syncRowSize;
			}	
		}
		else
		{
			int defaultRowSize = -1;
			IPersistableDebugElement elmt = (IPersistableDebugElement)getMemoryBlock().getAdapter(IPersistableDebugElement.class);
			if (elmt != null)
			{
				if (elmt.supportsProperty(this, IDebugPreferenceConstants.PREF_ROW_SIZE_BY_MODEL))
				{
					defaultRowSize = getDefaultFromPersistableElement(IDebugPreferenceConstants.PREF_ROW_SIZE_BY_MODEL);
					return defaultRowSize * getAddressableSize();
				}
			}
			
			if (defaultRowSize <= 0)
				// no synchronized property, ask preference store by id
				defaultRowSize = getDefaultRowSizeByModel(getMemoryBlock().getModelIdentifier());
			
			if (defaultRowSize > 0)
				bytePerLine = defaultRowSize * getAddressableSize();
		}
		return bytePerLine;
	}
	
	/**
	 * Returns the addressible size of this rendering's memory block in bytes.
	 * 
	 * @return the addressible size of this rendering's memory block in bytes
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
	
	private int getDefaultFromPersistableElement(String propertyId) {
		int defaultValue = -1;
		IPersistableDebugElement elmt = (IPersistableDebugElement)getMemoryBlock().getAdapter(IPersistableDebugElement.class);
		if (elmt != null)
		{
			try {
				Object valueMB = elmt.getProperty(this, propertyId);
				if (valueMB != null && !(valueMB instanceof Integer))
				{
					IStatus status = DebugUIPlugin.newErrorStatus("Model returned invalid type on " + propertyId, null); //$NON-NLS-1$
					DebugUIPlugin.log(status);
				}
				
				if (valueMB != null)
				{
					Integer value = (Integer)valueMB;
					defaultValue = value.intValue();
				}
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		return defaultValue;
	}
	
	/**
	 * @param modelId
	 * @return default number of addressable units per line for the model
	 */
	private int getDefaultRowSizeByModel(String modelId)
	{
		int row = DebugUITools.getPreferenceStore().getInt(getRowPrefId(modelId));
		if (row == 0)
		{
			DebugUITools.getPreferenceStore().setValue(getRowPrefId(modelId), IDebugPreferenceConstants.PREF_ROW_SIZE_DEFAULT);
		}
		
		row = DebugUITools.getPreferenceStore().getInt(getRowPrefId(modelId));
		return row;
		
	}
	
	/**
	 * @param modelId
	 * @return default number of addressable units per column for the model
	 */
	private int getDefaultColumnSizeByModel(String modelId)
	{
		int col = DebugUITools.getPreferenceStore().getInt(getColumnPrefId(modelId));
		if (col == 0)
		{
			DebugUITools.getPreferenceStore().setValue(getColumnPrefId(modelId), IDebugPreferenceConstants.PREF_COLUMN_SIZE_DEFAULT);
		}
		
		col = DebugUITools.getPreferenceStore().getInt(getColumnPrefId(modelId));
		return col;
	}
	
	
	private String getRowPrefId(String modelId) {
		String rowPrefId = IDebugPreferenceConstants.PREF_ROW_SIZE + ":" + modelId; //$NON-NLS-1$
		return rowPrefId;
	}

	private String getColumnPrefId(String modelId) {
		String colPrefId = IDebugPreferenceConstants.PREF_COLUMN_SIZE + ":" + modelId; //$NON-NLS-1$
		return colPrefId;
	}
	
	/**
	 * Format view tab based on the bytes per line and column.
	 * 
	 * @param bytesPerLine - number of bytes per line, possible values: (1 / 2 / 4 / 8 / 16) * addressableSize
	 * @param columnSize - number of bytes per column, possible values: (1 / 2 / 4 / 8 / 16) * addressableSize
	 * @return true if format is successful, false, otherwise
	 */
	public boolean format(int bytesPerLine, int columnSize)
	{	
		
		// bytes per cell must be divisible to bytesPerLine
		if (bytesPerLine % columnSize != 0)
		{
			return false;
		}
		
		if (bytesPerLine < columnSize)
		{
			return false;
		}
		
		// do not format if the view tab is already in that format
		if(fBytePerLine == bytesPerLine && fColumnSize == columnSize){
			return false;
		}
		
		fBytePerLine = bytesPerLine;
		fColumnSize = columnSize;
		formatViewer();
		
		updateSyncRowSize();
		updateSyncColSize();
		
		return true;
	}
		
	
	/**
	 * Returns the number of addressable units per row.
	 *  
	 * @return number of addressable units per row
	 */
	public int getAddressableUnitPerLine() {
		return fBytePerLine / getAddressableSize();
	}
	
	/**
	 * Returns the number of addressable units per column.
	 * 
	 * @return number of addressable units per column
	 */
	public int getAddressableUnitPerColumn() {
		return fColumnSize / getAddressableSize();
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
	
	private BigInteger getMemoryBlockBaseAddress() throws DebugException
	{
		if (getMemoryBlock() instanceof IMemoryBlockExtension)
			return ((IMemoryBlockExtension)getMemoryBlock()).getBigBaseAddress();
		else
			return BigInteger.valueOf(getMemoryBlock().getStartAddress());
	}
	
	/**
	 * Displays an error message for the given exception.
	 * @param e exception to display 
	 */
	private void showMessage(final String message)
	{
		UIJob job = new UIJob("Display Message Job"){ //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				StyledText styleText = null;
				fShowMessage = true;

				styleText = fTextViewer.getTextWidget();
				
				if (styleText != null)
					styleText.setText(message);
				fPageBook.showPage(fTextViewer.getControl());
				
				return Status.OK_STATUS;
			}};
			
		job.setSystem(true);
		job.schedule();
	}
	
	/**
	 * Returns the number of bytes displayed in a single column cell.
	 * 
	 * @return the number of bytes displayed in a single column cell
	 */
	public int getBytesPerColumn()
	{
		return fColumnSize;
	}

	/**
	 * Returns the number of bytes displayed in a row.
	 * 
	 * @return the number of bytes displayed in a row
	 */
	public int getBytesPerLine()
	{		
		return fBytePerLine;
	}
	
	public boolean isDisplayingError()
	{
		return fShowMessage;
	}
	
	/**
	 * Displays the content of the table viewer.
	 */
	public void showTable()
	{
		UIJob job = new UIJob("Display Table Job"){ //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				fShowMessage = false;
				fPageBook.showPage(fTableViewer.getControl());
				return Status.OK_STATUS;
			}};
		job.setSystem(true);
		job.schedule();
	}
	
	private BigInteger getTopVisibleAddress() {
		
		if (fTableViewer == null)
			return BigInteger.valueOf(0);

		Table table = fTableViewer.getTable();
		int topIndex = table.getTopIndex();

		if (topIndex < 0) { return null; }

		if (table.getItemCount() > topIndex) 
		{
			MemorySegment topItem = (MemorySegment)table.getItem(topIndex).getData();
			if (topItem != null)
			{
				return topItem.getAddress();
			}
		}
		return null;
	}
	
	private  synchronized void  reloadTable(final BigInteger topAddress) {
		
		if (AsyncVirtualContentTableViewer.DEBUG_DYNAMIC_LOADING)
			System.out.println("reload at: " + topAddress.toString(16)); //$NON-NLS-1$
		
		fContentInput.setLoadAddress(topAddress);
		fContentInput.setNumLines(getNumLinesToLoad());
		fTableViewer.refresh();
		fTableViewer.setTopIndex(topAddress);
	}
	
	private boolean isAtTopLimit()
	{	
		BigInteger startAddress = fContentInput.getStartAddress();
		startAddress = MemoryViewUtil.alignDoubleWordBoundary(startAddress);
		if (fTableViewer.getContentManager() instanceof IVirtualContentManager)
		{
			IVirtualContentManager vcMgr = (IVirtualContentManager)fTableViewer.getContentManager();
			Object key = vcMgr.getKey(0);
			if (key instanceof BigInteger)
			{
				BigInteger startBufferAddress = (BigInteger)key;
				startBufferAddress = MemoryViewUtil.alignDoubleWordBoundary(startBufferAddress);
				
				if (startAddress.compareTo(startBufferAddress) == 0)
					return true;
			}
		}
		return false;
	}
	
	private boolean isAtBottomLimit()
	{
		BigInteger endAddress = fContentInput.getEndAddress();
		endAddress = MemoryViewUtil.alignDoubleWordBoundary(endAddress);
		
		int numElements = fTableViewer.getContentManager().getElements().length;
		
		if (fTableViewer.getContentManager() instanceof IVirtualContentManager)
		{
			IVirtualContentManager vcMgr = (IVirtualContentManager)fTableViewer.getContentManager();
			Object key = vcMgr.getKey(numElements-1);
			if (key instanceof BigInteger)
			{
				BigInteger endBufferAddress = (BigInteger)key;
				endBufferAddress = MemoryViewUtil.alignDoubleWordBoundary(endBufferAddress);
				
				if (endAddress.compareTo(endBufferAddress) == 0)
					return true;
			}
		}
		
		return false;		
	}
	
	private void formatViewer() {
		
		fTableViewer.disposeColumns();
		fTableViewer.disposeCellEditors();
		doFormatTable();
		fTableViewer.setColumnHeaders(getColumnProperties());
		fTableViewer.showColumnHeader(true);
		fTableViewer.setCellEditors(createCellEditors(fTableViewer.getTable()));
		
		fTableViewer.formatViewer();
		
		// This resize needs to happen after the viewer has finished
		// getting the labels.
		// This fix is a hack to delay the resize until the viewer has a chance to get
		// the setData event from the UI thread.  Otherwise, the columns will be
		// squeezed together.
		// TODO:  test on linux
		UIJob job = new UIJob("resize to fit"){ //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				resizeColumnsToPreferredSize();
				return Status.OK_STATUS;
			}};
		
		job.setSystem(true);
		job.schedule();
	}

	private void doFormatTable() {
		int bytesPerLine = getBytesPerLine();
		int columnSize = getBytesPerColumn();
		int numColumns = bytesPerLine/columnSize;
		
		Table table = fTableViewer.getTable();
		TableColumn column0 = new TableColumn(table,SWT.LEFT,0);
		column0.setText(DebugUIMessages.AbstractTableRendering_2); 
		
		// create new byte columns
		TableColumn [] byteColumns = new TableColumn[numColumns];		
		for (int i=0;i<byteColumns.length; i++)
		{
			TableColumn column = new TableColumn(table, SWT.LEFT, i+1);
			byteColumns[i] = column;
		}
		
		//Empty column for cursor navigation
		TableColumn emptyCol = new TableColumn(table,SWT.LEFT,byteColumns.length+1);
		emptyCol.setText(" "); //$NON-NLS-1$
		emptyCol.setWidth(1);
		emptyCol.setResizable(false);
	    table.setHeaderVisible(true);
	    
	    // allow clients to override column labels
	   setColumnHeadings();
	    
	}
	
	private String[] getColumnProperties()
	{
		int numColumns = getAddressableUnitPerLine()/getAddressableUnitPerColumn();
		// +2 to include properties for address and navigation column
		String[] columnProperties = new String[numColumns+2];
		columnProperties[0] = TableRenderingLine.P_ADDRESS;
		
		int addressableUnit = getAddressableUnitPerColumn();

		// use column beginning offset to the row address as properties
		for (int i=1; i<columnProperties.length-1; i++)
		{
			// column properties are stored as number of addressable units from the
			// the line address
			columnProperties[i] = Integer.toHexString((i-1)*addressableUnit);
		}
		
		// Empty column for cursor navigation
		columnProperties[columnProperties.length-1] = " "; //$NON-NLS-1$
		return columnProperties;
	}
	
   private CellEditor[] createCellEditors(Table table) {
        CellEditor[] editors = new CellEditor[table.getColumnCount()];
        for (int i=0; i<editors.length; i++)
        {
        	editors[i] = new TextCellEditor(table);
        }
        return editors;
    }
   
   	private ICellModifier createCellModifier() {
       return new AsyncTableRenderingCellModifier(this);
   	}

	
	public void dispose() {
		
		if (fIsDisposed)
			return;
		
		removeListeners();
		
		if (fTableViewer != null)
		{
			if (fViewerListener != null)
				fTableViewer.removeVirtualContentListener(fViewerListener);
			
			if (fPresentationErrorListener != null)
				fTableViewer.removePresentationErrorListener(fPresentationErrorListener);
			
			fTableViewer.removeSelectionChangedListener(fViewerSelectionChangedListener);
			fTableViewer.getTable().getVerticalBar().removeSelectionListener(fScrollBarSelectionListener);
			
			fTableViewer.dispose();
		}
		
		fIsDisposed = true;
		
		super.dispose();
	}
	
	/**
	 * Updates the label of this rendering, optionally displaying the
	 * base address of this rendering's memory block.
	 * 
	 * @param showAddress whether to display the base address of this
	 *  rendering's memory block in this rendering's label
	 */
	protected void updateRenderingLabel(final boolean showAddress)
	{
		Job job = new Job("Update Rendering Label"){ //$NON-NLS-1$

			protected IStatus run(IProgressMonitor monitor) {
				fLabel = buildLabel(showAddress);
				firePropertyChangedEvent(new PropertyChangeEvent(AbstractAsyncTableRendering.this, IBasicPropertyConstants.P_TEXT, null, fLabel));
				return Status.OK_STATUS;
			}};
		job.setSystem(true);
		job.schedule();
	}
	
	private String buildLabel(boolean showAddress) {
		String label = ""; //$NON-NLS-1$
		if (getMemoryBlock() instanceof IMemoryBlockExtension)
		{
			label = ((IMemoryBlockExtension)getMemoryBlock()).getExpression();
			
			if (label.startsWith("&")) //$NON-NLS-1$
				label = "&" + label; //$NON-NLS-1$
			
			if (label == null)
			{
				label = DebugUIMessages.AbstractTableRendering_8; 
			}
			
			try {
				if (showAddress && ((IMemoryBlockExtension)getMemoryBlock()).getBigBaseAddress() != null)
				{	
					label += " : 0x"; //$NON-NLS-1$
					label += ((IMemoryBlockExtension)getMemoryBlock()).getBigBaseAddress().toString(16).toUpperCase();
				}
			} catch (DebugException e) {
				// do nothing, the label will not show the address
			}
		}
		else
		{
			long address = getMemoryBlock().getStartAddress();
			label = Long.toHexString(address).toUpperCase();
		}
		
		String preName = DebugUITools.getMemoryRenderingManager().getRenderingType(getRenderingId()).getLabel();
		
		if (preName != null)
			label += " <" + preName + ">"; //$NON-NLS-1$ //$NON-NLS-2$
		
		return decorateLabel(label);
	}
	
	public String getLabel() {
		
		if (fLabel == null)
		{
			fLabel = DebugUIMessages.AbstractAsyncTableRendering_1;
			updateRenderingLabel(true);
		}
		
		return fLabel;
	}
	
	public Object getAdapter(Class adapter) {
		
		if (adapter == IColorProvider.class)
			return getColorProviderAdapter();
		
		if (adapter == ILabelProvider.class)
			return getLabelProviderAdapter();
		
		if (adapter == IFontProvider.class)
			return getFontProviderAdapter();
		
		if (adapter == IModelChangedListener.class)
		{
			return fModelChangedListener;
		}
		
		if (adapter == IWorkbenchAdapter.class)
		{
			// needed workbench adapter to fill the title of property page
			if (fWorkbenchAdapter == null) {
				fWorkbenchAdapter = new IWorkbenchAdapter() {
					public Object[] getChildren(Object o) {
						return new Object[0];
					}
	
					public ImageDescriptor getImageDescriptor(Object object) {
						return null;
					}
	
					public String getLabel(Object o) {
						return AbstractAsyncTableRendering.this.getLabel();
					}
	
					public Object getParent(Object o) {
						return null;
					}
				};
			}
			return fWorkbenchAdapter;
		}
		
		return super.getAdapter(adapter);
	}
	
	/**
	 * Returns the number of characters a byte will convert to
	 * or -1 if unknown.
	 * 
	 * @return the number of characters a byte will convert to
	 *  or -1 if unknown
	 */
	public int getNumCharsPerByte()
	{
		return -1;
	}
	
	/**
	 * Create actions for the view tab
	 */
	protected void createActions() {
		
		fCopyToClipboardAction = new CopyTableRenderingToClipboardAction(this, fTableViewer);
		fGoToAddressAction = new GoToAddressAction(this);
		fResetMemoryBlockAction = new ResetToBaseAddressAction(this);
		
		fPrintViewTabAction = new PrintTableRenderingAction(this, fTableViewer);
		
		fFormatRenderingAction = new FormatTableRenderingAction(this);		
		fReformatAction = new ReformatAction(this);
		fToggleAddressColumnAction = new ToggleAddressColumnAction();
		
		IMemoryRenderingSite site = getMemoryRenderingContainer().getMemoryRenderingSite();
		if (site.getSite().getSelectionProvider() != null)
		{
			fPropertiesDialogAction = new PropertyDialogAction(site.getSite(),site.getSite().getSelectionProvider()); 
		}
		
		fNextAction = new NextPageAction();
		fPrevAction = new PrevPageAction();
	}
	
	public BigInteger getSelectedAddress() {
		Object key = fTableViewer.getSelectionKey();
		
		if (key != null && key instanceof BigInteger)
			return (BigInteger)key;
		
		return null;
	}

	/**
	 * Returns the currently selected content in this rendering as MemoryByte.
	 * 
	 * @return the currently selected content in array of MemoryByte.  
	 * Returns an empty array if the selected address is out of buffered range.
	 */
	public MemoryByte[] getSelectedAsBytes()
	{
		// TODO:  must be called on UI thread or fails... need to look at this and make
		// sure that this can be called on non-UI thread
		if (getSelectedAddress() == null)
			return new MemoryByte[0];
		
		TableCursor cursor = fTableViewer.getCursor();
		int col = cursor.getColumn();
		TableItem rowItem = cursor.getRow();
		
		// check precondition
		if (col == 0 || col > getBytesPerLine()/getBytesPerColumn())
		{
			return new MemoryByte[0];
		}
		
		Object data = rowItem.getData();
		if (data == null || !(data instanceof MemorySegment))
			return new MemoryByte[0];
		
		MemorySegment line = (MemorySegment)data;
		int offset = (col-1)*(getAddressableUnitPerColumn()*getAddressableSize());
		
		// make a copy of the bytes to ensure that data cannot be changed
		// by caller
		MemoryByte[] bytes = line.getBytes(offset, getAddressableUnitPerColumn()*getAddressableSize());
		MemoryByte[] retBytes = new MemoryByte[bytes.length];
		
		System.arraycopy(bytes, 0, retBytes, 0, bytes.length);
		
		return retBytes;
	}

	/**
	 * Returns the currently selected content in this rendering as a String.
	 * 
	 * @return the currently selected content in this rendering
	 */
	public String getSelectedAsString() {

		// TODO:  must be called on UI thread or fails... need to look at this and make
		// sure that this can be called on non-UI thread
		
		if (getSelectedAddress() == null)
			return ""; //$NON-NLS-1$
		
		TableCursor cursor = fTableViewer.getCursor();
		int col = cursor.getColumn();
		TableItem rowItem = cursor.getRow();
		int row = fTableViewer.getTable().indexOf(rowItem);
		
		if (col == 0)
		{
			return rowItem.getText(0);
		}
		
		// check precondition
		if (col > getBytesPerLine()/getBytesPerColumn())
		{
			return ""; //$NON-NLS-1$
		}
				
		TableItem tableItem = fTableViewer.getTable().getItem(row);
		
		return tableItem.getText(col);	
	}

	public void goToAddress(BigInteger address) throws DebugException {
		
		int i = fTableViewer.getVirtualContentManager().indexOf(address);

		if (i >= 0)
		{
			// address is within range, set cursor and reveal
			fTableViewer.setSelection(address);
			updateSyncTopAddress(address);
			updateSyncSelectedAddress(address);
		}
		else
		{
			// if not extended memory block
			// do not allow user to go to an address that's out of range
			if (!(getMemoryBlock() instanceof IMemoryBlockExtension))
			{
				Status stat = new Status(
				 IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(),
				 DebugException.NOT_SUPPORTED, DebugUIMessages.AbstractTableRendering_11, null  
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
				 DebugException.NOT_SUPPORTED, DebugUIMessages.AbstractTableRendering_11, null  
				);
				DebugException e = new DebugException(stat);
				throw e;
			}
			
			// load at the address
			reloadTable(address);
			fTableViewer.setSelection(address);
			
			updateSyncSelectedAddress(address);

			if (!isDynamicLoad())
			{						
				updateSyncPageStartAddress(address);
			}
			
			updateSyncTopAddress(address);
		}
	}			
	
	public void refresh() {
		fTableViewer.refresh();
	}

	public void resizeColumnsToPreferredSize() {
		fTableViewer.resizeColumnsToPreferredSize();
		
		if (!fIsShowAddressColumn)
		{
			fTableViewer.getTable().getColumn(0).setWidth(0);
		}
	}

	/**
	 * Updates labels of this rendering.
	 */
	public void updateLabels()
	{
		// update tab labels
		updateRenderingLabel(true);
		
		if (fTableViewer != null)
		{
			// update column labels
			setColumnHeadings();
			
			// rebuild cache and force labels to be refreshed
			fTableViewer.formatViewer();
		}
	}
	
	/**
	 * Fills the context menu for this rendering
	 * 
	 * @param menu menu to fill
	 */
	protected void fillContextMenu(IMenuManager menu) {
	
		menu.add(new Separator("topMenu")); //$NON-NLS-1$
		menu.add(fResetMemoryBlockAction);
		menu.add(fGoToAddressAction);
	
		menu.add(new Separator());
		
		menu.add(fFormatRenderingAction);

		if (!isDynamicLoad())
		{		
			menu.add(new Separator());
			menu.add(fPrevAction);
			menu.add(fNextAction);
		}
		
		menu.add(new Separator());
		menu.add(fReformatAction);
		menu.add(fToggleAddressColumnAction);
		menu.add(new Separator());
		menu.add(fCopyToClipboardAction);
		menu.add(fPrintViewTabAction);
		if (fPropertiesDialogAction != null)
		{
			menu.add(new Separator());
			menu.add(fPropertiesDialogAction);
		}
		
	}
	
	private int getPageSizeInUnits()
	{
		return fPageSize * getAddressableUnitPerLine();
	}
	
	private void getPageSizeFromPreference()
	{
		fPageSize = DebugUIPlugin.getDefault().getPreferenceStore().getInt(IDebugPreferenceConstants.PREF_TABLE_RENDERING_PAGE_SIZE);
	}
	
	private void updateDynamicLoadProperty() {
		
		boolean value = DebugUIPlugin
				.getDefault()
				.getPreferenceStore()
				.getBoolean(IDebugPreferenceConstants.PREF_DYNAMIC_LOAD_MEM);
		
		if (value != isDynamicLoad())
		{
			setDynamicLoad(value);
		
			if (!fIsDisposed) {
				if (isDynamicLoad()) {
					fContentInput.setPostBuffer(20);
					fContentInput.setPreBuffer(20);
					fContentInput.setDefaultBufferSize(20);
					fContentInput.setNumLines(getNumberOfVisibleLines());
	
				} else {
					fContentInput.setPostBuffer(0);
					fContentInput.setPreBuffer(0);
					fContentInput.setDefaultBufferSize(0);
					fContentInput.setNumLines(fPageSize);
				}	
			}
		}
	}
	
	private void getDynamicLoadFromPreference()
	{
		setDynamicLoad(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugPreferenceConstants.PREF_DYNAMIC_LOAD_MEM));
	}
	
	private boolean isDynamicLoad()
	{
		return fPresentationContext.isDynamicLoad();
	}
	
	private int getPageSize()
	{
		return fPageSize;
	}
	
	private int getNumLinesToLoad() {
		int numberOfLines = -1;
		
		if (isDynamicLoad())
			numberOfLines = getNumberOfVisibleLines();
		else
			numberOfLines = getPageSize();
		
		return numberOfLines;
	}
	
	private void setDynamicLoad(boolean load)
	{
		fPresentationContext.setDynamicLoad(load);
	}
	
	private void handlePageStartAddressChanged(BigInteger address)
	{
		// do not handle if in dynamic mode
		if (isDynamicLoad())
			return;
		
		if (!(getMemoryBlock() instanceof IMemoryBlockExtension))
			return;
		
		// do not handle event if the base address of the memory
		// block has changed, wait for debug event to update to
		// new location
		if (isMemoryBlockBaseAddressChanged())
			return;

		if(fTableViewer.getKey(0).equals(address))
			return;
	
		BigInteger start = fContentInput.getStartAddress();
		BigInteger end = fContentInput.getEndAddress();
		
		// smaller than start address, load at start address
		if (address.compareTo(start) < 0)
		{
			if (isAtTopLimit())
				return;
			
			address = start;
		}
		
		// bigger than end address, no need to load, alread at top
		if (address.compareTo(end) > 0)
		{
			if (isAtBottomLimit())
				return;
			
			address = end.subtract(BigInteger.valueOf(getPageSizeInUnits()));
		}
		
		fContentInput.setLoadAddress(address);
		Runnable runnable = new Runnable() {
			public void run() {
				refresh();
				fTableViewer.getTable().setTopIndex(0);
			}};
		
		runOnUIThread(runnable);

		updateSyncPageStartAddress(address);
		updateSyncTopAddress(address);
	}
	private void handleDyanicLoadChanged() {
		
		// if currently in dynamic load mode, update page
		// start address
		updateSyncPageStartAddress(getTopVisibleAddress());
		
		updateDynamicLoadProperty();
		if (isDynamicLoad())
		{
			refresh();
			fTableViewer.setTopIndex(getTopVisibleAddress());
		}
		else
		{
			BigInteger pageStart = (BigInteger)getSynchronizedProperty(IInternalDebugUIConstants.PROPERTY_PAGE_START_ADDRESS);
			if (pageStart == null)
				pageStart = getTopVisibleAddress();
			handlePageStartAddressChanged(pageStart);
		}
	}
	
	public void becomesHidden() {
		
		if (!fIsCreated)
			return;
		
		if (isVisible() == false)
		{
			// super should always be called
			super.becomesHidden();
			return;
		}

		super.becomesHidden();
		
		if (getMemoryBlock() instanceof IMemoryBlockExtension)
		{	
			updateRenderingLabel(false);
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRendering#becomesVisible()
	 */
	public void becomesVisible() {
		
		if (!fIsCreated)
			return;
		
		// do not do anything if already visible
		if (isVisible() == true)
		{
			// super should always be called
			super.becomesVisible();
			return;
		}
		
		super.becomesVisible();
		
		// TODO:  format not handled properly when the rendering becomes visible
		
		boolean value = DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugPreferenceConstants.PREF_DYNAMIC_LOAD_MEM);
		if (value != isDynamicLoad())
			// this call will cause a reload
			handleDyanicLoadChanged();
		else
			refresh();
		
		synchronize();
		updateRenderingLabel(true);
	}
	
	/**
	 * Handle column size changed event from synchronizer
	 * @param newColumnSize
	 */
	private void columnSizeChanged(final int newColumnSize) {
		// ignore event if rendering is not visible
		if (!isVisible())
			return;

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				int rowSize = getBytesPerLine();
				if (rowSize < newColumnSize)
					rowSize = newColumnSize;
					
				format(rowSize, newColumnSize);
			}
		});
	}
	
	/**
	 * @param newRowSize - new row size in number of bytes
	 */
	private void rowSizeChanged(final int newRowSize)
	{
		// ignore event if rendering is not visible
		if (!isVisible())
			return;
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				int size = newRowSize;
				if (size < getBytesPerColumn())
					size = getBytesPerColumn();
				
				format(size, getBytesPerColumn());
			}
		});		
	}
	
	/**
	 * Get properties from synchronizer and synchronize settings
	 */
	private void synchronize()
	{			
		if (!isDynamicLoad())
		{
			BigInteger pageStart = (BigInteger)getSynchronizedProperty(IInternalDebugUIConstants.PROPERTY_PAGE_START_ADDRESS);
			if (pageStart != null && fContentInput != null && fContentInput.getLoadAddress() != null)
			{
				if (!fContentInput.getLoadAddress().equals(pageStart))
					handlePageStartAddressChanged(pageStart);
			}
			else if (pageStart != null)
			{
				handlePageStartAddressChanged(pageStart);
			}
		}
		
		Integer rowSize = (Integer) getSynchronizedProperty(AbstractTableRendering.PROPERTY_ROW_SIZE);
		Integer columnSize = (Integer) getSynchronizedProperty(AbstractTableRendering.PROPERTY_COL_SIZE);
		BigInteger selectedAddress = (BigInteger)getSynchronizedProperty(AbstractTableRendering.PROPERTY_SELECTED_ADDRESS);
		BigInteger topAddress = (BigInteger)getSynchronizedProperty(AbstractTableRendering.PROPERTY_TOP_ADDRESS);
		
		if (rowSize != null)
		{
			int rSize = rowSize.intValue();
			if (rSize > 0 && rSize != fBytePerLine) {
				rowSizeChanged(rSize);
			}
		}
		
		if (columnSize != null) {
			int colSize = columnSize.intValue();
			if (colSize > 0 && colSize != fColumnSize) {
				columnSizeChanged(colSize);
			}
		}
		if (topAddress != null) {
			if (!topAddress.equals(getTopVisibleAddress())) {
				if (selectedAddress != null) {
					if (!getSelectedAddress().equals(selectedAddress)) {
						selectedAddressChanged(selectedAddress);
					}
				}
				topVisibleAddressChanged(topAddress);
			}
		}
		if (selectedAddress != null) {
			if (selectedAddress.compareTo(getSelectedAddress()) != 0) {
				selectedAddressChanged(selectedAddress);
			}
		}
	}
	
	/**
	 * update selected address in synchronizer if update is true.
	 */
	private void updateSyncSelectedAddress(BigInteger address) {
		
		if (!fIsCreated)
			return;
		PropertyChangeEvent event = new PropertyChangeEvent(this, AbstractTableRendering.PROPERTY_SELECTED_ADDRESS, null, address);
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
	 * update column size in synchronizer
	 */
	private void updateSyncRowSize() {
		
		if (!fIsCreated)
			return;
		
		PropertyChangeEvent event = new PropertyChangeEvent(this, AbstractTableRendering.PROPERTY_ROW_SIZE, null, new Integer(fBytePerLine));
		firePropertyChangedEvent(event);
	}
	
	/**
	 * update top visible address in synchronizer
	 */
	private void updateSyncTopAddress(BigInteger address) {
		
		if (!fIsCreated)
			return;

		PropertyChangeEvent event = new PropertyChangeEvent(this, AbstractTableRendering.PROPERTY_TOP_ADDRESS, null, address);
		firePropertyChangedEvent(event);
	}
	
	private void updateSyncPageStartAddress(BigInteger address) {
	
		if (!fIsCreated)
			return;
		
		if (isMemoryBlockBaseAddressChanged())
			return;
		
		PropertyChangeEvent event = new PropertyChangeEvent(this, IInternalDebugUIConstants.PROPERTY_PAGE_START_ADDRESS, null, address);
		firePropertyChangedEvent(event);
	}
	
	/**
	 * Returns the color provider for this rendering's memory block or
	 * <code>null</code> if none.
	 * <p>
	 * By default a color provider is obtained by aksing this rendering's
	 * memory bock for its {@link IColorProvider} adapter. When the color
	 * provider is queried for color information, it is provided with a
	 * {@link MemoryRenderingElement} as an argument. 
	 * </p>
	 * @return the color provider for this rendering's memory block,
	 *  or <code>null</code>
	 */
	protected IColorProvider getColorProviderAdapter()
	{
		return (IColorProvider)getMemoryBlock().getAdapter(IColorProvider.class);
	}
	
	/**
	 * Returns the label provider for this rendering's memory block or
	 * <code>null</code> if none.
	 * <p>
	 * By default a label provider is obtained by aksing this rendering's
	 * memory bock for its {@link ILabelProvider} adapter. When the label
	 * provider is queried for label information, it is provided with a
	 * {@link MemoryRenderingElement} as an argument. 
	 * </p>
	 * @return the label provider for this rendering's memory block,
	 *  or <code>null</code>
	 */
	protected ILabelProvider getLabelProviderAdapter()
	{
		return (ILabelProvider)getMemoryBlock().getAdapter(ILabelProvider.class);
	}
	
	/**
	 * Returns the font provider for this rendering's memory block or
	 * <code>null</code> if none.
	 * <p>
	 * By default a font provider is obtained by aksing this rendering's
	 * memory bock for its {@link IFontProvider} adapter. When the font
	 * provider is queried for font information, it is provided with a
	 * {@link MemoryRenderingElement} as an argument. 
	 * </p>
	 * @return the font provider for this rendering's memory block,
	 *  or <code>null</code>
	 */
	protected IFontProvider getFontProviderAdapter()
	{
		return (IFontProvider)getMemoryBlock().getAdapter(IFontProvider.class);
	}
	
	/**
	 * Returns the table presentation for this rendering's memory block or
	 * <code>null</code> if none.
	 * <p>
	 * By default a table presentation is obtained by aksing this rendering's
	 * memory bock for its {@link IMemoryBlockTablePresentation} adapter.
	 * </p>
	 * @return the table presentation for this rendering's memory block,
	 *  or <code>null</code>
	 */
	protected IMemoryBlockTablePresentation getTablePresentationAdapter()
	{
		return (IMemoryBlockTablePresentation)getMemoryBlock().getAdapter(IMemoryBlockTablePresentation.class);
	}
	
	/**
	 * Setup the viewer so it supports hovers to show the offset of each field
	 */
	private void createToolTip() {
		
		fToolTipShell = new Shell(DebugUIPlugin.getShell(), SWT.ON_TOP | SWT.RESIZE );
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginWidth = 2;
		gridLayout.marginHeight = 0;
		fToolTipShell.setLayout(gridLayout);
		fToolTipShell.setBackground(fTableViewer.getTable().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		
		final Control toolTipControl = createToolTipControl(fToolTipShell);
		
		if (toolTipControl == null)
		{
			// if client decide not to use tooltip support
			fToolTipShell.dispose();
			return;
		}
		
		MouseTrackAdapter listener = new MouseTrackAdapter(){
			
			private TableItem fTooltipItem = null;
			private int fCol = -1;
			
			public void mouseExit(MouseEvent e){
				
				if (!fToolTipShell.isDisposed())
					fToolTipShell.setVisible(false);
				fTooltipItem = null;
			}
			
			public void mouseHover(MouseEvent e){
				
				Point hoverPoint = new Point(e.x, e.y);
				Control control = null;
				
				if (e.widget instanceof Control)
					control = (Control)e.widget;
				
				if (control == null)
					return;
				
				hoverPoint = control.toDisplay(hoverPoint);
				TableItem item = getItem(hoverPoint);
				int column = getColumn(hoverPoint);
				
				//Only if there is a change in hover
				if(this.fTooltipItem != item || fCol != column){
					
					//Keep Track of the latest hover
					fTooltipItem = item;
					fCol = column;
					
					if(item != null){
						toolTipAboutToShow(toolTipControl, fTooltipItem, column);
						
						//Setting location of the tooltip
						Rectangle shellBounds = fToolTipShell.getBounds();
						shellBounds.x = hoverPoint.x;
						shellBounds.y = hoverPoint.y + item.getBounds(0).height;
						
						fToolTipShell.setBounds(shellBounds);
						fToolTipShell.pack();
						
						fToolTipShell.setVisible(true);
					}
					else {
						fToolTipShell.setVisible(false);
					}
				}
			}
		};
		
		fTableViewer.getTable().addMouseTrackListener(listener);
		fTableViewer.getCursor().addMouseTrackListener(listener);
	}
	
	/**
	 * Creates the control used to display tool tips for cells in this table. By default
	 * a label is used to display the address of the cell. Clients may override this
	 * method to create custom tooltip controls.
	 * <p>
	 * Also see the methods <code>getToolTipText(...)</code> and 
	 * <code>toolTipAboutToShow(...)</code>.
	 * </p>
	 * @param composite parent for the tooltip control
	 * @return the tooltip control to be displayed
	 * @since 3.2
	 */
	protected Control createToolTipControl(Composite composite) {
		Control fToolTipLabel = new Label(composite, SWT.NONE);
		fToolTipLabel.setForeground(fTableViewer.getTable().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		fToolTipLabel.setBackground(fTableViewer.getTable().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		fToolTipLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL |
				GridData.VERTICAL_ALIGN_CENTER));
		return fToolTipLabel;
	}
	
	/**
	 * Bug with table widget,BUG 113015, the widget is not able to return the correct
	 * table item if SWT.FULL_SELECTION is not on when the table is created.
	 * Created the following function to work around the problem.
	 * We can remove this method when the bug is fixed.
	 * @param point
	 * @return the table item where the point is located, return null if the item cannot be located.
	 */
	private TableItem getItem(Point point)
	{
		TableItem[] items = fTableViewer.getTable().getItems();
		for (int i=0; i<items.length; i++)
		{
			Point start = new Point(items[i].getBounds(0).x, items[i].getBounds(0).y);
			start = fTableViewer.getTable().toDisplay(start);
			Point end = new Point(start.x + items[i].getBounds(0).width, start.y + items[i].getBounds(0).height);
			
			if (start.y < point.y && point.y < end.y)
				return items[i];
		}
		return null;
	}
	
	/**
	 * Method for figuring out which column the point is located.
	 * @param point
	 * @return the column index where the point is located, return -1 if column is not found.
	 */
	private int getColumn(Point point)
	{
		int colCnt = fTableViewer.getTable().getColumnCount();
		TableItem item = fTableViewer.getTable().getItem(0);
		for (int i=0; i<colCnt; i++)
		{
			Point start = new Point(item.getBounds(i).x, item.getBounds(i).y);
			start = fTableViewer.getTable().toDisplay(start);
			Point end = new Point(start.x + item.getBounds(i).width, start.y + item.getBounds(i).height);
			
			if (start.x < point.x && end.x > point.x)
				return i;
		}
		return -1;
	}
	
	/**
	 * Called when the tool tip is about to show in this rendering.
	 * Clients who overrides <code>createTooltipControl</code> may need to
	 * also override this method to ensure that the tooltip shows up properly
	 * in their customized control.
	 * <p>
	 * By default a text tooltip is displayed, and the contents for the tooltip
	 * are generated by the <code>getToolTipText(...)</code> method.
	 * </p>
	 * @param toolTipControl - the control for displaying the tooltip
	 * @param item - the table item where the mouse is pointing.
	 * @param col - the column at which the mouse is pointing.
	 * @since 3.2
	 */
	protected void toolTipAboutToShow(Control toolTipControl, TableItem item,
			int col) {
		if (toolTipControl instanceof Label) {
			Object address = fTableViewer.getKey(fTableViewer.getTable().indexOf(item), col);
			if (address != null  && address instanceof BigInteger) {
				Object data = item.getData();
				if (data instanceof MemorySegment) {
					MemorySegment line = (MemorySegment) data;

					if (col > 0) {
						int start = (col - 1) * getBytesPerColumn();
						int end = start + getBytesPerColumn();
						MemoryByte[] bytes = line.getBytes(start, end);

						String str = getToolTipText((BigInteger)address, bytes);

						if (str != null)
							((Label) toolTipControl).setText(str);
					} else {
						String str = getToolTipText((BigInteger)address,
								new MemoryByte[] {});

						if (str != null)
							((Label) toolTipControl).setText(str);
					}
				}
			}
		}
	}
	
	/**
	 * Returns the text to display in a tool tip at the specified address
	 * for the specified bytes. By default the address of the bytes is displayed.
	 * Subclasses may override.
	 * 
	 * @param address address of cell that tool tip is displayed for 
	 * @param bytes the bytes in the cell
	 * @return the tooltip text for the memory bytes located at the specified
	 *         address
	 * @since 3.2
	 */
	protected String getToolTipText(BigInteger address, MemoryByte[] bytes)
	{
		StringBuffer buf = new StringBuffer("0x"); //$NON-NLS-1$
		buf.append(address.toString(16).toUpperCase());
		
		return buf.toString();
	}
	
	private void setColumnHeadings()
	{
		String[] columnLabels = new String[0];
		
		IMemoryBlockTablePresentation presentation = getTablePresentationAdapter();
		if (presentation != null)
		{
			columnLabels = presentation.getColumnLabels(getMemoryBlock(), getBytesPerLine(), getBytesPerLine()/getBytesPerColumn());
		}
		
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
	 * 
	 * Return this rendering's viewer
	 * @return this rendering's viewer
	 */
	public StructuredViewer getViewer()
	{
		return fTableViewer;
	}
	
	private boolean isMemoryBlockBaseAddressChanged()
	{
		try {
			BigInteger address = getMemoryBlockBaseAddress();
			BigInteger oldBaseAddress = fContentInput.getContentBaseAddress();
			if (!oldBaseAddress.equals(address))
				return true;
		} catch (DebugException e) {
			// fail silently
		}
		return false;
	}
	
	/**
	 * Returns text for the given memory bytes at the specified address for the specified
	 * rendering type. This is called by the label provider for.
	 * Subclasses must override.
	 * 
	 * @param renderingTypeId rendering type identifier
	 * @param address address where the bytes belong to
	 * @param data the bytes
	 * @return a string to represent the memory. Cannot not return <code>null</code>.
	 * 	Returns a string to pad the cell if the memory cannot be converted
	 *  successfully.
	 */
	abstract public String getString(String renderingTypeId, BigInteger address, MemoryByte[] data);
	
	/**
	 * Returns bytes for the given text corresponding to bytes at the given
	 * address for the specified rendering type. This is called by the cell modifier
	 * when modifying bytes in a memory block.
	 * Subclasses must convert the string value to an array of bytes.  The bytes will
	 * be passed to the debug adapter for memory block modification.
	 * Returns <code>null</code> if the bytes cannot be formatted properly.
	 * 
	 * @param renderingTypeId rendering type identifier
	 * @param address address the bytes begin at
	 * @param currentValues current values of the data in bytes format
	 * @param newValue the string to be converted to bytes
	 * @return the bytes converted from a string
	 */
	abstract public byte[] getBytes(String renderingTypeId, BigInteger address, MemoryByte[] currentValues, String newValue);
}
