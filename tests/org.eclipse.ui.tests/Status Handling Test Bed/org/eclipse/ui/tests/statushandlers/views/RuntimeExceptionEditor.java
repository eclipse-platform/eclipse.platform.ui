package org.eclipse.ui.tests.statushandlers.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.part.EditorPart;

/**
 * 
 */
public class RuntimeExceptionEditor extends EditorPart {

	public void doSave(IProgressMonitor monitor) {

	}

	public void doSaveAs() {

	}

	public void init(IEditorSite site, IEditorInput input) {

	}

	public boolean isDirty() {

		return false;
	}

	public boolean isSaveAsAllowed() {

		return false;
	}

	public void createPartControl(Composite parent) {

	}

	public void setFocus() {

	}

	public IEditorSite getEditorSite() {
		throw new RuntimeException(
				"A sample RuntimeException thrown during editor site retrieval.");
	}

}
