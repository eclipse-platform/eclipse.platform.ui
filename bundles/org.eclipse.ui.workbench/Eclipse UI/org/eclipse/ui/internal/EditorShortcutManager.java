package org.eclipse.ui.internal;

import java.util.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;

public class EditorShortcutManager {
	private List list;
	
	public EditorShortcutManager() {
		list = new ArrayList();
	}
	
	public void add(EditorShortcut item) {
		list.add(item);
	}
	
	public void remove (EditorShortcut item) {
		list.remove(item);
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
}
