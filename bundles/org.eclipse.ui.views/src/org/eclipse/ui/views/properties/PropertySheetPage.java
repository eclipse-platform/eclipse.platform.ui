/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.properties;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IContext;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.part.Page;
import org.osgi.framework.Bundle;

/**
 * The standard implementation of property sheet page which presents
 * a table of property names and values obtained from the current selection
 * in the active workbench part.
 * <p>
 * This page obtains the information about what to properties display from 
 * the current selection (which it tracks). 
 * </p>
 * <p>
 * The model for this page is a hierarchy of <code>IPropertySheetEntry</code>.
 * The page may be configured with a custom model by setting the root entry.
 * <p>
 * If no root entry is set then a default model is created which uses the
 * <code>IPropertySource</code> interface to obtain the properties of
 * the current slection. This requires that the selected objects provide an
 * <code>IPropertySource</code> adapter (or implement 
 * <code>IPropertySource</code> directly). This restiction can be overcome
 * by providing this page with an <code>IPropertySourceProvider</code>. If
 * supplied, this provider will be used by the default model to obtain a
 * property source for the current selection 
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @see IPropertySource
 */
public class PropertySheetPage extends Page implements IPropertySheetPage {
	/**
	 * Help context id 
	 * (value <code>"org.eclipse.ui.property_sheet_page_help_context"</code>).
	 */
	public static final String HELP_CONTEXT_PROPERTY_SHEET_PAGE = "org.eclipse.ui.property_sheet_page_help_context"; //$NON-NLS-1$

	private PropertySheetViewer viewer;
	private IPropertySheetEntry rootEntry;
	private IPropertySourceProvider provider;

	private DefaultsAction defaultsAction;
	private FilterAction filterAction;
	private CategoriesAction categoriesAction;
	private CopyPropertyAction copyAction;

	private ICellEditorActivationListener cellEditorActivationListener;
	private CellEditorActionHandler cellEditorActionHandler;

