package org.eclipse.ui.editors.text;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import org.eclipse.ui.IActionBars;
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

	/** Convert to Windows action. */
	private RetargetTextEditorAction fConvertToWindows;
	/** Convert to UNIX action. */
	private RetargetTextEditorAction fConvertToUNIX;
	/** Convert to Mac action. */
	private RetargetTextEditorAction fConvertToMac;
	/** Encoding action group */
	private EncodingActionGroup fEncodingActionGroup;


	public TextEditorActionContributor() {
		super();
		
		// line delimiter conversion
		fConvertToWindows= new RetargetTextEditorAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToWindows."); //$NON-NLS-1$ 
		fConvertToUNIX= new RetargetTextEditorAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToUNIX."); //$NON-NLS-1$ 
		fConvertToMac= new RetargetTextEditorAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToMac."); //$NON-NLS-1$
		
		// character encoding
		fEncodingActionGroup= new EncodingActionGroup();
	}	
	
	private void doSetActiveEditor(IEditorPart part) {
				
		ITextEditor textEditor= null;
		if (part instanceof ITextEditor)
			textEditor= (ITextEditor) part;
			
		// line delimiter conversion
		fConvertToWindows.setAction(getAction(textEditor, ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_WINDOWS));
		fConvertToUNIX.setAction(getAction(textEditor, ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_UNIX));
		fConvertToMac.setAction(getAction(textEditor, ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_MAC));
		
		// character encoding
		fEncodingActionGroup.retarget(textEditor);
	}
	
	/*
	 * @see IEditorActionBarContributor#setActiveEditor(IEditorPart)
	 */
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
		doSetActiveEditor(part);
	}
	
	/*
	 * @see IEditorActionBarContributor#init(IActionBars)
	 */
	public void init(IActionBars bars) {
		super.init(bars);
		
		// line delimiter conversion
		IMenuManager menuManager= bars.getMenuManager();
		IMenuManager editMenu= menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			MenuManager subMenu= new MenuManager(TextEditorMessages.getString("Editor.ConvertLineDelimiters.label")); //$NON-NLS-1$

			subMenu.add(fConvertToWindows);
			subMenu.add(fConvertToUNIX);
			subMenu.add(fConvertToMac);
	
			editMenu.add(subMenu);
		}		
		
		// character encoding
		fEncodingActionGroup.fillActionBars(bars);
	}
	
	/*
	 * @see IEditorActionBarContributor#dispose()
	 */
	public void dispose() {
		doSetActiveEditor(null);
		super.dispose();
	}
}
