package org.eclipse.ui.internal;

import java.util.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;

public class EditorShortcutManager {
	private List list;
	
	private ListenerList listeners = new ListenerList();
	
	public EditorShortcutManager() {
		list = new ArrayList();
	}
	
	public int indexof(EditorShortcut shortcut) {
		return list.indexOf(shortcut);
	}
	
	public boolean add(EditorShortcut item) {
		if (list.contains(item)) {
			return false;
		} else {
			list.add(item);
			Object list[] = listeners.getListeners();
			for (int i = 0; i < list.length; i++) {
				((IEditorShortcutListener)list[i]).shortcutAdded(item);
			}
			return true;
		}
	}
	
	public void remove(EditorShortcut item) {
		list.remove(item);
		Object list[] = listeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			((IEditorShortcutListener)list[i]).shortcutRemoved(item);
		}		
	}
	
	public EditorShortcut[] getItems() {
		EditorShortcut[] e = new EditorShortcut[list.size()];
		list.toArray(e);
		return e;
	}	
		
	public IStatus saveState(IMemento mem) {
		for (Iterator listIterator = list.iterator(); listIterator.hasNext();) {
			EditorShortcut editorShortcut = (EditorShortcut) listIterator.next();
			IMemento childMem = mem.createChild(IWorkbenchConstants.TAG_ITEM);
			editorShortcut.saveState(childMem);
		}
		return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null);
	}		

	public IStatus restoreState(IMemento mem) {
		IMemento children[] = mem.getChildren(IWorkbenchConstants.TAG_ITEM);
		for (int i = 0; i < children.length; i++) {
			EditorShortcut editorShortcut = (EditorShortcut.create(children[i]));
			add(editorShortcut);
		}
		return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null);
	}
	
	public void addListener(IEditorShortcutListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(IEditorShortcutListener listener) {
		listeners.remove(listener);
	}
	public void fireShortcutRename(EditorShortcut shortcut) {
		Object list[] = listeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			((IEditorShortcutListener)list[i]).shortcutRenamed(shortcut);
		}
	}
}
