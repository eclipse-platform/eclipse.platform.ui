package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.*;
/**
 * Extends PartPluginAction for usage in editor parts. Objects
 * of this class are created by reading the registry (extension point "editorActions").
 */
public final class EditorPluginAction extends PartPluginAction {
	private IEditorPart currentEditor;
	private String actionID;
/**
 * This class adds the requirement that action delegates
 * loaded on demand implement IViewActionDelegate
 */
public EditorPluginAction() {
	super();
}
/**
 * Runs the action.
 */
public void run() {
	// this message dialog is problematic.
	if (getDelegate() == null) {
		setDelegate(createDelegate());
		editorChanged(currentEditor);
	}
	super.run();
}
/**
 * This class adds the requirement that action delegates
 * loaded on demand implement IViewActionDelegate
 */
public void init(IConfigurationElement actionElement, String runAttribute, IEditorPart part) {
	super.init(actionElement, runAttribute);
	actionID = actionElement.getAttribute("id");
	if (part != null) 
		editorChanged(part);
}
/**
 * Creates an instance of the delegate class as defined on
 * the configuration element. It will also initialize
 * it with the editor part.
 */
protected IActionDelegate createDelegate() {
	IActionDelegate delegate = super.createDelegate();
	if (delegate == null)
		return null;
	if (!(delegate instanceof IEditorActionDelegate)) {
		WorkbenchPlugin.log("Action should implement IEditorActionDelegate: " + getText());//$NON-NLS-1$
		return null;
	}
	return delegate;
}
/**
 * Handles editor change by re-registering for selection
 * changes and updating IEditorActionDelegate.
 */

public void editorChanged(IEditorPart part) {
	if (currentEditor!=null) {
		unregisterSelectionListener(currentEditor);
	}
	currentEditor = part;
	if (getDelegate() == null) {
		if (isOkToCreateDelegate())
		   setDelegate(createDelegate());
	}
	if (getDelegate() != null) {
		IEditorActionDelegate editorDelegate = (IEditorActionDelegate)getDelegate();
		if(editorDelegate instanceof IPersistableAction) {
			PartSite site = (PartSite)currentEditor.getSite();
			if(site.getPersistableAction(actionID) == null) {
				IPersistableAction persistable = (IPersistableAction)editorDelegate;
				IMemento mem = site.getMemento(actionID);
		   		if(mem != null)
			   		((IPersistableAction)editorDelegate).restoreState(this,part,mem);
		   		site.addPersistableAction(actionID,persistable);
			}
		}
		editorDelegate.setActiveEditor(this, part);
	}
	registerSelectionListener(part);
}
/**
 * Returns true if the view has been set
 * The view may be null after the constructor is called and
 * before the view is stored.  We cannot create the delegate
 * at that time.
 */
public boolean isOkToCreateDelegate() {
	if(currentEditor == null)
		return false;
	if(super.isOkToCreateDelegate())
		return true;
	PartSite site = (PartSite)currentEditor.getSite();
	return site.getMemento(actionID) != null;
}
}
