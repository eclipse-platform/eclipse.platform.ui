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
public ViewPluginAction(IConfigurationElement actionElement, String runAttribute, IViewPart viewPart) {
	super(actionElement, runAttribute);
	this.viewPart = viewPart;
	registerSelectionListener(viewPart);
}

/** 
 * Initialize an action delegate.
 * Subclasses may override this.
 */
protected IActionDelegate initDelegate(Object obj) 
	throws WorkbenchException
{
	if (obj instanceof IViewActionDelegate) {
		IViewActionDelegate vad = (IViewActionDelegate)obj;
		vad.init(viewPart);
		return vad;
	} else
		throw new WorkbenchException("Action must implement IViewActionDelegate"); //$NON-NLS-1$
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
