/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.ClearOutputAction;
import org.eclipse.debug.internal.ui.actions.FollowHyperlinkAction;
import org.eclipse.debug.internal.ui.actions.KeyBindingFollowHyperlinkAction;
import org.eclipse.debug.internal.ui.actions.TextViewerAction;
import org.eclipse.debug.internal.ui.actions.TextViewerGotoLineAction;
import org.eclipse.debug.internal.ui.console.IConsoleView;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * A page for a console connected to I/O streams of a process
 * 
 * @since 3.0
 */
public class ProcessConsolePage implements IPageBookViewPage, ISelectionListener, IAdaptable, IShowInSource, IShowInTargetList, IDebugEventSetListener {

	//page site
	private IPageSite fSite = null;
	
	// viewer
	private ConsoleViewer fViewer = null;

	// the view this page is contained in
	private IConsoleView fView;
	
	// the console this page displays
	private ProcessConsole fConsole;
	
	// scroll lock
	private boolean fIsLocked = false;
	
	// text selection listener
	private ISelectionChangedListener fTextListener =  new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			updateSelectionDependentActions();
		}};

	// actions
	private ClearOutputAction fClearOutputAction;
	private Map fGlobalActions= new HashMap(10);
	protected List fSelectionActions = new ArrayList(3);
	private FollowHyperlinkAction fFollowLinkAction;
	private ScrollLockAction fScrollLockAction;
	private ConsoleTerminateAction fTerminate;
	private ConsoleRemoveAllTerminatedAction fRemoveTerminated;
	private KeyBindingFollowHyperlinkAction fKeyBindingFollowLinkAction;
	
	// menus
	private Menu fMenu;

	/**
	 * Constructs a new process page 
	 */
	public ProcessConsolePage(IConsoleView view, ProcessConsole console) {
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
	public void init(IPageSite site) throws PartInitException {
		fSite = site;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		fViewer = new ConsoleViewer(parent);
		fViewer.setDocument(DebugUIPlugin.getDefault().getConsoleDocumentManager().getConsoleDocument(getProcess()));
		
		MenuManager manager= new MenuManager("#ProcessConsole", "#ProcessConsole"); //$NON-NLS-1$ //$NON-NLS-2$
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager m) {
				contextMenuAboutToShow(m);
			}
		});
		fMenu= manager.createContextMenu(getControl());
		getControl().setMenu(fMenu);
		
		IPageSite site= getSite();
		site.registerContextMenu(DebugUIPlugin.getUniqueIdentifier() + ".processConsole", manager, getConsoleViewer()); //$NON-NLS-1$
		site.setSelectionProvider(getConsoleViewer());
		
		createActions();
		configureToolBar(getSite().getActionBars().getToolBarManager());
		
		getSite().getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		fViewer.getSelectionProvider().addSelectionChangedListener(fTextListener);
	}

	/**
	 * Fill the context menu
	 * 
	 * @param menu menu
	 */
	protected void contextMenuAboutToShow(IMenuManager menu) {
		ConsoleDocument doc= (ConsoleDocument)getConsoleViewer().getDocument();
		if (doc == null) {
			return;
		}
		if (doc.isReadOnly()) {
			menu.add((IAction)fGlobalActions.get(IWorkbenchActionConstants.COPY));
			menu.add((IAction)fGlobalActions.get(IWorkbenchActionConstants.SELECT_ALL));						
		} else {
			updateAction(IWorkbenchActionConstants.PASTE);
			menu.add((IAction)fGlobalActions.get(IWorkbenchActionConstants.CUT));
			menu.add((IAction)fGlobalActions.get(IWorkbenchActionConstants.COPY));
			menu.add((IAction)fGlobalActions.get(IWorkbenchActionConstants.PASTE));
			menu.add((IAction)fGlobalActions.get(IWorkbenchActionConstants.SELECT_ALL));
		}

		menu.add(new Separator("FIND")); //$NON-NLS-1$
		menu.add((IAction)fGlobalActions.get(IWorkbenchActionConstants.FIND));
		menu.add((IAction)fGlobalActions.get(ITextEditorActionConstants.GOTO_LINE));
		fFollowLinkAction.setEnabled(fFollowLinkAction.getHyperLink() != null);
		menu.add(fFollowLinkAction);
		menu.add(fClearOutputAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));	
		menu.add(fTerminate);	
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#dispose()
	 */
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		getSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		fViewer.getSelectionProvider().removeSelectionChangedListener(fTextListener);
		
		if (fRemoveTerminated != null) {
			fRemoveTerminated.dispose();
		}
		
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
		fClearOutputAction= new ClearOutputAction(getConsoleViewer());
		fRemoveTerminated = new ConsoleRemoveAllTerminatedAction();
		
		// In order for the clipboard actions to accessible via their shortcuts
		// (e.g., Ctrl-C, Ctrl-V), we *must* set a global action handler for
		// each action		
		IActionBars actionBars= getSite().getActionBars();
		TextViewerAction action= new TextViewerAction(getConsoleViewer(), ITextOperationTarget.CUT);
		action.configureAction(DebugUIViewsMessages.getString("ConsoleView.Cu&t@Ctrl+X_3"), DebugUIViewsMessages.getString("ConsoleView.Cut_4"), DebugUIViewsMessages.getString("ConsoleView.Cut_4")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		action.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
		action.setHoverImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_CUT_HOVER));
		setGlobalAction(actionBars, IWorkbenchActionConstants.CUT, action);
		action= new TextViewerAction(getConsoleViewer(), ITextOperationTarget.COPY);
		action.configureAction(DebugUIViewsMessages.getString("ConsoleView.&Copy@Ctrl+C_6"), DebugUIViewsMessages.getString("ConsoleView.Copy_7"), DebugUIViewsMessages.getString("ConsoleView.Copy_7")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		action.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		action.setHoverImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY_HOVER));		
		setGlobalAction(actionBars, IWorkbenchActionConstants.COPY, action);
		action= new TextViewerAction(getConsoleViewer(), ITextOperationTarget.PASTE);
		action.configureAction(DebugUIViewsMessages.getString("ConsoleView.&Paste@Ctrl+V_9"), DebugUIViewsMessages.getString("ConsoleView.Paste_10"), DebugUIViewsMessages.getString("ConsoleView.Paste_Clipboard_Text_11")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		action.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
		action.setHoverImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_HOVER));		
		setGlobalAction(actionBars, IWorkbenchActionConstants.PASTE, action);
		action= new TextViewerAction(getConsoleViewer(), ITextOperationTarget.SELECT_ALL);
		action.configureAction(DebugUIViewsMessages.getString("ConsoleView.Select_&All@Ctrl+A_12"), DebugUIViewsMessages.getString("ConsoleView.Select_All"), DebugUIViewsMessages.getString("ConsoleView.Select_All")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		setGlobalAction(actionBars, IWorkbenchActionConstants.SELECT_ALL, action);
		
		//XXX Still using "old" resource access
		ResourceBundle bundle= ResourceBundle.getBundle("org.eclipse.debug.internal.ui.views.DebugUIViewsMessages"); //$NON-NLS-1$
		setGlobalAction(actionBars, IWorkbenchActionConstants.FIND, new FindReplaceAction(bundle, "find_replace_action.", getConsoleView())); //$NON-NLS-1$
	
		action= new TextViewerGotoLineAction(getConsoleViewer());
		setGlobalAction(actionBars, ITextEditorActionConstants.GOTO_LINE, action);
		
		fFollowLinkAction = new FollowHyperlinkAction(getConsoleViewer());
		
		fKeyBindingFollowLinkAction= new KeyBindingFollowHyperlinkAction(getConsoleViewer(), actionBars);
		fKeyBindingFollowLinkAction.setActionDefinitionId("org.eclipse.jdt.ui.edit.text.java.open.editor"); //$NON-NLS-1$
		getConsoleView().getSite().getKeyBindingService().registerAction(fKeyBindingFollowLinkAction);
		
		fScrollLockAction = new ScrollLockAction(getConsoleViewer());
		fScrollLockAction.setChecked(fIsLocked);
		getConsoleViewer().setAutoScroll(!fIsLocked);
						
		actionBars.updateActionBars();
		
		getConsoleViewer().getTextWidget().addVerifyKeyListener(new VerifyKeyListener() {
			public void verifyKey(VerifyEvent event) {
				if (event.stateMask == SWT.CTRL && event.keyCode == 0 && event.character == 0x0C) {
					IAction gotoLine= (IAction)fGlobalActions.get(ITextEditorActionConstants.GOTO_LINE);
					if (gotoLine.isEnabled()) {
						gotoLine.run();
						event.doit= false;
					}
				}
			}
		});
		
		fTerminate = new ConsoleTerminateAction(getConsole());
		DebugPlugin.getDefault().addDebugEventListener(this);
		
		fSelectionActions.add(IWorkbenchActionConstants.CUT);
		fSelectionActions.add(IWorkbenchActionConstants.COPY);
		fSelectionActions.add(IWorkbenchActionConstants.PASTE);
		fSelectionActions.add(IWorkbenchActionConstants.FIND);
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
	 * Returns the console viewer in this page.
	 * 
	 * @return the console viewer in this page
	 */
	protected ConsoleViewer getConsoleViewer() {
		return fViewer;
	}	

	/**
	 * @see AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager mgr) {
		mgr.appendToGroup(IDebugUIConstants.LAUNCH_GROUP, fTerminate);
		mgr.appendToGroup(IDebugUIConstants.LAUNCH_GROUP, fRemoveTerminated);
		//mgr.add(fProcessDropDownAction);
		mgr.appendToGroup(IDebugUIConstants.OUTPUT_GROUP, fScrollLockAction);
		mgr.appendToGroup(IDebugUIConstants.OUTPUT_GROUP, fClearOutputAction);
	}

	/**
	 * Returns the process associated with this page
	 * 
	 * @return the process associated with this page
	 */
	protected IProcess getProcess() {
		return getConsole().getProcess();
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
	protected ProcessConsole getConsole() {
		return fConsole;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (getProcess().equals(DebugUITools.getCurrentProcess())) {
			getConsoleView().display(getConsole());
		}
	}
	
	/**
	 * @see WorkbenchPart#getAdapter(Class)
	 */
	public Object getAdapter(Class required) {
		if (IFindReplaceTarget.class.equals(required)) {
			return getConsoleViewer().getFindReplaceTarget();
		}
		if (Widget.class.equals(required)) {
			return getConsoleViewer().getTextWidget();
		}
		if (IShowInSource.class.equals(required)) {
			return this;
		}
		if (IShowInTargetList.class.equals(required)) {
			return this; 
		}
		return null;
	}	
	
	/**
	 * @see IShowInSource#getShowInContext()
	 */
	public ShowInContext getShowInContext() {
		IProcess process = getProcess();
		if (process == null) {
			return null;
		} else {
			IDebugTarget target = (IDebugTarget)process.getAdapter(IDebugTarget.class);
			ISelection selection = null;
			if (target == null) {
				selection = new StructuredSelection(process);
			} else {
				selection = new StructuredSelection(target);
			}
			return new ShowInContext(null, selection);
		}
	}
	
	/**
	 * @see IShowInTargetList#getShowInTargetIds()
	 */
	public String[] getShowInTargetIds() {
		return new String[] {IDebugUIConstants.ID_DEBUG_VIEW};
	}
	
	/**
	 * Update terminate action.
	 * 
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getSource().equals(getProcess())) {
				Runnable r = new Runnable() {
					public void run() {
						if (isAvailable()) {
							fTerminate.update();
						}				
					}
				};
				if (isAvailable()) {				
					getControl().getDisplay().asyncExec(r);
				}
			}
		}
	}

	/**
	 * Returns whether this page's controls are available.
	 * 
	 * @return whether this page's controls are available
	 */
	protected boolean isAvailable() {
		return getControl() != null;
	}
		
}
