package org.eclipse.ui.internal;

public interface IEditorShortcutListener {
	public void shortcutRemoved(EditorShortcut shortcut); 
	public void shortcutAdded(EditorShortcut shortcut);
	public void shortcutRenamed(EditorShortcut shortcut);
}
