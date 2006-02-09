package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.provisional.ide.OpenWithEntry;
import org.eclipse.ui.part.FileEditorInput;

public class FileOpenWithEntry extends OpenWithEntry {

	public FileOpenWithEntry(IEditorDescriptor editorDesc, IFile file) {
		super(editorDesc, file);
		Assert.isNotNull(editorDesc);
	}

	public void openEditor(IWorkbenchPage page, boolean activate,
			int matchFlags, boolean rememberEditor) throws PartInitException {
		Assert.isNotNull(getEditorDescriptor());
		String editorId = getEditorDescriptor().getId();
		IFile file = (IFile) getElement();
		page.openEditor(
				new FileEditorInput(file), editorId, activate, matchFlags);
		if (rememberEditor) {
			// only remember the default editor if the open succeeds
			IDE.setDefaultEditor(file, editorId);
		}
	}

	public ImageDescriptor getImageDescriptor() {
		IEditorRegistry registry = PlatformUI.getWorkbench()
				.getEditorRegistry();
		IFile file = (IFile) getElement();

		ImageDescriptor imageDesc;
		
		if (getEditorDescriptor() != null) {
			String editorId = getEditorDescriptor() == null ? null
				: getEditorDescriptor().getId();
			if (IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID.equals(editorId)
					|| IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID.equals(editorId)) {
				return registry.getSystemExternalEditorImageDescriptor(file
						.getName());
			}
			imageDesc = getEditorDescriptor().getImageDescriptor();
			if (imageDesc != null) {
				return imageDesc;
			}
		}
		
		// TODO: is this case valid, and if so, what are the implications
		// for content-type editor bindings?
		imageDesc = registry.getImageDescriptor(file.getName());
		if (imageDesc != null)
			return imageDesc;

		return super.getImageDescriptor();
	}
}