	private Clipboard clipboard;
	/**
	 * Creates a new property sheet page.
	 */
	public PropertySheetPage() {
		super();
	}
	/* (non-Javadoc)
	 * Method declared on <code>IPage</code>.
	 */
	public void createControl(Composite parent) {
		// create a new viewer
		viewer = new PropertySheetViewer(parent);

		// set the model for the viewer
		if (rootEntry == null) {
			// create a new root
			PropertySheetEntry root = new PropertySheetEntry();
			if (provider != null)
				// set the property source provider
				root.setPropertySourceProvider(provider);
			rootEntry = root;
		}
		viewer.setRootEntry(rootEntry);
		viewer.addActivationListener(getCellEditorActivationListener());
		// add a listener to track when the entry selection changes
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleEntrySelection(event.getSelection());
			}
		});
		initDragAndDrop();
		makeActions();
		
		// Create the popup menu for the page.
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.add(copyAction);
		menuMgr.add(new Separator());
		menuMgr.add(defaultsAction);
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		// Set help on the viewer 
		viewer.getControl().addHelpListener(new HelpListener() {
			/*
			 * @see HelpListener#helpRequested(HelpEvent)
			 */
			public void helpRequested(HelpEvent e) {
				// Get the context for the selected item
				IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
				if (!selection.isEmpty()) {
					IPropertySheetEntry entry = (IPropertySheetEntry)selection.getFirstElement();
					Object helpContextId = entry.getHelpContextIds();
					if (helpContextId != null) {
						if (helpContextId instanceof String) {
							WorkbenchHelp.displayHelp((String)helpContextId);
							return;
						}

						// Since 2.0 the only valid type for helpContextIds
						// is a String (a single id).
						// However for backward compatibility we have to handle
						// and array of contexts (Strings and/or IContexts) 
						// or a context computer.
						Object[] contexts = null;
						if (helpContextId instanceof IContextComputer) {
							// get local contexts
							contexts = ((IContextComputer)helpContextId).getLocalContexts(e);
						} else {
							contexts = (Object[])helpContextId;
						}
						// Ignore all but the first element in the array
						if (contexts[0] instanceof IContext) 
							WorkbenchHelp.displayHelp((IContext)contexts[0]);
						else
							WorkbenchHelp.displayHelp((String)contexts[0]);
						return;
					}
				}
				
				// No help for the selection so show page help
				WorkbenchHelp.displayHelp(HELP_CONTEXT_PROPERTY_SHEET_PAGE);
			}
		});
	}
	/**
	 * The <code>PropertySheetPage</code> implementation of this <code>IPage</code> method 
	 * disposes of this page's entries.
	 */
	public void dispose() {
		super.dispose();
		if (rootEntry != null) {
			rootEntry.dispose();
			rootEntry = null;
		}
		if (clipboard != null) {
			clipboard.dispose();
			clipboard = null;
		}			
	}
	/**
	 * Returns the cell editor activation listener for this page
	 * @return ICellEditorActivationListener the cell editor activation listener for this page
	 */
	private ICellEditorActivationListener getCellEditorActivationListener() {
		if (cellEditorActivationListener == null) {
			cellEditorActivationListener = new ICellEditorActivationListener() {
				public void cellEditorActivated(CellEditor cellEditor) {
					if (cellEditorActionHandler != null)
						cellEditorActionHandler.addCellEditor(cellEditor);
				}
				public void cellEditorDeactivated(CellEditor cellEditor) {
					if (cellEditorActionHandler != null)
						cellEditorActionHandler.removeCellEditor(cellEditor);
				}
			};
		}
		return cellEditorActivationListener;
	}
	/* (non-Javadoc)
	 * Method declared on IPage (and Page).
	 */
	public Control getControl() {
		if (viewer == null)
			return null;
		return viewer.getControl();
	}
	/**
	 * Returns the image descriptor with the given relative path.
	 * @param relativePath The path relative to the workspace root
	 * @return ImageDescriptor the descriptor for the receiver
	 */
	private ImageDescriptor getImageDescriptor(String relativePath) {
		String iconPath = "icons/full/"; //$NON-NLS-1$
		
		Bundle bundle = Platform.getBundle(PlatformUI.PLUGIN_ID);
		URL url =  bundle.getEntry(iconPath + relativePath);
		return ImageDescriptor.createFromURL(url);
		
	}
	/**
	 * Handles a selection change in the entry table.
	 *
	 * @param selection the new selection
	 */
	public void handleEntrySelection(ISelection selection) {
		if (defaultsAction != null) {
			if (selection.isEmpty()) {
				defaultsAction.setEnabled(false);
				return;
			}
			// see if item is editable
			boolean editable = viewer.getActiveCellEditor() != null;
			defaultsAction.setEnabled(editable);
		}
	}
	/**
	 * Adds drag and drop support.
	 */
	protected void initDragAndDrop() {
		int operations = DND.DROP_COPY;
		Transfer[] transferTypes = new Transfer[]{
			TextTransfer.getInstance()};
		DragSourceListener listener = new DragSourceAdapter() {
			public void dragSetData(DragSourceEvent event){
				performDragSetData(event);
			}
			public void dragFinished(DragSourceEvent event){
				//Nothing to do here
			}
		};
		DragSource dragSource = new DragSource(((TableTree)viewer.getControl()).getTable(), operations);
		dragSource.setTransfer(transferTypes);
		dragSource.addDragListener(listener);
	}
	/**
	 * The user is attempting to drag.  Add the appropriate
	 * data to the event.
	 * @param event The event sent from the drag and drop support.
	 */
	void performDragSetData(DragSourceEvent event) {
		// Get the selected property
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		if (selection.isEmpty()) 
			return;
		// Assume single selection
		IPropertySheetEntry entry = (IPropertySheetEntry)selection.getFirstElement();

		// Place text as the data
		StringBuffer buffer = new StringBuffer();
		buffer.append(entry.getDisplayName());
		buffer.append("\t"); //$NON-NLS-1$
		buffer.append(entry.getValueAsString());
		
		event.data = buffer.toString();			
	}
	/**
	 * Make action objects.
	 */
	private void makeActions() {
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		
		// Restore Default Value
		defaultsAction = new DefaultsAction(viewer, "defaults"); //$NON-NLS-1$
		defaultsAction.setText(PropertiesMessages.getString("Defaults.text")); //$NON-NLS-1$
		defaultsAction.setToolTipText(PropertiesMessages.getString("Defaults.toolTip")); //$NON-NLS-1$
		defaultsAction.setImageDescriptor(getImageDescriptor("elcl16/defaults_ps.gif")); //$NON-NLS-1$
		defaultsAction.setDisabledImageDescriptor(getImageDescriptor("dlcl16/defaults_ps.gif")); //$NON-NLS-1$
		defaultsAction.setEnabled(false);

		// Show Advanced Properties
		filterAction = new FilterAction(viewer, "filter"); //$NON-NLS-1$
		filterAction.setText(PropertiesMessages.getString("Filter.text")); //$NON-NLS-1$
		filterAction.setToolTipText(PropertiesMessages.getString("Filter.toolTip")); //$NON-NLS-1$
		filterAction.setImageDescriptor(getImageDescriptor("elcl16/filter_ps.gif")); //$NON-NLS-1$
		filterAction.setChecked(false);

		// Show Categories
		categoriesAction = new CategoriesAction(viewer, "categories"); //$NON-NLS-1$
		categoriesAction.setText(PropertiesMessages.getString("Categories.text")); //$NON-NLS-1$
		categoriesAction.setToolTipText(PropertiesMessages.getString("Categories.toolTip")); //$NON-NLS-1$
		categoriesAction.setImageDescriptor(getImageDescriptor("elcl16/tree_mode.gif")); //$NON-NLS-1$
		categoriesAction.setChecked(true);
	
		// Copy	
		Shell shell = viewer.getControl().getShell();
		clipboard = new Clipboard(shell.getDisplay());
		copyAction = new CopyPropertyAction(viewer, "copy", clipboard); //$NON-NLS-1$
		copyAction.setText(PropertiesMessages.getString("CopyProperty.text")); //$NON-NLS-1$
		copyAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
	}
	/* (non-Javadoc)
	 * Method declared on IPage (and Page).
	 */
	public void makeContributions(IMenuManager menuManager, IToolBarManager toolBarManager, IStatusLineManager statusLineManager) {

		// add actions to the tool bar
		toolBarManager.add(categoriesAction);
		toolBarManager.add(filterAction);
		toolBarManager.add(defaultsAction);

		// add actions to the menu
		menuManager.add(categoriesAction);
		menuManager.add(filterAction);
		
		// set status line manager into the viewer
		viewer.setStatusLineManager(statusLineManager);
	}
	/**
	 * Updates the model for the viewer.
	 * <p>
	 * Note that this means ensuring that the model reflects the state
	 * of the current viewer input. 
	 * </p>
	 */
	public void refresh() {
		if (viewer == null)
			return;
		// calling setInput on the viewer will cause the model to refresh
		viewer.setInput(viewer.getInput());
	}
	/* (non-Javadoc)
	 * Method declared on ISelectionListener.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (viewer == null)
			return;

		// change the viewer input since the workbench selection has changed.
		if (selection instanceof IStructuredSelection) {
			viewer.setInput(((IStructuredSelection) selection).toArray());
		}
	}
	/**
	 * The <code>PropertySheetPage</code> implementation of this <code>IPage</code> method
	 * calls <code>makeContributions</code> for backwards compatibility with
	 * previous versions of <code>IPage</code>. 
	 * <p>
	 * Subclasses may reimplement.
	 * </p>
	 */
	public void setActionBars(IActionBars actionBars) {
		super.setActionBars(actionBars);
		cellEditorActionHandler = new CellEditorActionHandler(actionBars);
		cellEditorActionHandler.setCopyAction(copyAction);
	}
	/**
	 * Sets focus to a part in the page.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	/**
	 * Sets the given property source provider as
	 * the property source provider.
	 * <p>
	 * Calling this method is only valid if you are using
	 * this page's default root entry.
	 * </p>
	 * @param newProvider the property source provider
	 */
	public void setPropertySourceProvider(IPropertySourceProvider newProvider) {
		provider = newProvider;
		if (rootEntry instanceof PropertySheetEntry) {
			((PropertySheetEntry) rootEntry).setPropertySourceProvider(provider);
			// the following will trigger an update
			viewer.setRootEntry(rootEntry);
		}
	}
	/**
	 * Sets the given entry as the model for the page.
	 *
	 * @param entry the root entry
	 */
	public void setRootEntry(IPropertySheetEntry entry) {
		rootEntry = entry;
		if (viewer != null)
			// the following will trigger an update
			viewer.setRootEntry(rootEntry);
	}
}
