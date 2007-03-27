package org.eclipse.ui.tests.statushandlers.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * 
 */
public class PartInitExceptionEditor extends EditorPart {

	public void doSave(IProgressMonitor monitor) {

	}

	public void doSaveAs() {

	}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		throw new PartInitException(
				"A sample PartInitException thrown during viewpart initialization.");

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

}
