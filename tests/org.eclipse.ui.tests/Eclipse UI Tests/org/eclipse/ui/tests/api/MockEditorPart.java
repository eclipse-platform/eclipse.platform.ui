package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.*;

public class MockEditorPart extends MockWorkbenchPart implements IEditorPart {

	public static final String ID_MOCK_EDITOR_1 = "org.eclipse.ui.tests.api.mockEditor1";
	public static final String ID_MOCK_EDITOR_2 = "org.eclipse.ui.tests.api.mockEditor2";

	private IEditorInput input;
	
	public MockEditorPart() {
		super();
	}
	
	/**
	 * @see IEditorPart#doSave(IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
	}

	/**
	 * @see IEditorPart#doSaveAs()
	 */
	public void doSaveAs() {
	}

	/**
	 * @see IEditorPart#getEditorInput()
	 */
	public IEditorInput getEditorInput() {
		return input;
	}

	/**
	 * @see IEditorPart#getEditorSite()
	 */
	public IEditorSite getEditorSite() {
		return (IEditorSite)getSite();
	}

	/**
	 * @see IEditorPart#gotoMarker(IMarker)
	 */
	public void gotoMarker(IMarker marker) {
	}

	/**
	 * @see IEditorPart#init(IEditorSite, IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input)
		throws PartInitException {
		this.input = input;
		setSite(site);
	}

	/**
	 * @see IEditorPart#isDirty()
	 */
	public boolean isDirty() {
		return false;
	}

	/**
	 * @see IEditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * @see IEditorPart#isSaveOnCloseNeeded()
	 */
	public boolean isSaveOnCloseNeeded() {
		return false;
	}

}

