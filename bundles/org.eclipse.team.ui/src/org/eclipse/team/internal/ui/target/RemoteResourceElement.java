/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ui.target;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.target.IRemoteTargetResource;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Used to show IRemoteTargetResource instances in the UI. In addition these elements
 * support caching of values returned from IRemoteTargetResource methods, as such these
 * instances aren't as much proxies as the underlying remote handles.
 * <p>
 * Implementation in progress: support for configuring these elements with a custom
 * progress monitor that can be the target of long running operations. This will
 * allow showing progress in wizards and in the SiteExplorerView when navigating
 * remote servers.</p>
 */
public class RemoteResourceElement implements IWorkbenchAdapter, IAdaptable {
	
	// remote resource this element represents
	private IRemoteTargetResource remote;
	
	// cache for the remote values
	private IRemoteResource[] children = null;
	private int size = 0;
	private String lastModified = null;

	// context in which to perform long-running operations
	private IRunnableContext runContext;
	
	public RemoteResourceElement(IRemoteTargetResource remote) {
		this.remote = remote;
	}
	
	public RemoteResourceElement(IRemoteTargetResource remote, IRunnableContext runContext) {
		this(remote);
		this.runContext = runContext;
	}
	
	public IRemoteTargetResource getRemoteResource() {
		return remote;
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) return this;
		return null;
	}

	public Object[] getChildren(Object o) {
		final Object[][] result = new Object[1][];
		try {	
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						// progress for fetching remote children is always unknown
						// let's not bother even trying to guess.
						monitor.beginTask(null, IProgressMonitor.UNKNOWN);
						if(children == null) {
							setCachedChildren(remote.members(Policy.subMonitorFor(monitor, 50)));
						}
						List remoteElements = new ArrayList();
						for (int i = 0; i < children.length; i++) {
							IRemoteTargetResource child = (IRemoteTargetResource)children[i];
							RemoteResourceElement element = new RemoteResourceElement(child, runContext);
									
							// cache size and last modified
							element.setLastModified(child.getLastModified(Policy.subMonitorFor(monitor, 25)));
							element.setSize(child.getSize(Policy.subMonitorFor(monitor, 25)));								
							remoteElements.add(element);
						}						
						result[0] = (RemoteResourceElement[])remoteElements.toArray(new RemoteResourceElement[remoteElements.size()]);														
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			};
			
			if(runContext == null) {
				TeamUIPlugin.runWithProgress(null, true /*cancelable*/, runnable);
			} else {
				runContext.run(true, true, runnable);
			}
		} catch (InterruptedException e) {
			return new Object[0];
		} catch (InvocationTargetException e) {
			TeamUIPlugin.handle(e.getTargetException());
			return new Object[0];
		}
		return result[0];
	}
	
	public ImageDescriptor getImageDescriptor(Object object) {
		if(remote.isContainer()) {
			return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
		} else {
			return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(remote.getName());
		}
	}
	
	public String getLabel(Object o) {
		// would be nice to display more than just the name (e.g. timestamp, size...)
		return remote.getName();
	}
	
	public Object getParent(Object o) {
		return null;
	}
	
	public int hashCode() {
		return getRemoteResource().hashCode();
	}
	
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof RemoteResourceElement))
			return false;
		return ((RemoteResourceElement)obj).getRemoteResource().equals(getRemoteResource());
	}
	
	public void clearChildren() {
		children = null;
	}
	
	public IRemoteResource[] getCachedChildren() {
		return children;
	}
	
	public void setCachedChildren(IRemoteResource[] children) {
		this.children = children;
	}
	
	protected void setRemoteResource(IRemoteTargetResource remote) {
		this.remote = remote;
	}
	
	public String getLastModified() {
		return lastModified;
	}

	public int getSize() {
		return size;
	}

	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public String getName() {
		return remote.getName();
	}
}