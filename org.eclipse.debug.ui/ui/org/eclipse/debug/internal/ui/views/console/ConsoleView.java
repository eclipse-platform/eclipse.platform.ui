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

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.actions.ClearOutputAction;
import org.eclipse.debug.internal.ui.actions.FollowHyperlinkAction;
import org.eclipse.debug.internal.ui.actions.KeyBindingFollowHyperlinkAction;
import org.eclipse.debug.internal.ui.actions.TextViewerAction;
import org.eclipse.debug.internal.ui.actions.TextViewerGotoLineAction;
import org.eclipse.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.console.ConsoleColorProvider;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;

public class ConsoleView extends AbstractDebugEventHandlerView implements IDocumentListener, ISelectionListener, IShowInSource, IShowInTargetList {

	protected ClearOutputAction fClearOutputAction= null;
	protected FollowHyperlinkAction fFollowLinkAction = null;
	protected KeyBindingFollowHyperlinkAction fKeyBindingFollowLinkAction = null;
	protected ProcessDropDownAction fProcessDropDownAction = null;
	protected ScrollLockAction fScrollLockAction = null;
	private boolean fIsLocked = false;

	protected Map fGlobalActions= new HashMap(10);
	protected List fSelectionActions = new ArrayList(3);
	
	protected IDocument fCurrentDocument= null;
	
	protected int fMode = MODE_CURRENT_PROCESS;
	
	// view modes
	/**
	 * Shows the output of the selected process in the debug view,
	 * or the current process when there is no debug view
	 */
	public static final int MODE_CURRENT_PROCESS = 1;
		
	/**
	 * Shows the output of a specific process
	 */
	public static final int MODE_SPECIFIC_PROCESS = 2;
	
	/**
	 * The current process being viewed, or <code>null</code.
	 */
	private IProcess fProcess;
	
	/**
	 * Current launch listener, or <code>null</code> if none.
	 */
	private ILaunchListener fLaunchListener = null;
	
	/**
	 * An empty document.
	 */
	class EmptyConsoleDocument extends ConsoleDocument {
		
		public EmptyConsoleDocument() {
			super(new ConsoleColorProvider());
		}
		
		protected ITextStore newTextStore() {
			return new ConsoleOutputTextStore(0);
		}
	}
	
	/**
	 * If showing the output for a specific process, the console view reverts to
	 * showing the output of the current process if that launch is removed.
	 */
	class LaunchListener implements ILaunchListener {
		
		/**
		 * @see org.eclipse.debug.core.ILaunchListener#launchAdded(org.eclipse.debug.core.ILaunch)
		 */
		public void launchAdded(ILaunch launch) {
		}

		/**
		 * @see org.eclipse.debug.core.ILaunchListener#launchChanged(org.eclipse.debug.core.ILaunch)
		 */
		public void launchChanged(ILaunch launch) {
		}

		/**
		 * @see org.eclipse.debug.core.ILaunchListener#launchRemoved(org.eclipse.debug.core.ILaunch)
		 */
		public void launchRemoved(ILaunch launch) {
			if (getMode() == MODE_SPECIFIC_PROCESS) {
				IProcess process = getProcess();
				if (process != null && launch.equals(process.getLaunch())) {
					setMode(MODE_CURRENT_PROCESS);
					setViewerInput(DebugUITools.getCurrentProcess());
				}
			}
		}

	}

	/**
	 * @see AbstractDebugView#createViewer(Composite)
	 */
	protected Viewer createViewer(Composite parent) {
		ConsoleViewer cv = new ConsoleViewer(parent);		
		cv.getSelectionProvider().addSelectionChangedListener(getSelectionChangedListener());
		cv.addTextInputListener(getTextInputListener());
		getSite().setSelectionProvider(cv.getSelectionProvider());
		
		// listen to selection changes in the debug view
		getSite().getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		
		setEventHandler(new ConsoleViewEventHandler(this));
		return cv;
	}
	
	/**
	 * @see AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.CONSOLE_VIEW;
	}
	
	/**
	 * Sets the input to the current process if no debug view is present
	 * on the current active page or if the current process is <code>null</code>.
	 */
	public void setViewerInputFromConsoleDocumentManager(IProcess process) {
		if (getMode() == MODE_CURRENT_PROCESS) {
			IViewPart debugView= findView(IDebugUIConstants.ID_DEBUG_VIEW);
			if (debugView == null || process == null) {
				setViewerInput(process);
			}
		}
	}
	
