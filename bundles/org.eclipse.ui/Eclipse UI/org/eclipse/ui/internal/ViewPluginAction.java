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
 * to implement interface IViewActionDeelgate.
 * This interface has one additional method (init)
 * whose purpose is to initialize the delegate with
 * the view part in which the action is intended to run.
 */
public final class ViewPluginAction extends PartPluginAction {
	private IViewPart viewPart;
/**
 * This class adds the requirement that action delegates
 * loaded on demand implement IViewActionDelegate
 */
public ViewPluginAction(IConfigurationElement actionElement, String runAttribute, IViewPart part) {
	super(actionElement, runAttribute);
	viewPart = part;
	registerSelectionListener(viewPart);
}
/**
 * Creates an instance of the delegate class as defined on
 * the configuration element. It will also initialize
 * it with the view part.
 */
protected IActionDelegate createDelegate() {
   IActionDelegate delegate = super.createDelegate();
   if (delegate == null) return null;
   if (delegate instanceof IViewActionDelegate) {
	   IViewActionDelegate viewDelegate = (IViewActionDelegate)delegate;
	   viewDelegate.init(viewPart);
//	   refreshSelection(viewPart);
   }
   else {
	  WorkbenchPlugin.log("Action should implement IViewActionDelegate: "+getText());//$NON-NLS-1$
	  return null;
   }
   return delegate;
}
/**
 * Returns true if the view has been set
 * The view may be null after the constructor is called and
 * before the view is stored.  We cannot create the delegate
 * at that time.
 */
public boolean isOkToCreateDelegate() {
	return super.isOkToCreateDelegate() && viewPart != null;
}
}
