package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.internal.ViewPane;
import java.util.*;

/**
 * The ViewFactory is used to control the creation and disposal of views.  
 * It implements a reference counting strategy so that one view can be shared
 * by more than one client.
 */
public class ViewFactory
{
	static boolean DEBUG = false;
	private WorkbenchPage page;
	private IViewRegistry viewReg;
	private ReferenceCounter counter;

/**
 * ViewManager constructor comment.
 */
public ViewFactory(WorkbenchPage page, IViewRegistry reg) {
	super();
	this.page = page;
	this.viewReg = reg;
	counter = new ReferenceCounter();
}
/**
 * Creates an instance of a view defined by id.
 * 
 * This factory implements reference counting.  The first call to this
 * method will return a new view.  Subsequent calls will return the
 * first view with an additional reference count.  The view is
 * disposed when releaseView is called an equal number of times
 * to getView.
 */
public ViewPane createView(String id) 
	throws PartInitException 
{
	return createView(id,null);
}
/**
 * Creates an instance of a view defined by id.
 * 
 * This factory implements reference counting.  The first call to this
 * method will return a new view.  Subsequent calls will return the
 * first view with an additional reference count.  The view is
 * disposed when releaseView is called an equal number of times
 * to getView.
 */
public ViewPane createView(String id,IMemento memento) 
	throws PartInitException 
{
	IViewDescriptor desc = viewReg.find(id);
	if(desc == null)
		throw new PartInitException(WorkbenchMessages.format("ViewFactory.couldNotCreate", new Object[] {id})); //$NON-NLS-1$
	IViewPart view = (IViewPart)counter.get(desc);
	if (view == null) {
		view = createView(desc,memento);
	} else {
		counter.addRef(desc);
	}
	PartSite site = (PartSite)view.getSite();
	return (ViewPane)site.getPane();
}
/**
 * Create a view rec with the given type and parent. 
 */
private IViewPart createView(IViewDescriptor desc,IMemento memento)
	throws PartInitException
{
	// Debugging
	if (DEBUG)
		System.out.println("Create " + desc.getLabel());//$NON-NLS-1$

	// Create the view.
	IViewPart view = null;
	try {
		view = desc.createView();
	} catch (CoreException e) {
		throw new PartInitException(WorkbenchMessages.format("ViewFactory.initException", new Object[] {desc.getID()})); //$NON-NLS-1$
	}
	
	// Create site
	ViewSite site = new ViewSite(view, page, desc);
	view.init(site,memento);
	if (view.getSite() != site)
		throw new PartInitException(WorkbenchMessages.format("ViewFactory.siteException", new Object[] {desc.getID()})); //$NON-NLS-1$


	// Create pane, etc.
	ViewPane pane = new ViewPane(view, page);
	site.setPane(pane);
	site.setActionBars(new ViewActionBars(page.getActionBars(), pane));
	
	// Add ref to view.
	counter.put(desc, view);
	
	// Return view.
	return view;
}
/**
 * Remove a view rec from the manager.
 *
 * The IViewPart.dispose method must be called at a higher level.
 */
private void destroyView(IViewDescriptor desc, IViewPart view) 
{
	// Debugging
	if (DEBUG)
		System.out.println("Dispose " + desc.getLabel());//$NON-NLS-1$

	// Free action bars, pane, etc.
	PartSite site = (PartSite)view.getSite();
	ViewActionBars actionBars = (ViewActionBars)site.getActionBars();
	actionBars.dispose();
	PartPane pane = site.getPane();
	pane.dispose();

	// Free the site.
	site.dispose();
}
/**
 * Returns the view with the given id, or <code>null</code> if not found.
 */
public IViewPart getView(String id) {
	IViewDescriptor desc = viewReg.find(id);
	return (IViewPart) counter.get(desc);
}
/**
 * Returns a list of views which are open.
 */
public IViewPart [] getViews() {
	List list = counter.values();
	IViewPart [] array = new IViewPart[list.size()];
	for (int i = 0; i < array.length; i++) {
		array[i] = (IViewPart)list.get(i);
	}
	return array;
}
/**
 * Returns whether a view with the given id exists.
 */
public boolean hasView(String id) {
	IViewDescriptor desc = viewReg.find(id);
	IViewPart view = (IViewPart)counter.get(desc);
	return (view != null);
}
/**
 * Releases an instance of a view defined by id.
 *
 * This factory does reference counting.  For more info see
 * getView.
 */
public void releaseView(String id) 
{
	IViewDescriptor desc = viewReg.find(id);
	IViewPart view = (IViewPart)counter.get(desc);
	if (view == null)
		return;
	int count = counter.removeRef(desc);
	if (count <= 0)
		destroyView(desc, view);
}
}
