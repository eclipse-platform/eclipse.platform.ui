package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.jface.action.IMenuListener;

/**
 * This class implements the Readme editor.  Since the readme
 * editor is mostly just a text editor, there is very little
 * implemented in this actual class.  It can be regarded as
 * simply decorating the text editor with a content outline.
 */
public class ReadmeEditor extends TextEditor {
	protected ReadmeContentOutlinePage page;
/**
 * Creates a new ReadmeEditor.
 */
public ReadmeEditor() {
	super();
}
/** (non-Javadoc)
 * Method declared on IEditorPart
 */
public void doSave(IProgressMonitor monitor) {
	super.doSave(monitor);
	if (page != null)
		page.update();
}
/** (non-Javadoc)
 * Method declared on IAdaptable
 */
public Object getAdapter(Class key) {
	if (key.equals(IContentOutlinePage.class)) {
		IEditorInput input = getEditorInput();
		if (input instanceof IFileEditorInput) {
			page = new ReadmeContentOutlinePage(((IFileEditorInput)input).getFile());
			return page;
		}
	}
	return super.getAdapter(key);
}
}