	/** 
	 * Sets the console to view the document's streams
	 * associated with the given process.
	 */
	public void setViewerInput(IProcess process) {
		if (!isAvailable()) {
			return;
		}
		if (getProcess() == process) {
			// do nothing if the input is the same as what is
			// being viewed. If this is the first input, set
			// the console to an empty document
			if (getConsoleViewer().getDocument() == null) {
				getConsoleViewer().setDocument(new EmptyConsoleDocument());
				getConsoleViewer().setEditable(false);
				updateObjects();
			}			
			return;
		}
		
		setProcess(process)	;
		
		Runnable r = new Runnable() {
			public void run() {
				if (!isAvailable()) {
					return;
				}
				IDocument doc = null;
				if (getProcess() != null) {
					doc = DebugUIPlugin.getDefault().getConsoleDocumentManager().getConsoleDocument(getProcess());
				}
				if (doc == null) {
					doc = new EmptyConsoleDocument();
				}
				getConsoleViewer().setDocument(doc);
				getConsoleViewer().setEditable(getProcess() != null && !getProcess().isTerminated());
				updateTitle();
				updateObjects();
				updateSelectionDependentActions();
				fKeyBindingFollowLinkAction.clearStatusLine();
			}
		};
		asyncExec(r);
	}
	
	protected void updateTitle() {
		// update view title
		String title = null;
		if (getProcess() == null) { 
			title = DebugUIViewsMessages.getString("ConsoleView.Console_1"); //$NON-NLS-1$
		} else {
			// use debug target title if applicable
			Object obj = getProcess().getAdapter(IDebugTarget.class);
			if (obj == null) {
				obj = getProcess();
			}
			StringBuffer buff= new StringBuffer(DebugUIViewsMessages.getString("ConsoleView.Console_1")); //$NON-NLS-1$
			buff.append(" ["); //$NON-NLS-1$
			buff.append(DebugUIPlugin.getModelPresentation().getText(obj));
			buff.append(']');
			title= buff.toString();
		}
		setTitle(title);
	}
		
