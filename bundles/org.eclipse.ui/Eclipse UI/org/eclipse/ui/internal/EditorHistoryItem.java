package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.ui.*;

/**
 * An item in the editor history.
 */
public class EditorHistoryItem {
	private IEditorInput input;
	private IEditorDescriptor descriptor;

/**
 * Constructs a new item.
 */	
public EditorHistoryItem() {
}
/**
 * Constructs a new item.
 */	
public EditorHistoryItem(IEditorInput input, IEditorDescriptor descriptor) {
	this.input = input;
	this.descriptor = descriptor;
}
/**
 * Returns the editor descriptor.
 * 
 * @return the editor descriptor.
 */
public IEditorDescriptor getDescriptor() {
	return descriptor;
}
/**
 * Returns the editor input.
 * 
 * @return the editor input.
 */
public IEditorInput getInput() {
	return input;
}
/**
 * Restores the object state from the given memento. 
 * 
 * @param memento the memento to restore the object state from
 */
public IStatus restoreState(IMemento memento) {
	String factoryId = memento.getString(IWorkbenchConstants.TAG_FACTORY_ID);
	
	Status result = new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null);
	if (factoryId == null) {
		WorkbenchPlugin.log("Unable to restore mru list - no input factory ID.");//$NON-NLS-1$
		return result;
	}
	IElementFactory factory = WorkbenchPlugin.getDefault().getElementFactory(factoryId);
	if (factory == null) {
		return result;
	}
	IMemento persistableMemento = memento.getChild(IWorkbenchConstants.TAG_PERSISTABLE);
	if (persistableMemento == null) {
		WorkbenchPlugin.log("Unable to restore mru list - no input element state: " + factoryId);//$NON-NLS-1$
		return result;
	}
	IAdaptable adaptable = factory.createElement(persistableMemento);
	if (adaptable == null || (adaptable instanceof IEditorInput) == false) {
		return result;
	}
	input = (IEditorInput) adaptable;
	// Get the editor descriptor.
	String editorId = memento.getString(IWorkbenchConstants.TAG_ID);
	if (editorId != null) {
		IEditorRegistry registry = WorkbenchPlugin.getDefault().getEditorRegistry();
		descriptor = registry.findEditor(editorId);
	}
	return result;
}
/**
 * Saves the object state in the given memento. 
 * 
 * @param memento the memento to save the object state in
 */
public void saveState(IMemento memento) {
	IPersistableElement persistable = input.getPersistable();
	
	if (persistable != null) {
		/*
		 * Store IPersistable of the IEditorInput in a separate section
		 * since it could potentially use a tag already used in the parent 
		 * memento and thus overwrite data.
		 */	
		IMemento persistableMemento = memento.createChild(IWorkbenchConstants.TAG_PERSISTABLE);
		persistable.saveState(persistableMemento);
		memento.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
		if (descriptor != null && descriptor.getId() != null) {
			memento.putString(IWorkbenchConstants.TAG_ID, descriptor.getId());
		}
	}
}
}
