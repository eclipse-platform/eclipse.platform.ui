package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;import org.eclipse.core.runtime.IAdaptable;import org.eclipse.debug.core.model.IProcess;import org.eclipse.debug.ui.IDebugUIConstants;import org.eclipse.jface.action.*;import org.eclipse.jface.text.*;import org.eclipse.jface.viewers.ISelectionChangedListener;import org.eclipse.jface.viewers.SelectionChangedEvent;import org.eclipse.swt.graphics.Point;import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;import org.eclipse.ui.IActionBars;import org.eclipse.ui.IWorkbenchActionConstants;import org.eclipse.ui.help.ViewContextComputer;import org.eclipse.ui.help.WorkbenchHelp;import org.eclipse.ui.part.ViewPart;import org.eclipse.ui.texteditor.*;

public class ConsoleView extends ViewPart implements IDocumentListener {
	
	protected final static String PREFIX= "console_view.";

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
		MenuManager menuMgr= new MenuManager("#PopUp", IDebugUIConstants.ID_CONSOLE_VIEW);
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});
		Menu menu= menuMgr.createContextMenu(fConsoleViewer.getTextWidget());
		fConsoleViewer.getTextWidget().setMenu(menu);
		// register the context menu such that other plugins may contribute to it
		getSite().registerContextMenu(menuMgr.getId(), menuMgr, fConsoleViewer);
		
		fConsoleViewer.getSelectionProvider().addSelectionChangedListener(getSelectionChangedListener());
		fConsoleViewer.addTextInputListener(getTextInputListener());
		setViewerInput(DebugUIPlugin.getDefault().getCurrentProcess());
		setTitleToolTip(DebugUIUtils.getResourceString(PREFIX + AbstractDebugView.TITLE_TOOLTIPTEXT));
		WorkbenchHelp.setHelp(
			parent,
			new ViewContextComputer(this, IDebugHelpContextIds.CONSOLE_VIEW));
	}

	/**
	 * @see IWorkbenchPart
	 */
	public void setFocus() {
		fConsoleViewer.getControl().setFocus();
	}
	
	/** 
	 * Sets the input of the viewer of this view in the
	 * UI thread.
	 */
	protected void setViewerInput(final IAdaptable element) {
		setViewerInput(element, true);
	}
	
	/** 
	 * Sets the input of the viewer of this view in the
	 * UI thread.  The current input process is determined
	 * if so specified.
	 */
	protected void setViewerInput(final IAdaptable element, final boolean determineCurrentProcess) {
		if (fConsoleViewer == null || fConsoleViewer.getControl().isDisposed()) {
			return;
		}
		Display display= fConsoleViewer.getControl().getDisplay();
		if (display != null) {
			display.asyncExec(new Runnable() {
				public void run() {
					if (fConsoleViewer == null || fConsoleViewer.getControl().isDisposed()) {
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
		fClearOutputAction.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_CLEAR));		
		fClearOutputAction.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_CLEAR));
		fClearOutputAction.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_CLEAR));
		
		ResourceBundle bundle= DebugUIUtils.getResourceBundle();
		// In order for the clipboard actions to accessible via their shortcuts
		// (e.g., Ctrl-C, Ctrl-V), we *must* set a global action handler for
		// each action		
		IActionBars actionBars= getViewSite().getActionBars();
		setGlobalAction(actionBars, ITextEditorActionConstants.CUT, new ConsoleViewerAction(bundle, "cut_action.", fConsoleViewer, fConsoleViewer.CUT));
		setGlobalAction(actionBars, ITextEditorActionConstants.COPY, new ConsoleViewerAction(bundle, "copy_action.", fConsoleViewer, fConsoleViewer.COPY));
		setGlobalAction(actionBars, ITextEditorActionConstants.PASTE, new ConsoleViewerAction(bundle, "paste_action.", fConsoleViewer, fConsoleViewer.PASTE));
		setGlobalAction(actionBars, ITextEditorActionConstants.SELECT_ALL, new ConsoleViewerAction(bundle, "select_all_action.", fConsoleViewer, fConsoleViewer.SELECT_ALL));
		setGlobalAction(actionBars, ITextEditorActionConstants.FIND, new FindReplaceAction(bundle, "find_replace_action.", this));				
		setGlobalAction(actionBars, ITextEditorActionConstants.GOTO_LINE, new ConsoleGotoLineAction(bundle, "goto_line_action.", fConsoleViewer));				
	
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

		menu.add(new Separator("FIND"));
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

