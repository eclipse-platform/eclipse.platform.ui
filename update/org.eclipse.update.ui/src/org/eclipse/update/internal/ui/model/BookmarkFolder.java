package org.eclipse.update.internal.ui.model;

import java.util.ArrayList;

public class BookmarkFolder extends NamedModelObject {
	protected ArrayList children= new ArrayList();
	public BookmarkFolder() {
	}
	
	public BookmarkFolder(String name) {
		super(name);
	}
	
	public Object[] getChildren(Object parent) {
		return children.toArray();
	}
	
	public boolean hasChildren() {
		return children.size()>0;
	} 

	public void addChild(NamedModelObject object) {
		internalAdd(object);
		notifyObjectsAdded(this, new Object[] {object});
	}	

	public void addChildren(NamedModelObject [] objects) {
		for (int i=0; i<objects.length; i++) {
			internalAdd(objects[i]);
		}
		notifyObjectsAdded(this, objects);
	}
	
	protected void internalAdd(NamedModelObject child) {
		children.add(child);
		child.setModel(getModel());
		child.setParent(this);
	}
	
	public void removeChildren(NamedModelObject [] objects) {
		for (int i=0; i<objects.length; i++) {
			children.remove(objects[i]);
			objects[i].setParent(null);
		}
		notifyObjectsRemoved(this, objects);
	}
}