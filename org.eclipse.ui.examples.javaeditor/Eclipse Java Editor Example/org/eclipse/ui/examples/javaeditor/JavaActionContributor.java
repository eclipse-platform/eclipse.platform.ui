package org.eclipse.ui.examples.javaeditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ResourceBundle;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * Contributes interesting Java actions to the desktop's Edit menu and
 * the toolbar.
 */
public class JavaActionContributor extends BasicTextEditorActionContributor {

	protected RetargetTextEditorAction fContentAssistProposal;
	protected RetargetTextEditorAction fContentAssistTip;
	protected TextEditorAction fTogglePresentation;

	/**
	 * Default constructor.
	 */
	public JavaActionContributor() {
		super();

		fContentAssistProposal= new RetargetTextEditorAction(JavaEditorMessages.getResourceBundle(), "ContentAssistProposal."); //$NON-NLS-1$
		fContentAssistTip= new RetargetTextEditorAction(JavaEditorMessages.getResourceBundle(), "ContentAssistTip."); //$NON-NLS-1$
		fTogglePresentation= new PresentationAction();
	}
	
	/* (non-Javadoc)
	 * Method declared on EditorActionBarContributor
	 */
	public void contributeToMenu(IMenuManager menuManager) {
		IMenuManager editMenu= menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			editMenu.add(new Separator());
			editMenu.add(fContentAssistProposal);
			editMenu.add(fContentAssistTip);
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on EditorActionBarContributor
	 */
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(new Separator());
		toolBarManager.add(fTogglePresentation);
	}
	
	/* (non-Javadoc)
	 * Method declared on EditorActionBarContributor
	 */
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);

		ITextEditor editor= null;
		if (part instanceof ITextEditor)
			editor= (ITextEditor) part;

		fContentAssistProposal.setAction(getAction(editor, "ContentAssistProposal")); //$NON-NLS-1$
		fContentAssistTip.setAction(getAction(editor, "ContentAssistTip")); //$NON-NLS-1$

		fTogglePresentation.setEditor(editor);
		fTogglePresentation.update();
	}
}
