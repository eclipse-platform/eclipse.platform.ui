package org.eclipse.ui.internal.provisional.ide;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

public abstract class OpenWithEntry {
	
	private IEditorDescriptor editorDesc;

	private Object element;

	/**
	 * @param editorDesc
	 * @param element
	 */
	public OpenWithEntry(IEditorDescriptor editorDesc, Object element) {
		Assert.isNotNull(element);
		this.editorDesc = editorDesc;
		this.element = element;
	}

	public IEditorDescriptor getEditorDescriptor() {
		return editorDesc;
	}

	protected Object getElement() {
		return element;
	}

	public abstract void openEditor(IWorkbenchPage page,
			boolean activate, int matchFlags, boolean rememberEditor) throws PartInitException;

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
