package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;
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
	private String actionID;
/**
 * This class adds the requirement that action delegates
 * loaded on demand implement IViewActionDelegate
 */

public ViewPluginAction() {
	super();
}

public void init(IConfigurationElement actionElement, String runAttribute, IViewPart part) {
	viewPart = part;
	actionID = actionElement.getAttribute("id");
	init(actionElement, runAttribute);
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
	   if(delegate instanceof IPersistableAction) {
	   		ViewSite site = (ViewSite)viewPart.getViewSite();
	   		IPersistableAction persistable = (IPersistableAction)delegate;
	   		IMemento mem = site.getMemento(actionID);
	   		((IViewActionDelegate)delegate).init(viewPart);
	   		if(mem != null)
		   		((IPersistableAction)delegate).restoreState(this,viewPart,mem);
	   		site.addPersistableAction(actionID,persistable);
	   } else {
	   		((IViewActionDelegate)delegate).init(viewPart);
	   }
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
	if(viewPart == null)
		return false;
	if(super.isOkToCreateDelegate())
		return true;
	ViewSite site = (ViewSite)viewPart.getViewSite();
	return site.getMemento(actionID) != null;
}
}
