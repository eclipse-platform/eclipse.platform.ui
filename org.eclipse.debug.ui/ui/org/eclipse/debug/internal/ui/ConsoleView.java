package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.help.ViewContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;

public class ConsoleView extends ViewPart implements IDocumentListener {

	protected ConsoleViewer fConsoleViewer= null;
	protected ClearOutputAction fClearOutputAction= null;

	protected Map fGlobalActions= new HashMap(10);
	protected List fSelectionActions = new ArrayList(3);
	
	protected IDocument fCurrentDocument= null;
	/**
	 * @see ViewPart#createChild(IWorkbenchPartContainer)
	 */
	public void createPartControl(Composite parent) {
		fConsoleViewer= new ConsoleViewer(parent);
		initializeActions();
		initializeToolBar();

		// create context menu
		MenuManager menuMgr= new MenuManager("#PopUp", IDebugUIConstants.ID_CONSOLE_VIEW); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});
		Menu menu= menuMgr.createContextMenu(fConsoleViewer.getTextWidget());
		fConsoleViewer.getTextWidget().setMenu(menu);
		// register the context menu such that other plugins may contribute to it
		getSite().registerContextMenu(menuMgr, fConsoleViewer);
		
		fConsoleViewer.getSelectionProvider().addSelectionChangedListener(getSelectionChangedListener());
		fConsoleViewer.addTextInputListener(getTextInputListener());
		getSite().setSelectionProvider(fConsoleViewer.getSelectionProvider());
		setViewerInput(DebugUIPlugin.getDefault().getCurrentProcess());
		WorkbenchHelp.setHelp(
			parent,
			new ViewContextComputer(this, IDebugHelpContextIds.CONSOLE_VIEW));
	}

	/**
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (fConsoleViewer != null) {
			fConsoleViewer.getControl().setFocus();
		}
	}
	
	/** 
	 * Sets the input of the viewer of this view in the
	 * UI thread.
	 */
	protected void setViewerInput(IAdaptable element) {
		setViewerInput(element, true);
	}
	
	/** 
	 * Sets the input of the viewer of this view in the
	 * UI thread.  The current input process is determined
	 * if so specified.
	 */
	protected void setViewerInput(final IAdaptable element, final boolean determineCurrentProcess) {
		if (fConsoleViewer == null || fConsoleViewer.getControl() == null || fConsoleViewer.getControl().isDisposed()) {
			return;
		}
		Display display= fConsoleViewer.getControl().getDisplay();
		if (display != null) {
			display.asyncExec(new Runnable() {
				public void run() {
					if (fConsoleViewer == null) {
						return;
					}
					Control viewerControl= fConsoleViewer.getControl();
					if (viewerControl == null || viewerControl.isDisposed()) {
						return;
					}
					IDocument doc= DebugUIPlugin.getDefault().getConsoleDocument((IProcess) element, determineCurrentProcess);
					fConsoleViewer.setDocument(doc);
				}
			});
		}
	}
	
	/**
	 * Initialize the actions of this view
	 */
	private void initializeActions() {
		fClearOutputAction= new ClearOutputAction(fConsoleViewer);
		
		// In order for the clipboard actions to accessible via their shortcuts
		// (e.g., Ctrl-C, Ctrl-V), we *must* set a global action handler for
		// each action		
		IActionBars actionBars= getViewSite().getActionBars();
		TextViewerAction action= new TextViewerAction(fConsoleViewer, fConsoleViewer.CUT);
		action.configureAction(DebugUIMessages.getString("ConsoleView.Cu&t@Ctrl+X_3"), DebugUIMessages.getString("ConsoleView.Cut_4"), DebugUIMessages.getString("ConsoleView.Cut_5")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		setGlobalAction(actionBars, ITextEditorActionConstants.CUT, action);
		action= new TextViewerAction(fConsoleViewer, fConsoleViewer.COPY);
		action.configureAction(DebugUIMessages.getString("ConsoleView.&Copy@Ctrl+C_6"), DebugUIMessages.getString("ConsoleView.Copy_7"), DebugUIMessages.getString("ConsoleView.Copy_8")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		setGlobalAction(actionBars, ITextEditorActionConstants.COPY, action);
		action= new TextViewerAction(fConsoleViewer, fConsoleViewer.PASTE);
		action.configureAction(DebugUIMessages.getString("ConsoleView.&Paste@Ctrl+V_9"), DebugUIMessages.getString("ConsoleView.Paste_10"), DebugUIMessages.getString("ConsoleView.Paste_Clipboard_Text_11")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		setGlobalAction(actionBars, ITextEditorActionConstants.PASTE, action);
		action= new TextViewerAction(fConsoleViewer, fConsoleViewer.SELECT_ALL);
		action.configureAction(DebugUIMessages.getString("ConsoleView.Select_&All@Ctrl+A_12"), DebugUIMessages.getString("ConsoleView.Select_All_13"), DebugUIMessages.getString("ConsoleView.Select_All_14")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		setGlobalAction(actionBars, ITextEditorActionConstants.SELECT_ALL, action);
		
		//XXX Still using "old" resource access
		ResourceBundle bundle= ResourceBundle.getBundle("org.eclipse.debug.internal.ui.DebugUIMessages"); //$NON-NLS-1$
		setGlobalAction(actionBars, ITextEditorActionConstants.FIND, new FindReplaceAction(bundle, "find_replace_action.", this));				 //$NON-NLS-1$
	
		action= new TextViewerGotoLineAction(fConsoleViewer);
		setGlobalAction(actionBars, ITextEditorActionConstants.GOTO_LINE, action);				
		actionBars.updateActionBars();
		
		fConsoleViewer.getTextWidget().addVerifyKeyListener(new VerifyKeyListener() {
			public void verifyKey(VerifyEvent event) {
				IAction gotoLine= (IAction)fGlobalActions.get(ITextEditorActionConstants.GOTO_LINE);
				if (event.stateMask == SWT.CTRL && event.keyCode == 0 && event.character == 0x0C && event.keyCode == 0 && gotoLine.isEnabled()) {
					gotoLine.run();
					event.doit= false;
				}
			}
		});
		
		fSelectionActions.add(ITextEditorActionConstants.CUT);
		fSelectionActions.add(ITextEditorActionConstants.COPY);
		fSelectionActions.add(ITextEditorActionConstants.PASTE);
		updateAction(ITextEditorActionConstants.FIND);
	}

	protected void setGlobalAction(IActionBars actionBars, String actionID, IAction action) {
		fGlobalActions.put(actionID, action); 
		actionBars.setGlobalActionHandler(actionID, action);
	}
	
	/**
	 * Configures the toolBar.
	 */
	private void initializeToolBar() {
		IToolBarManager tbm= getViewSite().getActionBars().getToolBarManager();
		tbm.add(fClearOutputAction);
		getViewSite().getActionBars().updateActionBars();
	}
	/**
	 * Adds the text manipulation actions to the <code>ConsoleViewer</code>
	 */
	protected void fillContextMenu(IMenuManager menu) {
		Point selectionRange= fConsoleViewer.getTextWidget().getSelection();
		ConsoleDocument doc= (ConsoleDocument) fConsoleViewer.getDocument();
		if (doc == null) {
			return;
		}
		if (doc.isReadOnly() || selectionRange.x < doc.getStartOfEditableContent()) {
			menu.add((IAction)fGlobalActions.get(ITextEditorActionConstants.COPY));
			menu.add((IAction)fGlobalActions.get(ITextEditorActionConstants.SELECT_ALL));
		} else {
			menu.add((IAction)fGlobalActions.get(ITextEditorActionConstants.CUT));
			menu.add((IAction)fGlobalActions.get(ITextEditorActionConstants.COPY));
			menu.add((IAction)fGlobalActions.get(ITextEditorActionConstants.PASTE));
			menu.add((IAction)fGlobalActions.get(ITextEditorActionConstants.SELECT_ALL));
		}

		menu.add(new Separator("FIND")); //$NON-NLS-1$
		menu.add((IAction)fGlobalActions.get(ITextEditorActionConstants.FIND));
		menu.add((IAction)fGlobalActions.get(ITextEditorActionConstants.GOTO_LINE));

		menu.add(fClearOutputAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * @see WorkbenchPart#getAdapter(Class)
	 */
	public Object getAdapter(Class required) {
		if (IFindReplaceTarget.class.equals(required)) {
			return fConsoleViewer.getFindReplaceTarget();
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
					updateAction(ITextEditorActionConstants.FIND);
				}
			};
	}

	protected void updateSelectionDependentActions() {
		Iterator iterator= fSelectionActions.iterator();
		while (iterator.hasNext())
			updateAction((String)iterator.next());		
	}

	protected void updateAction(String actionId) {
		IAction action= (IAction)fGlobalActions.get(actionId);
		if (action instanceof IUpdate)
			((IUpdate) action).update();
	}
	
	public void dispose() {
		if (fConsoleViewer != null) {
			fConsoleViewer.dispose();
			fConsoleViewer= null;
		}
		if (fCurrentDocument != null) {
			fCurrentDocument.removeDocumentListener(this);
		}
		super.dispose();
	}
	/**
	 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent arg0) {
	}

	/**
	 * @see IDocumentListener#documentChanged(DocumentEvent)
	 */
	public void documentChanged(DocumentEvent arg0) {
		updateAction(ITextEditorActionConstants.FIND);
	}
}

