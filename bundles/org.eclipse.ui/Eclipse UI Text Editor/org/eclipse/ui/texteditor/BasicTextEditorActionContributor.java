package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;import org.eclipse.jface.action.IAction;import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.IStatusLineManager;

import org.eclipse.ui.IActionBars;import org.eclipse.ui.IEditorPart;import org.eclipse.ui.IWorkbenchActionConstants;import org.eclipse.ui.part.EditorActionBarContributor;



/**
 * Manages the installation and deinstallation of global actions for 
 * the same type of editors.
 * <p>
 * If instantiated and used as-is, the contributor connects actions
 * of the current editor only to predefined global actions. No additions
 * are made.
 * <p>
 * Subclasses may override the following methods:
 * <ul>
 *   <li><code>contributeToMenu</code> - extend to contribute to menu</li>
 *   <li><code>contributeToToolBar</code> - reimplement to contribute to toolbar</li>
 *   <li><code>contributeToStatusLine</code> - reimplement to contribute to status line</li>
 *   <li><code>setActiveEditor</code> - extend to react to editor changes</li>
 * </ul>
 * </p>
 */
public class BasicTextEditorActionContributor extends EditorActionBarContributor {
	
	/** The global actions to be connected with editor actions */
	private final static String[] ACTIONS= {
		ITextEditorActionConstants.UNDO, 
		ITextEditorActionConstants.REDO,
		ITextEditorActionConstants.CUT,
		ITextEditorActionConstants.COPY,
		ITextEditorActionConstants.PASTE,
		ITextEditorActionConstants.DELETE,
		ITextEditorActionConstants.SELECT_ALL,
		ITextEditorActionConstants.FIND,
		ITextEditorActionConstants.BOOKMARK,
		ITextEditorActionConstants.ADD_TASK,
		ITextEditorActionConstants.PRINT,
		ITextEditorActionConstants.REVERT
	};
	
	/** The status fields to be set to the editor */
	private final static String[] STATUSFIELDS= {
		ITextEditorActionConstants.STATUS_CATEGORY_ELEMENT_STATE,
		ITextEditorActionConstants.STATUS_CATEGORY_INPUT_MODE,
		ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION
	};
	
	/** The active editor part */
	private IEditorPart fActiveEditorPart;

	/** The undo action */
	private RetargetTextEditorAction fUndo;
	/** The redo action */
	private RetargetTextEditorAction fRedo;
	/** The delete line action */
	private RetargetTextEditorAction fDeleteLine;
	/** The delete line to beginning action */
	private RetargetTextEditorAction fDeleteLineToBeginning;
	/** The delete line to end action */
	private RetargetTextEditorAction fDeleteLineToEnd;
	/** The set mark action */
	private RetargetTextEditorAction fSetMark;
	/** The clear mark action */
	private RetargetTextEditorAction fClearMark;
	/** The swap mark action */
	private RetargetTextEditorAction fSwapMark;
	/** The find next action */
	private RetargetTextEditorAction fFindNext;
	/** The find previous action */
	private RetargetTextEditorAction fFindPrevious;	
	/** The incremental find action */
	private RetargetTextEditorAction fIncrementalFind;	
	/** The go to line action */
	private RetargetTextEditorAction fGotoLine;
	/** The map of status fields */
	private Map fStatusFields;
	
	
	/**
	 * Creates an empty editor action bar contributor. The action bars are
	 * furnished later via the <code>init</code> method.
	 *
	 * @see org.eclipse.ui.IEditorActionBarContributor#init
	 */
	public BasicTextEditorActionContributor() {

		fUndo= new RetargetTextEditorAction(EditorMessages.getResourceBundle(), "Undo."); //$NON-NLS-1$
		fRedo= new RetargetTextEditorAction(EditorMessages.getResourceBundle(), "Redo."); //$NON-NLS-1$		
		fDeleteLine= new RetargetTextEditorAction(EditorMessages.getResourceBundle(), "DeleteLine."); //$NON-NLS-1$
		fDeleteLineToBeginning= new RetargetTextEditorAction(EditorMessages.getResourceBundle(), "DeleteLineToBeginning."); //$NON-NLS-1$
		fDeleteLineToEnd= new RetargetTextEditorAction(EditorMessages.getResourceBundle(), "DeleteLineToEnd."); //$NON-NLS-1$
		fSetMark= new RetargetTextEditorAction(EditorMessages.getResourceBundle(), "SetMark."); //$NON-NLS-1$
		fClearMark= new RetargetTextEditorAction(EditorMessages.getResourceBundle(), "ClearMark."); //$NON-NLS-1$
		fSwapMark= new RetargetTextEditorAction(EditorMessages.getResourceBundle(), "SwapMark."); //$NON-NLS-1$
		
		fFindNext= new RetargetTextEditorAction(EditorMessages.getResourceBundle(), "FindNext."); //$NON-NLS-1$
		fFindPrevious= new RetargetTextEditorAction(EditorMessages.getResourceBundle(), "FindPrevious."); //$NON-NLS-1$
		fIncrementalFind= new RetargetTextEditorAction(EditorMessages.getResourceBundle(), "IncrementalFind."); //$NON-NLS-1$
		fGotoLine= new RetargetTextEditorAction(EditorMessages.getResourceBundle(), "GotoLine."); //$NON-NLS-1$
		
		fStatusFields= new HashMap(3);
		for (int i= 0; i < STATUSFIELDS.length; i++)
			fStatusFields.put(STATUSFIELDS[i], new StatusLineContributionItem(STATUSFIELDS[i]));
	}
	
