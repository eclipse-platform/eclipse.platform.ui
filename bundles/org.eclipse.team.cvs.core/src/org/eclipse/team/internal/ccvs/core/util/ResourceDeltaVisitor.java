package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.Policy;

public abstract class ResourceDeltaVisitor implements IResourceDeltaVisitor {

	private static IResourceChangeListener listener;
	private static ResourceDeltaVisitor visitor;
	
	private Map removals;
	private Map additions;
	private Map changes;
	
	public ResourceDeltaVisitor register() {
		if (visitor == null)
			visitor = this;
		if (listener == null)
			listener = new IResourceChangeListener() {
				public void resourceChanged(IResourceChangeEvent event) {
					try {
						IResourceDelta root = event.getDelta();
						IResourceDelta[] projectDeltas = root.getAffectedChildren();
						for (int i = 0; i < projectDeltas.length; i++) {
							IResourceDelta delta = projectDeltas[i];
							IResource resource = delta.getResource();
							ITeamProvider provider = TeamPlugin.getManager().getProvider(resource);
							if (provider instanceof CVSTeamProvider)
								delta.accept(visitor);
						}
						visitor.handle();
					} catch (CoreException e) {
						Util.logError(Policy.bind("ResourceDeltaVisitor.visitError"), e);
					}
				}
			};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
		return visitor;
	}
	
	public void deregister() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
	}
	
	public ResourceDeltaVisitor() {
		this.additions = new HashMap();
		this.removals = new HashMap();
		this.changes = new HashMap();
	}
	
	/**
	 * @see IResourceDeltaVisitor#visit(IResourceDelta)
	 */
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		IProject project = resource.getProject();
		switch (delta.getKind()) {
			case IResourceDelta.ADDED :
				addAddition(project, resource);
				break;
			case IResourceDelta.REMOVED :
				// Only record files as there's nothing we can do about folders
				addRemoval(project, resource);
				break;
			case IResourceDelta.CHANGED :
				if ((delta.getFlags() & IResourceDelta.MOVED_TO) > 0)
					addAddition(project, getResourceFor(project, resource, delta.getMovedToPath()));
				if ((delta.getFlags() & IResourceDelta.MOVED_FROM) > 0)
					addRemoval(project, getResourceFor(project, resource, delta.getMovedFromPath()));
				addChange(project, resource);
				break;
		}
		return true;
	}
	
	protected IResource getResourceFor(IProject container, IResource destination, IPath originating) {
		switch(destination.getType()) {
			case IResource.FILE : return container.getFile(originating); 			
			case IResource.FOLDER: return container.getFolder(originating);
			case IResource.PROJECT: return ResourcesPlugin.getWorkspace().getRoot().getProject(originating.toString());
		}
		return destination;
	}
	
	private void addChange(IProject project, IResource resource) {
		List changes = (List)this.changes.get(project);
		if (changes == null) {
			changes = new ArrayList();
			this.changes.put(project, changes);
		}
		changes.add(resource);
	}
	
	private void addAddition(IProject project, IResource resource) {
		List additions = (List)this.additions.get(project);
		if (additions == null) {
			additions = new ArrayList();
			this.additions.put(project, additions);
		}
		additions.add(resource);
	}
	
	private void addRemoval(IProject project, IResource resource) {
		List removals = (List)this.removals.get(project);
		if (removals == null) {
			removals = new ArrayList();
			this.removals.put(project, removals);
		}
		removals.add(resource);
	}
	
	private void handle() {
		
		handleAdded(additions);
		additions.clear();
		
		handleRemoved(removals);
		removals.clear();
		
		handleChanged(changes);
		changes.clear();
	}
	
	/**
	 * Handle all the additions, can be overwritten (not suggested)
	 * 
	 * The map contains lists of changes mapped to the IProjects 
	 * the changes are in. The default handling calles handleAdded
	 * for every entry.
	 */
	protected void handleAdded(Map additions) {
		// Start by printing out the changes
		//System.out.println("Resources added");
		Iterator i = additions.keySet().iterator();
		while (i.hasNext()) {
			IProject project = (IProject)i.next();
			ITeamProvider provider = TeamPlugin.getManager().getProvider(project);
			Iterator j = ((List)additions.get(project)).iterator();
			while (j.hasNext()) {
				IResource resource = (IResource)j.next();
				handleAdded(project,resource);
				//System.out.println(resource.toString());
			}
		}			
	}

	/**
	 * Handle all the changes, can be overwritten (not suggested)
	 * 
	 * The map contains lists of changes mapped to the IProjects 
	 * the changes are in. The default handling calles handleChanged
	 * for every entry.
	 */
	protected void handleChanged(Map changes) {
		// Start by printing out the changes
		//System.out.println("Resources added");
		Iterator i = changes.keySet().iterator();
		while (i.hasNext()) {
			IProject project = (IProject)i.next();
			ITeamProvider provider = TeamPlugin.getManager().getProvider(project);
			Iterator j = ((List)changes.get(project)).iterator();
			while (j.hasNext()) {
				IResource resource = (IResource)j.next();
				handleChanged(project,resource);
				//System.out.println(resource.toString());
			}
		}			
	}

	/**
	 * Handle all the removals, can be overwritten (not suggested)
	 * 
	 * The map contains lists of changes mapped to the IProjects 
	 * the changes are in. The default handling calles handleRemoved
	 * for every entry.
	 */	
	protected void handleRemoved(Map removals) {
		//System.out.println("Resources removed");
		Iterator i = removals.keySet().iterator();
		while (i.hasNext()) {
			IProject project = (IProject)i.next();
			ITeamProvider provider = TeamPlugin.getManager().getProvider(project);
			Iterator j = ((List)removals.get(project)).iterator();
			while (j.hasNext()) {
				IResource resource = (IResource)j.next();
				handleRemoved(project,resource);
				//System.out.println(resource.toString());
			}
		}
	}
	
	/**
	 * React on every addition
	 */
	protected abstract void handleAdded(IProject project,IResource resource);
	
	/**
	 * React on every removal
	 */
	protected abstract void handleRemoved(IProject project,IResource resource);
	
	/**
	 * React on every change
	 */
	protected abstract void handleChanged(IProject project,IResource resource);
	
}