	/**
	 * @see AbstractDebugView#createActions()
	 */
	protected void createActions() {
		fClearOutputAction= new ClearOutputAction(getConsoleViewer());
		
		// In order for the clipboard actions to accessible via their shortcuts
		// (e.g., Ctrl-C, Ctrl-V), we *must* set a global action handler for
		// each action		
		IActionBars actionBars= getViewSite().getActionBars();
		TextViewerAction action= new TextViewerAction(getTextViewer(), ITextOperationTarget.CUT);
		action.configureAction(DebugUIViewsMessages.getString("ConsoleView.Cu&t@Ctrl+X_3"), DebugUIViewsMessages.getString("ConsoleView.Cut_4"), DebugUIViewsMessages.getString("ConsoleView.Cut_4")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		action.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
		action.setHoverImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_CUT_HOVER));
		setGlobalAction(actionBars, IWorkbenchActionConstants.CUT, action);
		action= new TextViewerAction(getTextViewer(), ITextOperationTarget.COPY);
		action.configureAction(DebugUIViewsMessages.getString("ConsoleView.&Copy@Ctrl+C_6"), DebugUIViewsMessages.getString("ConsoleView.Copy_7"), DebugUIViewsMessages.getString("ConsoleView.Copy_7")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		action.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		action.setHoverImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY_HOVER));		
		setGlobalAction(actionBars, IWorkbenchActionConstants.COPY, action);
		action= new TextViewerAction(getTextViewer(), ITextOperationTarget.PASTE);
		action.configureAction(DebugUIViewsMessages.getString("ConsoleView.&Paste@Ctrl+V_9"), DebugUIViewsMessages.getString("ConsoleView.Paste_10"), DebugUIViewsMessages.getString("ConsoleView.Paste_Clipboard_Text_11")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		action.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
		action.setHoverImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_HOVER));		
		setGlobalAction(actionBars, IWorkbenchActionConstants.PASTE, action);
		action= new TextViewerAction(getTextViewer(), ITextOperationTarget.SELECT_ALL);
		action.configureAction(DebugUIViewsMessages.getString("ConsoleView.Select_&All@Ctrl+A_12"), DebugUIViewsMessages.getString("ConsoleView.Select_All"), DebugUIViewsMessages.getString("ConsoleView.Select_All")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		setGlobalAction(actionBars, IWorkbenchActionConstants.SELECT_ALL, action);
		
		//XXX Still using "old" resource access
		ResourceBundle bundle= ResourceBundle.getBundle("org.eclipse.debug.internal.ui.views.DebugUIViewsMessages"); //$NON-NLS-1$
		setGlobalAction(actionBars, IWorkbenchActionConstants.FIND, new FindReplaceAction(bundle, "find_replace_action.", this)); //$NON-NLS-1$
	
		action= new TextViewerGotoLineAction(getConsoleViewer());
		setGlobalAction(actionBars, ITextEditorActionConstants.GOTO_LINE, action);
		
		fFollowLinkAction = new FollowHyperlinkAction(getConsoleViewer());
		
		fKeyBindingFollowLinkAction= new KeyBindingFollowHyperlinkAction(getConsoleViewer(), actionBars);
		fKeyBindingFollowLinkAction.setActionDefinitionId("org.eclipse.jdt.ui.edit.text.java.open.editor"); //$NON-NLS-1$
		getSite().getKeyBindingService().registerAction(fKeyBindingFollowLinkAction);
		
		fProcessDropDownAction = new ProcessDropDownAction(this);
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
		
		fSelectionActions.add(IWorkbenchActionConstants.CUT);
		fSelectionActions.add(IWorkbenchActionConstants.COPY);
		fSelectionActions.add(IWorkbenchActionConstants.PASTE);
				
		// initialize input, after viewer has been created
		setViewerInput(DebugUITools.getCurrentProcess());
	}

	protected void setGlobalAction(IActionBars actionBars, String actionID, IAction action) {
		fGlobalActions.put(actionID, action); 
		actionBars.setGlobalActionHandler(actionID, action);
	}
	
	/**
	 * @see AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager mgr) {
		mgr.add(new Separator(IDebugUIConstants.LAUNCH_GROUP));
		mgr.add(new Separator(IDebugUIConstants.OUTPUT_GROUP));
		mgr.add(fProcessDropDownAction);
		mgr.add(fScrollLockAction);
		mgr.add(fClearOutputAction);
	}

	/**
	 * Adds the text manipulation actions to the <code>ConsoleViewer</code>
	 */
	protected void fillContextMenu(IMenuManager menu) {
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
	}


	/**
	 * @see WorkbenchPart#getAdapter(Class)
	 */
	public Object getAdapter(Class required) {
		if (!isAvailable()) {
			return null;
		}
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
		return super.getAdapter(required);
	}


	protected final ISelectionChangedListener getSelectionChangedListener() {
		return new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					updateSelectionDependentActions();
				}
			};
	}
	
	protected final ITextInputListener getTextInputListener() {
		return new ITextInputListener() {
				public void inputDocumentAboutToBeChanged(IDocument old, IDocument nw) {
					if (old != null) {
						old.removeDocumentListener(ConsoleView.this);
					}
					fCurrentDocument = nw;
					if (nw != null) {
						nw.addDocumentListener(ConsoleView.this);
					}
				}
				public void inputDocumentChanged(IDocument doc, IDocument doc2) {
					updateAction(IWorkbenchActionConstants.FIND);
				}
			};
	}

	protected void updateSelectionDependentActions() {
		Iterator iterator= fSelectionActions.iterator();
		while (iterator.hasNext()) {
			updateAction((String)iterator.next());		
		}
	}

	protected void updateAction(String actionId) {
		if (!isAvailable()) {
			return;
		}
		IAction action= (IAction)fGlobalActions.get(actionId);
		if (action instanceof IUpdate) {
			((IUpdate) action).update();
		}
	}
	
	public void dispose() {
		getSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		if (getConsoleViewer() != null) {
			getConsoleViewer().dispose();
		}
		if (fCurrentDocument != null) {
			fCurrentDocument.removeDocumentListener(this);
		}
		if (fFollowLinkAction != null) {
			fFollowLinkAction.dispose();
		}
		if (fKeyBindingFollowLinkAction != null) {
			fKeyBindingFollowLinkAction.dispose();
			getSite().getKeyBindingService().unregisterAction(fKeyBindingFollowLinkAction);
		}
		if (fLaunchListener != null) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(fLaunchListener);
			fLaunchListener = null;
		}
		if (fProcessDropDownAction != null) {
			fProcessDropDownAction.dispose();
			fProcessDropDownAction = null;
		}
		super.dispose();
	}
	
	/**
	 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent e) {
	}

	/**
	 * @see IDocumentListener#documentChanged(DocumentEvent)
	 */
	public void documentChanged(DocumentEvent e) {
		updateAction(IWorkbenchActionConstants.FIND);
	}

	public ConsoleViewer getConsoleViewer() {
		return (ConsoleViewer)getViewer();
	}
	
	/**
	 * Sets the process being viewed
	 * 
	 * @param process process or <code>null</code>
	 */
	private void setProcess(IProcess process) {
		fProcess = process;
	}
	
	/**
	 * Returns the process being viewed, or <code>null</code>
	 * 
	 * @return process
	 */
	public IProcess getProcess() {
		return fProcess;
	}
	
	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (getMode() == MODE_CURRENT_PROCESS) {
			setViewerInput(DebugUITools.getCurrentProcess());
		}
	}

	/**
	 * Returns this view's mode.
	 */
	protected int getMode() {
		return fMode;
	}
	
	/**
	 * Sets this view's mode
	 */
	protected void setMode(int mode) {
		if (getMode() != mode) {
			fMode = mode;
			if (mode == MODE_SPECIFIC_PROCESS) {
				fLaunchListener = new LaunchListener();
				DebugPlugin.getDefault().getLaunchManager().addLaunchListener(fLaunchListener);
			} else {
				if (fLaunchListener != null) {
					DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(fLaunchListener);
					fLaunchListener = null;
				}
			}
		}
	}
	
	/**
	 * Save scroll lock.
	 * 
	 * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		super.saveState(memento);
		if (fScrollLockAction != null) {
			memento.putString(getViewSite().getId() + ".scrollLock", new Boolean(fScrollLockAction.isChecked()).toString()); //$NON-NLS-1$
		}
	}

	/**
	 * Restore scroll lock.
	 * 
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			String lock = memento.getString(site.getId() + ".scrollLock"); //$NON-NLS-1$
			if (lock != null) {
				fIsLocked = new Boolean(lock).booleanValue(); 
			}
		}
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
	
}