	/**
	 * Returns the active editor part.
	 *
	 * @return the active editor part
	 */
	protected final IEditorPart getActiveEditorPart() {
		return fActiveEditorPart;
	}
	
	/**
	 * Returns the action registered with the given text editor.
	 *
	 * @param editor the editor, or <code>null</code>
	 * @param actionId the action id
	 * @return the action, or <code>null</code> if none
	 */
	protected final IAction getAction(ITextEditor editor, String actionId) {
		return (editor == null ? null : editor.getAction(actionId));
	}
	
	/**
	 * The method installs the global action handlers for the given text editor.
	 */
	private void doSetActiveEditor(IEditorPart part) {
		
		if (fActiveEditorPart == part)
			return;
			
		if (fActiveEditorPart instanceof ITextEditorExtension) {
			ITextEditorExtension extension= (ITextEditorExtension) fActiveEditorPart;
			for (int i= 0; i < STATUSFIELDS.length; i++)
				extension.setStatusField(null, STATUSFIELDS[i]);
		}

		fActiveEditorPart= part;
		ITextEditor editor= (part instanceof ITextEditor) ? (ITextEditor) part : null;
		
		IActionBars actionBars= getActionBars();
		if (actionBars != null) {
			for (int i= 0; i < ACTIONS.length; i++)
				actionBars.setGlobalActionHandler(ACTIONS[i], getAction(editor, ACTIONS[i]));
		}

		fUndo.setAction(getAction(editor, ITextEditorActionConstants.UNDO));
		fRedo.setAction(getAction(editor, ITextEditorActionConstants.REDO));
		fDeleteLine.setAction(getAction(editor, ITextEditorActionConstants.DELETE_LINE));
		fDeleteLineToBeginning.setAction(getAction(editor, ITextEditorActionConstants.DELETE_LINE_TO_BEGINNING));
		fDeleteLineToEnd.setAction(getAction(editor, ITextEditorActionConstants.DELETE_LINE_TO_END));
		fSetMark.setAction(getAction(editor, ITextEditorActionConstants.SET_MARK));
		fClearMark.setAction(getAction(editor, ITextEditorActionConstants.CLEAR_MARK));
		fSwapMark.setAction(getAction(editor, ITextEditorActionConstants.SWAP_MARK));
		fFindNext.setAction(getAction(editor, ITextEditorActionConstants.FIND_NEXT));
		fFindPrevious.setAction(getAction(editor, ITextEditorActionConstants.FIND_PREVIOUS));
		fIncrementalFind.setAction(getAction(editor, ITextEditorActionConstants.FIND_INCREMENTAL));
		fGotoLine.setAction(getAction(editor, ITextEditorActionConstants.GOTO_LINE));
		
		if (fActiveEditorPart instanceof ITextEditorExtension) {
			ITextEditorExtension extension= (ITextEditorExtension) fActiveEditorPart;
			for (int i= 0; i < STATUSFIELDS.length; i++)
				extension.setStatusField((IStatusField) fStatusFields.get(STATUSFIELDS[i]), STATUSFIELDS[i]);
		}
	}
	
	/**
	 * The <code>BasicTextEditorActionContributor</code> implementation of this 
	 * <code>IEditorActionBarContributor</code> method installs the global 
	 * action handler for the given text editor. Subclasses may extend.
	 */
	public void setActiveEditor(IEditorPart part) {
		doSetActiveEditor(part);
	}
	
	/*
	 * @see EditorActionBarContributor#contributeToMenu(IMenuManager)
	 */
	public void contributeToMenu(IMenuManager menu) {

		IMenuManager editMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {

			IMenuManager markMenu= new MenuManager(EditorMessages.getString("Editor.mark.submenu.label")); //$NON-NLS-1$
			markMenu.add(fSetMark);
			markMenu.add(fClearMark);
			markMenu.add(fSwapMark);
			editMenu.add(markMenu);

			IMenuManager deleteLineMenu= new MenuManager(EditorMessages.getString("Editor.delete.line.submenu.label")); //$NON-NLS-1$
			deleteLineMenu.add(fDeleteLine);
			deleteLineMenu.add(fDeleteLineToBeginning);
			deleteLineMenu.add(fDeleteLineToEnd);
			editMenu.add(deleteLineMenu);
			
			editMenu.appendToGroup(IWorkbenchActionConstants.FIND_EXT, fFindNext);
			editMenu.appendToGroup(IWorkbenchActionConstants.FIND_EXT,fFindPrevious);
			editMenu.appendToGroup(IWorkbenchActionConstants.FIND_EXT,fIncrementalFind);
			editMenu.appendToGroup(IWorkbenchActionConstants.FIND_EXT,fGotoLine);
		}
	}
	
	/*
	 * @see EditorActionBarContributor#contributeToStatusLine(IStatusLineManager)
	 */
	public void contributeToStatusLine(IStatusLineManager statusLineManager) {
		super.contributeToStatusLine(statusLineManager);
		for (int i= 0; i < STATUSFIELDS.length; i++)
			statusLineManager.add((IContributionItem) fStatusFields.get(STATUSFIELDS[i]));
	}
	
	/*
	 * @see IEditorActionBarContributor#dispose()
	 */
	public void dispose() {
		doSetActiveEditor(null);
		super.dispose();
	}
}
