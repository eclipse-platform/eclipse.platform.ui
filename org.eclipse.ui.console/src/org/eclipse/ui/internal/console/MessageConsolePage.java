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
package org.eclipse.ui.internal.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.console.actions.ClearOutputAction;
import org.eclipse.ui.console.actions.TextViewerAction;
import org.eclipse.ui.console.actions.TextViewerGotoLineAction;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * A page for a console connected to I/O streams of a process
 * 
 * @since 3.0
 */
public class MessageConsolePage implements IPageBookViewPage, IAdaptable, IPropertyChangeListener {

	//page site
	private IPageSite fSite = null;
	
	// viewer
	private MessageConsoleViewer fViewer = null;

	// the view this page is contained in
	private IConsoleView fView;
	
	// the console this page displays
	private MessageConsole fConsole;
	
	// text selection listener
	private ISelectionChangedListener fTextListener =  new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			updateSelectionDependentActions();
		}};

	// actions
	private ClearOutputAction fClearOutputAction;
	private Map fGlobalActions= new HashMap(10);
	private List fSelectionActions = new ArrayList(3);
	
	// menus
	private Menu fMenu;
	
	/**
	 * Constructs a new process page 
	 */
	public MessageConsolePage(IConsoleView view, MessageConsole console) {
		fView = view;
		fConsole = console;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPageBookViewPage#getSite()
	 */
	public IPageSite getSite() {
		return fSite; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPageBookViewPage#init(org.eclipse.ui.part.IPageSite)
	 */
	public void init(IPageSite site) {
		fSite = site;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		fViewer = new MessageConsoleViewer(parent);
		fViewer.setDocument(getConsole().getDocument());
		fViewer.getTextWidget().setTabs(getConsole().getTabWidth());
		
		MenuManager manager= new MenuManager("#MessageConsole", "#MessageConsole"); //$NON-NLS-1$ //$NON-NLS-2$
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager m) {
				contextMenuAboutToShow(m);
			}
		});
		fMenu= manager.createContextMenu(getControl());
		getControl().setMenu(fMenu);
		
		IPageSite site= getSite();
		site.registerContextMenu(ConsolePlugin.getUniqueIdentifier() + ".messageConsole", manager, getViewer()); //$NON-NLS-1$
		site.setSelectionProvider(getViewer());
		
		createActions();
		configureToolBar(getSite().getActionBars().getToolBarManager());
		
		fViewer.getSelectionProvider().addSelectionChangedListener(fTextListener);
		setFont(getConsole().getFont());
		getConsole().addPropertyChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		Object source = event.getSource();
		String property = event.getProperty();
		if (source.equals(getConsole()) && MessageConsole.P_FONT.equals(property)) {
			setFont(getConsole().getFont());	
		} else if (MessageConsole.P_STREAM_COLOR.equals(property) && source instanceof MessageConsoleStream) {
			MessageConsoleStream stream = (MessageConsoleStream)source;
			if (stream.getConsole().equals(getConsole())) {
				getViewer().getTextWidget().redraw();
			}
		} else if (source.equals(getConsole()) && property.equals(MessageConsole.P_TAB_SIZE)) {
			if (fViewer != null) {
				fViewer.getTextWidget().setTabs(getConsole().getTabWidth());
				fViewer.getTextWidget().redraw();
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#dispose()
	 */
	public void dispose() {
		getConsole().removePropertyChangeListener(this);
		fViewer.getSelectionProvider().removeSelectionChangedListener(fTextListener);
		
		if (fMenu != null && !fMenu.isDisposed()) {
			fMenu.dispose();
			fMenu= null;
		}
		
		if (fViewer != null) {
			fViewer.dispose();
			fViewer = null;
		}
		fSite = null;
		fSelectionActions.clear();
	}
	

	/**
	 * Fill the context menu
	 * 
	 * @param menu menu
	 */
	protected void contextMenuAboutToShow(IMenuManager menu) {
		menu.add((IAction)fGlobalActions.get(ActionFactory.COPY.getId()));
		menu.add((IAction)fGlobalActions.get(ActionFactory.SELECT_ALL.getId()));						
		menu.add(new Separator("FIND")); //$NON-NLS-1$
		menu.add((IAction)fGlobalActions.get(ActionFactory.FIND.getId()));
		menu.add((IAction)fGlobalActions.get(ITextEditorActionConstants.GOTO_LINE));
		menu.add(fClearOutputAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#getControl()
	 */
	public Control getControl() {
		if (fViewer != null) {
			return fViewer.getControl();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
	 */
	public void setActionBars(IActionBars actionBars) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#setFocus()
	 */
	public void setFocus() {
		Control control = getControl(); 
		if (control != null) {
			control.setFocus();
		}
		updateSelectionDependentActions();
	}
	
	protected void createActions() {
		fClearOutputAction= new ClearOutputAction(getViewer());
		
		// In order for the clipboard actions to accessible via their shortcuts
		// (e.g., Ctrl-C, Ctrl-V), we *must* set a global action handler for
		// each action		
		IActionBars actionBars= getSite().getActionBars();
		TextViewerAction action= new TextViewerAction(getViewer(), ITextOperationTarget.COPY);
		action.configureAction(ConsoleMessages.getString("MessageConsolePage.&Copy@Ctrl+C_6"), ConsoleMessages.getString("MessageConsolePage.Copy_7"), ConsoleMessages.getString("MessageConsolePage.Copy_7")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));		
		setGlobalAction(actionBars, ActionFactory.COPY.getId(), action);
		action= new TextViewerAction(getViewer(), ITextOperationTarget.SELECT_ALL);
		action.configureAction(ConsoleMessages.getString("MessageConsolePage.Select_&All@Ctrl+A_12"), ConsoleMessages.getString("MessageConsolePage.Select_All"), ConsoleMessages.getString("MessageConsolePage.Select_All")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		setGlobalAction(actionBars, ActionFactory.SELECT_ALL.getId(), action);
		
		//XXX Still using "old" resource access
		ResourceBundle bundle= ResourceBundle.getBundle("org.eclipse.ui.internal.console.ConsoleMessages"); //$NON-NLS-1$
		setGlobalAction(actionBars, ActionFactory.FIND.getId(), new FindReplaceAction(bundle, "find_replace_action.", getConsoleView())); //$NON-NLS-1$
	
		action= new TextViewerGotoLineAction(getViewer());
		setGlobalAction(actionBars, ITextEditorActionConstants.GOTO_LINE, action);
								
		actionBars.updateActionBars();
				
		fSelectionActions.add(ActionFactory.COPY.getId());
		fSelectionActions.add(ActionFactory.FIND.getId());
	}
	
	protected void updateSelectionDependentActions() {
		Iterator iterator= fSelectionActions.iterator();
		while (iterator.hasNext()) {
			updateAction((String)iterator.next());		
		}
	}	
	
	protected void updateAction(String actionId) {
		IAction action= (IAction)fGlobalActions.get(actionId);
		if (action instanceof IUpdate) {
			((IUpdate) action).update();
		}
	}	
		
	protected void setGlobalAction(IActionBars actionBars, String actionID, IAction action) {
		fGlobalActions.put(actionID, action); 
		actionBars.setGlobalActionHandler(actionID, action);
	}
		
	/**
	 * Returns the viewer in this page.
	 * 
	 * @return the viewer in this page
	 */
	protected MessageConsoleViewer getViewer() {
		return fViewer;
	}	

	protected void configureToolBar(IToolBarManager mgr) {
		mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fClearOutputAction);
	}

	/**
	 * Returns the view this page is contained in
	 * 
	 * @return the view this page is contained in
	 */
	protected IConsoleView getConsoleView() {
		return fView;
	}
	
	/**
	 * Returns the console this page is displaying
	 * 
	 * @return the console this page is displaying
	 */
	protected MessageConsole getConsole() {
		return fConsole;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class required) {
		if (IFindReplaceTarget.class.equals(required)) {
			return getViewer().getFindReplaceTarget();
		}
		if (Widget.class.equals(required)) {
			return getViewer().getTextWidget();
		}
		return null;
	}	

	/**
	 * Sets the font for this page.
	 * 
	 * @param font font
	 */
	protected void setFont(Font font) {
		getViewer().getTextWidget().setFont(font);
	}

	/**
	 * Refreshes this page
	 */
	protected void refresh() {
		getViewer().refresh();
	}
}
