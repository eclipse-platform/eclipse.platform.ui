package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.ResourceBundle;import org.eclipse.jface.action.IAction;import org.eclipse.jface.action.IMenuManager;import org.eclipse.ui.IActionBars;import org.eclipse.ui.IEditorPart;import org.eclipse.ui.IWorkbenchActionConstants;import org.eclipse.ui.part.EditorActionBarContributor;



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
		ITextEditorActionConstants.PRINT		
	};
	
	/** The active editor part */
	private IEditorPart fActiveEditorPart;
	/** The go to line action */
	private RetargetTextEditorAction fGotoLine;	
	
	/**
	 * Creates an empty editor action bar contributor. The action bars are
	 * furnished later via the <code>init</code> method.
	 *
	 * @see org.eclipse.ui.IEditorActionBarContributor#init
	 */
	public BasicTextEditorActionContributor() {
		fGotoLine= new RetargetTextEditorAction(EditorMessages.getResourceBundle(), "GotoLine."); //$NON-NLS-1$
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
	 * The <code>BasicTextEditorActionContributor</code> implementation of this 
	 * <code>IEditorActionBarContributor</code> method installs the global 
	 * action handler for the given text editor. Subclasses may extend.
	 */
	public void setActiveEditor(IEditorPart part) {
		
		if (fActiveEditorPart == part)
			return;
			
		fActiveEditorPart= part;
		ITextEditor editor= (part instanceof ITextEditor) ? (ITextEditor) part : null;
		
		IActionBars actionBars= getActionBars();
		if (actionBars != null) {
			for (int i= 0; i < ACTIONS.length; i++)
				actionBars.setGlobalActionHandler(ACTIONS[i], getAction(editor, ACTIONS[i]));
		}
		
		fGotoLine.setAction(getAction(editor, ITextEditorActionConstants.GOTO_LINE));
	}
	
	/**
	 * @see EditorActionBarContributor#contributeToMenu(IMenuManager)
	 */
	public void contributeToMenu(IMenuManager menu) {
		IMenuManager editMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			editMenu.add(fGotoLine);
		}
	}
}
