/*
 * Created on Mar 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms.editor;

import org.eclipse.jface.viewers.*;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.MultiPageSelectionProvider;
;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FormEditorSelectionProvider extends MultiPageSelectionProvider {
	private ISelection globalSelection;
	/**
	 * @param multiPageEditor
	 */
	public FormEditorSelectionProvider(FormEditor formEditor) {
		super(formEditor);
	}
	
	public ISelection getSelection() {
		IEditorPart activeEditor = ((FormEditor)getMultiPageEditor()).getActiveEditor();
		if (activeEditor != null) {
			ISelectionProvider selectionProvider = activeEditor.getSite().getSelectionProvider();
			if (selectionProvider != null)
				return selectionProvider.getSelection();
		}
		return globalSelection;
	}
	/* (non-Javadoc)
	 * Method declared on <code>ISelectionProvider</code>.
	 */
	public void setSelection(ISelection selection) {
		IEditorPart activeEditor = ((FormEditor)getMultiPageEditor()).getActiveEditor();
		if (activeEditor != null) {
			ISelectionProvider selectionProvider = activeEditor.getSite().getSelectionProvider();
			if (selectionProvider != null)
				selectionProvider.setSelection(selection);
		}
		else
			this.globalSelection = selection;
	}
}
