/*******************************************************************************
 * Copyright (c) 2002, 2004 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug fixes
 *     John-Mason P. Shackelford - bug 40255
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor;

import java.util.ResourceBundle;

import org.eclipse.ant.internal.ui.editor.actions.OpenDeclarationAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

/**
 * Contributes interesting Ant Editor actions to the desktop's Edit menu and the toolbar.
 * 
 */
public class AntEditorActionContributor extends TextEditorActionContributor {

	protected RetargetTextEditorAction fContentAssistProposal;
	protected RetargetTextEditorAction fContentFormat;
	private OpenDeclarationAction fOpenDeclarationAction;

	public AntEditorActionContributor() {
		super();
		ResourceBundle bundle = ResourceBundle.getBundle("org.eclipse.ant.internal.ui.editor.AntEditorMessages"); //$NON-NLS-1$
		fContentAssistProposal = new RetargetTextEditorAction(bundle, "ContentAssistProposal."); //$NON-NLS-1$
		fContentFormat = new RetargetTextEditorAction(bundle, "ContentFormat."); //$NON-NLS-1$
	}
	
	protected void initializeActions(AntEditor editor) {	 
		fOpenDeclarationAction= new OpenDeclarationAction(editor);
	}
	
	private void doSetActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);

		ITextEditor editor= null;
		if (part instanceof ITextEditor) {
			editor= (ITextEditor) part;
		}

		fContentAssistProposal.setAction(getAction(editor, "ContentAssistProposal")); //$NON-NLS-1$
		fContentFormat.setAction(getAction(editor, "ContentFormat")); //$NON-NLS-1$
		
		if (part instanceof AntEditor) {
			if (fOpenDeclarationAction == null) {
				initializeActions((AntEditor) part);
				contributeToMenu(getActionBars().getMenuManager());
			}
		}

		if (fOpenDeclarationAction != null) {
			fOpenDeclarationAction.setEditor((AntEditor) part);		
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void contributeToMenu(IMenuManager menu) {
		if (fOpenDeclarationAction == null) {
			return;
		}
		super.contributeToMenu(menu);
		
		IMenuManager navigateMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
		if (navigateMenu != null) {
			navigateMenu.appendToGroup(IWorkbenchActionConstants.OPEN_EXT, fOpenDeclarationAction);
			navigateMenu.setVisible(true);
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorActionBarContributor#init(org.eclipse.ui.IActionBars)
     */
    public void init(IActionBars bars) {
        super.init(bars);
        
        IMenuManager menuManager= bars.getMenuManager();
        IMenuManager editMenu= menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
        if (editMenu != null) {
            editMenu.add(new Separator());
            editMenu.add(fContentAssistProposal);
            editMenu.add(fContentFormat);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToToolBar(org.eclipse.jface.action.IToolBarManager)
     */
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
		//TODO the validate action is still in development
		//toolBarManager.add(fValidateAction);
		//toolBarManager.update(false);
	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionBarContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IEditorPart part) {
		doSetActiveEditor(part);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionBarContributor#dispose()
	 */
	public void dispose() {
		doSetActiveEditor(null);
		super.dispose();
	}
}
