package org.eclipse.ui.editors.text;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

/**
 * 
 */
public class TextEditorActionContributor extends BasicTextEditorActionContributor {

	/** Convert to DOS action. */
	private RetargetTextEditorAction fConvertToDOS;
	/** Convert to UNIX action. */
	private RetargetTextEditorAction fConvertToUNIX;
	/** Convert to Mac action. */
	private RetargetTextEditorAction fConvertToMac;


	public TextEditorActionContributor() {
		super();
		fConvertToDOS= new RetargetTextEditorAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToDOS."); //$NON-NLS-1$ 
		fConvertToUNIX= new RetargetTextEditorAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToUNIX."); //$NON-NLS-1$ 
		fConvertToMac= new RetargetTextEditorAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToMac."); //$NON-NLS-1$ 
	}	

	/*
	 * @see IEditorActionBarContributor#setActiveEditor(IEditorPart)
	 */
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);

		if (!(part instanceof ITextEditor))
			return;

		ITextEditor textEditor= (ITextEditor) part;

		fConvertToDOS.setAction(getAction(textEditor, ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_DOS));
		fConvertToUNIX.setAction(getAction(textEditor, ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_UNIX));
		fConvertToMac.setAction(getAction(textEditor, ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_MAC));
	}

	/*
	 * @see EditorActionBarContributor#contributeToMenu(IMenuManager)
	 */
	public void contributeToMenu(IMenuManager menu) {
		IMenuManager editMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			MenuManager subMenu= new MenuManager(TextEditorMessages.getString("Editor.ConvertLineDelimiters.label")); //$NON-NLS-1$

			subMenu.add(fConvertToDOS);
			subMenu.add(fConvertToUNIX);
			subMenu.add(fConvertToMac);
	
			editMenu.add(subMenu);
		}
	}

}
