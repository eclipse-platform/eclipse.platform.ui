package org.eclipse.ui.internal.provisional.ide;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * An entry in the Open With menu, responsible for providing a label and image
 * descriptor, and for opening the editor when the entry is chosen.
 * 
 * @see IEditorOpenStrategy
 * @see OpenWithInfo
 * @since 3.2
 */
public abstract class OpenWithEntry {

	private IEditorDescriptor editorDesc;

	private Object element;

	/**
	 * Creates a new <code>OpenWithEntry</code> for the given editor
	 * descriptor and element.
	 * 
	 * @param editorDesc
	 *            the editor descriptor or <code>null</code> if unspecified
	 * @param element
	 *            the model element
	 */
	public OpenWithEntry(IEditorDescriptor editorDesc, Object element) {
		Assert.isNotNull(element);
		this.editorDesc = editorDesc;
		this.element = element;
	}

	/**
	 * Returns the editor descriptor, or <code>null</code> if unspecified.
	 * 
	 * @return the editor descriptor, or <code>null</code>
	 */
	public IEditorDescriptor getEditorDescriptor() {
		return editorDesc;
	}

	/**
	 * Returns the model element.
	 * 
	 * @return the model element
	 */
	protected Object getElement() {
		return element;
	}

	/**
	 * Opens the editor(s) represented by this entry.
	 * 
	 * @param page
	 *            the workbench page
	 * @param activate
	 *            <code>true</code> to activate the editor, <code>false</code>
	 *            to open without activating
	 * @param matchFlags
	 *            the match flags (see
	 *            {@link IWorkbenchPage#openEditor(org.eclipse.ui.IEditorInput, String, boolean, int)}
	 *            for more details
	 * @param rememberEditor
	 *            <code>true</code> to remember the editor descriptor as the
	 *            last type of editor used on the model element,
	 *            <code>false</code> to not remember
	 * @throws PartInitException
	 *             if an error occurs while attempting to open
	 */
	public abstract void openEditor(IWorkbenchPage page, boolean activate,
			int matchFlags, boolean rememberEditor) throws PartInitException;

	/**
	 * Returns the label to show for this entry.
	 * 
	 * @return the label to show for this entry
	 */
	public String getLabel() {
		return getEditorDescriptor() == null ? "" : getEditorDescriptor().getLabel(); //$NON-NLS-1$
	}

	/**
	 * Returns an image descriptor to show for this entry, or <code>null</code>
	 * if no image should be shown.
	 * 
	 * @return the image descriptor or <code>null</code>
	 */
	public ImageDescriptor getImageDescriptor() {
		return editorDesc.getImageDescriptor();
	}

}
