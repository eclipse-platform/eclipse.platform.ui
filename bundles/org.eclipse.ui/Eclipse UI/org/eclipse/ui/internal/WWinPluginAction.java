package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.*;

/**
 * This class extends regular plugin action with the
 * additional requirement that the delegate has
 * to implement interface IWorkbenchWindowActionDeelgate.
 * This interface has one additional method (init)
 * whose purpose is to initialize the delegate with
 * the window in which the action is intended to run.
 */
public class WWinPluginAction extends PluginAction
	implements IActionSetContributionItem
{
	private IWorkbenchWindow window;
	private String actionSetId;
/**
 * Constructs a new WWinPluginAction object..
 */
public WWinPluginAction() {
	super();
}

public void init(IConfigurationElement actionElement, String runAttribute, IWorkbenchWindow window) {
	super.init(actionElement, runAttribute);
	this.window = window;
	window.getSelectionService().addSelectionListener(this);
}
/**
 * Creates an instance of the delegate class as defined on
 * the configuration element. It will also initialize
 * it with the view part.
 */
protected IActionDelegate createDelegate() {
	IActionDelegate delegate = super.createDelegate();
	if (delegate == null)
		return null;
	if (delegate instanceof IWorkbenchWindowActionDelegate) {
		IWorkbenchWindowActionDelegate winDelegate =
			(IWorkbenchWindowActionDelegate) delegate;
		winDelegate.init(window);
		return delegate;
	} else {
		WorkbenchPlugin.log(
			"Action should implement IWorkbenchWindowActionDelegate: " + getText());//$NON-NLS-1$
		return null;
	}
}
/**
 * Disposes of the action and any resources held.
 */
public void dispose() {
	window.getSelectionService().removeSelectionListener(this);
	if (getDelegate() instanceof IWorkbenchWindowActionDelegate) {
		IWorkbenchWindowActionDelegate winDelegate =
			(IWorkbenchWindowActionDelegate) getDelegate();
		winDelegate.dispose();
	}
}
/**
 * Returns the action set id.
 */
public String getActionSetId() {
	return actionSetId;
}
/**
 * Returns true if the window has been set.  
 * The window may be null after the constructor is called and
 * before the window is stored.  We cannot create the delegate
 * at that time.
 */
public boolean isOkToCreateDelegate() {
	return super.isOkToCreateDelegate() && window != null;
}
/**
 * Sets the action set id.
 */
public void setActionSetId(String newActionSetId) {
	actionSetId = newActionSetId;
}
}
