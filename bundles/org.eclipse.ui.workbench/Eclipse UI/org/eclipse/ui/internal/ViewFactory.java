/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.misc.UIStats;
import org.eclipse.ui.internal.registry.*;

/**
 * The ViewFactory is used to control the creation and disposal of views.  
 * It implements a reference counting strategy so that one view can be shared
 * by more than one client.
 */
public class ViewFactory {
	private WorkbenchPage page;
	private IViewRegistry viewReg;
	private ReferenceCounter counter;
	private HashMap mementoTable = new HashMap();

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
public IViewReference createView(final String id) throws PartInitException {
	IViewDescriptor desc = viewReg.find(id);
	if(desc == null)
		throw new PartInitException(WorkbenchMessages.format("ViewFactory.couldNotCreate", new Object[] {id})); //$NON-NLS-1$
	IViewReference ref = (IViewReference)counter.get(desc);
	if (ref == null) {
		ref = new ViewReference(id);
		counter.put(desc,ref);
	} else {
		counter.addRef(desc);
	}
	return ref;
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
public IStatus restoreView(final IViewReference ref) {
	final IStatus result[] = new IStatus[1];
	BusyIndicator.showWhile(
		page.getWorkbenchWindow().getShell().getDisplay(),
		new Runnable() {
			public void run() {
				result[0] = busyRestoreView(ref);
			}
		}
	);
	return result[0];
}
public IStatus busyRestoreView(final IViewReference ref) {
	if(ref.getPart(false) != null)
		return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$
		
	final String viewID = ref.getId();
	final IMemento stateMem = (IMemento)mementoTable.get(viewID);
	mementoTable.remove(viewID);
	
	final boolean resetPart[] = {true};
	final IStatus result[] = new IStatus[]{new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null)}; //$NON-NLS-1$
	Platform.run(new SafeRunnable() {
		public void run() {
			IViewDescriptor desc = viewReg.find(viewID);
			if(desc == null) {
				result[0] = new Status(
					IStatus.ERROR,PlatformUI.PLUGIN_ID,0,
					WorkbenchMessages.format("ViewFactory.couldNotCreate", new Object[] {viewID}), //$NON-NLS-1$
					null);
				return;
			}
		
			// Create the view.
			IViewPart view = null;
			String label = desc.getLabel();
			try {
				try {
					UIStats.start(UIStats.CREATE_PART,label);
					view = desc.createView();
				} finally {
					UIStats.end(UIStats.CREATE_PART,label);
				}
				((ViewReference)ref).setPart(view);
			} catch (CoreException e) {
				PartPane pane = ((ViewReference)ref).getPane();
				if(pane != null) {
					page.getPerspectivePresentation().removePart(pane);
					pane.dispose();
				}
				result[0] = new Status(
					IStatus.ERROR,PlatformUI.PLUGIN_ID,0,
					WorkbenchMessages.format("ViewFactory.initException", new Object[] {desc.getID()}), //$NON-NLS-1$
					e);
				return;
			}
			
			// Create site
			ViewSite site = new ViewSite(view, page, desc);
			try {
				try {
					UIStats.start(UIStats.INIT_PART,label);
					view.init(site,stateMem);
				} finally {
					UIStats.end(UIStats.INIT_PART,label);
				}
			} catch (PartInitException e) {
				releaseView(viewID);
				result[0] = new Status(
					IStatus.ERROR,PlatformUI.PLUGIN_ID,0,
					WorkbenchMessages.format("Perspective.exceptionRestoringView",new String[]{viewID}), //$NON-NLS-1$
					e);
				return;
			}				
			if (view.getSite() != site)  {
				releaseView(viewID);
				result[0] = new Status(
					IStatus.ERROR,PlatformUI.PLUGIN_ID,0,
					WorkbenchMessages.format("ViewFactory.siteException", new Object[] {desc.getID()}), //$NON-NLS-1$
					null);
				return;
			}
		
			PartPane pane = ((ViewReference)ref).getPane();
			if(pane == null) {
				pane = new ViewPane(ref,page);
				((ViewReference)ref).setPane(pane);
			}
			site.setPane(pane);
			site.setActionBars(new ViewActionBars(page.getActionBars(), (ViewPane)pane));
			resetPart[0] = false;
			site.getPane().createChildControl();
			result[0] =  new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$
		}
		public void handleException(Throwable e) {
			if(resetPart[0]) {
				((ViewReference)ref).setPart(null);
				page.hideView(ref);
			}
			//Execption is already logged.
			result[0] = new Status(
				IStatus.ERROR,PlatformUI.PLUGIN_ID,0,
				WorkbenchMessages.format("Perspective.exceptionRestoringView",new String[]{viewID}), //$NON-NLS-1$
				e);

		}
	});
	return result[0];
}

public IStatus saveState(IMemento memento) {
	final MultiStatus result = new MultiStatus(
		PlatformUI.PLUGIN_ID,IStatus.OK,
		WorkbenchMessages.getString("ViewFactory.problemsSavingViews"),null); //$NON-NLS-1$
	
	final IViewReference refs[] = getViews();
	for (int i = 0; i < refs.length; i++) {
		final IMemento viewMemento = memento.createChild(IWorkbenchConstants.TAG_VIEW);
		viewMemento.putString(IWorkbenchConstants.TAG_ID, refs[i].getId());
		final IViewPart view = (IViewPart)refs[i].getPart(false);
		if(view != null) {
			final int index = i;
			Platform.run(new SafeRunnable() {
				public void run() {
					view.saveState(viewMemento.createChild(IWorkbenchConstants.TAG_VIEW_STATE));
				}
				public void handleException(Throwable e) {
					result.add(new Status(
						IStatus.ERROR,PlatformUI.PLUGIN_ID,0,
						WorkbenchMessages.format("ViewFactory.couldNotSave",new String[]{refs[index].getTitle()}), //$NON-NLS-1$
						e));
				}
			});
		} else {
			IMemento mem = (IMemento)mementoTable.get(refs[i].getId());
			if(mem != null) {
				IMemento child = viewMemento.createChild(IWorkbenchConstants.TAG_VIEW_STATE);
				child.putMemento(mem);
			}
		}
	}
	return result;
}
public IStatus restoreState(IMemento memento) {
	IMemento mem[] = memento.getChildren(IWorkbenchConstants.TAG_VIEW);
	for (int i = 0; i < mem.length; i++) {
		String id = mem[i].getString(IWorkbenchConstants.TAG_ID);
		mementoTable.put(id,mem[i].getChild(IWorkbenchConstants.TAG_VIEW_STATE));
	}
	return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$
}
/**
 * Remove a view rec from the manager.
 *
 * The IViewPart.dispose method must be called at a higher level.
 */
private void destroyView(IViewDescriptor desc, IViewPart view) 
{
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
public IViewReference getView(String id) {
	IViewDescriptor desc = viewReg.find(id);
	return (IViewReference) counter.get(desc);
}
/**
 * Returns a list of views which are open.
 */
public IViewReference[] getViews() {
	List list = counter.values();
	IViewReference [] array = new IViewReference[list.size()];
	for (int i = 0; i < array.length; i++) {
		array[i] = (IViewReference)list.get(i);
	}
	return array;
}
/**
 * Returns whether a view with the given id exists.
 */
public boolean hasView(String id) {
	IViewDescriptor desc = viewReg.find(id);
	Object view = counter.get(desc);
	return (view != null);
}
/**
 * Releases an instance of a view defined by id.
 *
 * This factory does reference counting.  For more info see
 * getView.
 */
public void releaseView(String id) {
	IViewDescriptor desc = viewReg.find(id);
	IViewReference ref = (IViewReference)counter.get(desc);
	if (ref == null)
		return;
	int count = counter.removeRef(desc);
	if (count <= 0) {
		IViewPart view = (IViewPart)ref.getPart(false);
		if(view != null)
			destroyView(desc, view);
	}
}

private class ViewReference extends WorkbenchPartReference implements IViewReference {
	
	boolean create = true;

	public ViewReference(String id) {
		ViewDescriptor desc = (ViewDescriptor)viewReg.find(id);
		ImageDescriptor iDesc = null;
		String title = null;
		if(desc != null) {
			iDesc = desc.getImageDescriptor();
			title = desc.getLabel();
		}
		init(id,title,null,iDesc);
	}

	/**
	 * @see IViewReference#isFastView()
	 */
	public boolean isFastView() {
		return page.isFastView(this);
	}
	public IViewPart getView(boolean restore) {
		return (IViewPart)getPart(restore);
	}
	public String getRegisteredName() {
		if(part != null)
			return part.getSite().getRegisteredName();
			
		IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
		IViewDescriptor desc = reg.find(getId());
		if(desc != null)
			return desc.getLabel();	
		return getTitle();
	}
	/**
	 * @see IWorkbenchPartReference#getPart(boolean)
	 */
	public IWorkbenchPart getPart(boolean restore) {
		if(part != null)
			return part;
		if(!create)
			return null;
		if(restore) {
			IStatus status = restoreView(this);
			if(status.getSeverity() == IStatus.ERROR) {
				create = false;
				Workbench workbench = (Workbench)PlatformUI.getWorkbench();
				if(!workbench.isStarting()) {
					ErrorDialog.openError(
						page.getWorkbenchWindow().getShell(),
						WorkbenchMessages.getString("ViewFactory.unableToRestoreViewTitle"), //$NON-NLS-1$
						WorkbenchMessages.format("ViewFactory.unableToRestoreViewMessage",new String[]{getTitle()}), //$NON-NLS-1$
						status,
						IStatus.WARNING | IStatus.ERROR);
				} 
			} else {
				releaseReferences();
			}			
		}
		return part;
	}
	public IWorkbenchPage getPage() {
		return page;
	}
	public void dispose() {
		super.dispose();
		create = false;
	}
}

}
