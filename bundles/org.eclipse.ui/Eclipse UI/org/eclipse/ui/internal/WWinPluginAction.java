package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
public class WWinPluginAction extends PluginAction {
	private IWorkbenchWindow window;
/**
 * Constructs a new WWinPluginAction object..
 */
public WWinPluginAction(IConfigurationElement actionElement, String runAttribute, IWorkbenchWindow window) {
	super(actionElement, runAttribute);
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
			"Action should implement IWorkbenchWindowActionDelegate: " + getText());
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
 * Returns true if the window has been set.  
 * The window may be null after the constructor is called and
 * before the window is stored.  We cannot create the delegate
 * at that time.
 */
public boolean isOkToCreateDelegate() {
	return super.isOkToCreateDelegate() && window != null;
}
}
