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
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.ccvs.core.CVSTeamProvider;

public class ResourceDeltaVisitor implements IResourceDeltaVisitor {

	private static IResourceChangeListener listener;
	private static ResourceDeltaVisitor visitor;
	
	private Map removals;
	private Map additions;
	
	public static ResourceDeltaVisitor register() {
		if (visitor == null)
			visitor = new ResourceDeltaVisitor();
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
						visitor.handleChanges();
					} catch (CoreException e) {
						Util.logError(Policy.bind("ResourceDeltaVisitor.visitError"), e);
					}
				}
			};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
		return visitor;
	}
	
	public static void deregister() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
	}
	
	public ResourceDeltaVisitor() {
		this.additions = new HashMap();
		this.removals = new HashMap();
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
					addAddition(project, project.getFile(delta.getMovedToPath()));
				if ((delta.getFlags() & IResourceDelta.MOVED_FROM) > 0)
					addRemoval(project, project.getFile(delta.getMovedFromPath()));
				break;
		}
		return true;
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
		// Only record files as there's nothing we can do about folders
		if (resource.getType() != IResource.FILE)
			return;
		List removals = (List)this.removals.get(project);
		if (removals == null) {
			removals = new ArrayList();
			this.removals.put(project, removals);
		}
		removals.add(resource);
	}
	
	private void handleChanges() {
		// Start by printing out the changes
		//System.out.println("Resources added");
		Iterator i = additions.keySet().iterator();
		while (i.hasNext()) {
			IProject project = (IProject)i.next();
			ITeamProvider provider = TeamPlugin.getManager().getProvider(project);
			Iterator j = ((List)additions.get(project)).iterator();
			while (j.hasNext()) {
				IResource resource = (IResource)j.next();
				//System.out.println(resource.toString());
			}
		}	
		additions.clear();
		
		//System.out.println("Resources removed");
		i = removals.keySet().iterator();
		while (i.hasNext()) {
			IProject project = (IProject)i.next();
			ITeamProvider provider = TeamPlugin.getManager().getProvider(project);
			Iterator j = ((List)removals.get(project)).iterator();
			while (j.hasNext()) {
				IResource resource = (IResource)j.next();
				//System.out.println(resource.toString());
			}
		}	
		removals.clear();
	}
}

