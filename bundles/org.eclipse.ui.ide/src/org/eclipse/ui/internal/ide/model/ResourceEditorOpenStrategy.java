package org.eclipse.ui.internal.ide.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.provisional.ide.IEditorOpenStrategy;
import org.eclipse.ui.internal.provisional.ide.OpenWithEntry;
import org.eclipse.ui.internal.provisional.ide.OpenWithInfo;
import org.eclipse.ui.part.FileEditorInput;

/**
 * The IDE's editor open strategy for resources.
 * 
 * @since 3.2
 */
public class ResourceEditorOpenStrategy implements IEditorOpenStrategy {

	private IEditorRegistry registry = PlatformUI.getWorkbench()
			.getEditorRegistry();

	public ResourceEditorOpenStrategy() {
		// do nothing
	}

	public OpenWithInfo getOpenWithInfo(Object element) {
		IFile file = ResourceUtil.getFile(element);
		if (file == null) {
			return null;
		}
		HashSet alreadyMapped = new HashSet();
		IEditorDescriptor[] editors = registry.getEditors(file.getName(), IDE
				.getContentType(file));
		IEditorDescriptor preferredEditor = IDE.getDefaultEditor(file);
		List entries = new ArrayList(editors.length);
		OpenWithEntry preferredEntry = null;
		for (int i = 0; i < editors.length; i++) {
			IEditorDescriptor editor = editors[i];
			if (alreadyMapped.contains(editor))
				continue;
			alreadyMapped.add(editor);
			OpenWithEntry entry = new FileOpenWithEntry(editor, file);
			entries.add(entry);
			if (editor.equals(preferredEditor)) {
				preferredEntry = entry;
			}
		}
		OpenWithEntry externalEntry = null;
//		if (registry.isSystemExternalEditorAvailable(file.getName())) {
			IEditorDescriptor externalEditor = registry.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
			externalEntry = new FileOpenWithEntry(externalEditor, file);
			if (preferredEntry == null && externalEditor.equals(preferredEditor)) {
				preferredEntry = externalEntry;
			}
//		}
		OpenWithEntry inPlaceEntry = null;
		if (registry.isSystemInPlaceEditorAvailable(file.getName())) {
			IEditorDescriptor inPlaceEditor = registry
					.findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
			inPlaceEntry = new FileOpenWithEntry(inPlaceEditor, file);
			if (preferredEntry == null && inPlaceEntry.equals(preferredEditor)) {
				preferredEntry = externalEntry;
			}
		}
		OpenWithEntry defaultEntry = new OpenWithEntry(null, file) {
			public void openEditor(IWorkbenchPage page,
					boolean activate, int matchFlags, boolean rememberEditor)
					throws PartInitException {
				IFile f = (IFile) getElement();
				IDE.setDefaultEditor(f, null);
				IEditorDescriptor desc = IDE.getEditorDescriptor(f);
				page.openEditor(
						new FileEditorInput(f), desc.getId(), activate,
						matchFlags);
			}
		};
		IEditorDescriptor textEditor = registry
			.findEditor(IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID); // may be null
		if (!alreadyMapped.contains(textEditor)) {
			OpenWithEntry textEditorEntry = new FileOpenWithEntry(textEditor, file);
			entries.add(textEditorEntry);
			if (preferredEntry == null) {
				preferredEntry = textEditorEntry;
			}
		}
		OpenWithEntry[] entryArray = (OpenWithEntry[]) entries.toArray(new OpenWithEntry[entries.size()]);
		return new OpenWithInfo(entryArray, preferredEntry, externalEntry,
				inPlaceEntry, defaultEntry);
	}

}
