package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.ui.TeamUIPlugin;

/**
 * Classes registered with the workbench decoration extension point. The <code>CVSDecorationRunnable</code> class
 * actually calculates the decoration, while this class is responsible for listening to the following sources
 * for indications that the decorators need updating:
 * <ul>
 * 	<li>workbench label requests (decorateText/decorateImage)
 * 	<li>workspace resource change events (resourceChanged)
 * 	<li>cvs state changes (resourceStateChanged)
 * </ul>
 * <p>
 * [Note: There are several optimization that can be implemented in this class: (1) cache something
 * so that computing the dirty state of containers does not imply traversal of all children, (2) improve
 * the queue used between the decorator and the decorator runnable such that priority can be
 * given to visible elements when decoration requests are made.]
 */
public class CVSDecorator extends TeamResourceDecorator implements IDecorationNotifier {

	// Resources that need an icon and text computed for display to the user, no order
	private Set decoratorNeedsUpdating = Collections.synchronizedSet(new HashSet());

	// When decorations are computed they are added to this cache via decorated() method
	private Map cache = Collections.synchronizedMap(new HashMap());

	// Updater thread, computes decoration labels and images
	private Thread decoratorUpdateThread;

	public CVSDecorator() {
		decoratorUpdateThread = new Thread(new CVSDecorationRunnable(this), "CVS");
		decoratorUpdateThread.start();
	}

	public String decorateText(String text, Object o) {
		IResource resource = getResource(o);
		if (resource == null || resource.getType() == IResource.ROOT)
			return text;
		if (getCVSProviderFor(resource) == null)
			return text;

		CVSDecoration decoration = (CVSDecoration) cache.get(resource);

		if (decoration != null) {
			String format = decoration.getFormat();
			if (format == null) {
				return text;
			} else {
				Map bindings = decoration.getBindins();
				if (bindings.isEmpty())
					return text;
				bindings.put(CVSDecoratorConfiguration.RESOURCE_NAME, text);
				return CVSDecoratorConfiguration.bind(format, bindings);
			}
		} else {
			addResourcesToBeDecorated(new IResource[] { resource });
			return text;
		}
	}

	public Image decorateImage(Image image, Object o) {
		IResource resource = getResource(o);
		if (resource == null || resource.getType() == IResource.ROOT)
			return image;
		if (getCVSProviderFor(resource) == null)
			return image;

		CVSDecoration decoration = (CVSDecoration) cache.get(resource);

		if (decoration != null) {
			List overlays = decoration.getOverlays();
			return overlays == null ? image : new OverlayIcon(image.getImageData(), new ImageDescriptor[][] {(ImageDescriptor[]) overlays.toArray(new ImageDescriptor[overlays.size()])}, new Point(16, 16)).createImage();
		} else {
			addResourcesToBeDecorated(new IResource[] { resource });
			return image;
		}
	}

	/*
	 * @see IDecorationNotifier#next()
	 */
	public synchronized IResource next() {
		try {
			if (decoratorNeedsUpdating.isEmpty()) {
				wait();
			}
			Iterator iterator = decoratorNeedsUpdating.iterator();
			IResource resource = (IResource) iterator.next();
			iterator.remove();

			//System.out.println("++ Next: " + resource.getFullPath() + " remaining in cache: " + cache.size());

			return resource;
		} catch (InterruptedException e) {
		}
		return null;
	}

	/*
	 * @see IDecorationNotifier#notify(IResource, CVSDecoration)
	 */
	public synchronized void decorated(IResource resource, CVSDecoration decoration) {
		cache.put(resource, decoration);
		postLabelEvents(new LabelProviderChangedEvent[] { new LabelProviderChangedEvent(this, resource)});
	}

	/*
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		//System.out.println(">> Resource Change Event");
		processDelta(event.getDelta());
	}

	/*
	 * @see IResourceStateChangeListener#resourceStateChanged(IResource[])
	 */
	public void resourceStateChanged(IResource[] changedResources) {
		// add depth first so that update thread processes parents first.
		//System.out.println(">> State Change Event");
		List resources = new ArrayList();
		for (int i = 0; i < changedResources.length; i++) {
			// ignore subtrees that aren't associated with a provider, this can happen on import
			// of a new project to CVS.
			if (getCVSProviderFor(changedResources[i]) == null) {
				continue;
			}
			resources.addAll(computeParents(changedResources[i]));
		}
		addResourcesToBeDecorated((IResource[]) resources.toArray(new IResource[resources.size()]));
	}

	public static void refresh() {
		try {
			IResource[] resources = ResourcesPlugin.getWorkspace().getRoot().members();
			for (int i = 0; i < resources.length; i++) {
				if (getCVSProviderFor(resources[i]) != null) {
					refresh(resources[i]);
				}
			}
		} catch (CoreException e) {
		}
	}

	public static void refresh(IResource resource) {
		final List resources = new ArrayList();
		try {
			resource.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) {
					resources.add(resource);
					return true;
				}
			});
			TeamPlugin.getManager().broadcastResourceStateChanges((IResource[]) resources.toArray(new IResource[resources.size()]));
		} catch (CoreException e) {
		}
	}

	private List computeParents(IResource resource) {
		IResource current = resource;
		List resources = new ArrayList();
		while (current.getType() != IResource.ROOT) {
			resources.add(current);
			current = current.getParent();
		}
		return resources;
	}

	private void processDelta(IResourceDelta delta) {
		final LinkedList events = new LinkedList();
		try {
			delta.accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource resource = delta.getResource();
					int type = resource.getType();

					// skip workspace root
					if (type == IResource.ROOT) {
						return true;
					}

					// ignore subtrees that aren't associated with a provider
					CVSTeamProvider p = getCVSProviderFor(resource);
					if (p == null) {
						return false;
					}

					// don't care about deletions
					if (delta.getKind() == IResourceDelta.REMOVED) {
						return false;
					}

					// handle .cvsignore changes that affect the state of peer files
					if (resource.getName().equals(".cvsignore") && type == IResource.FILE) {
						addResourcesToBeDecorated(resource.getParent().members());
					}

					// ignore subtrees that are ignored by team
					if (!p.hasRemote(resource)) {
						return false;
					}
					// chances are the team outgoing bit needs to be updated
					// if any child has changed.
					events.addFirst(resource);
					return true;
				}
			});
		} catch (CoreException e) {
			TeamUIPlugin.log(e.getStatus());
		}
		addResourcesToBeDecorated((IResource[]) events.toArray(new IResource[events.size()]));
	}

	private synchronized void addResourcesToBeDecorated(IResource[] resources) {
		if (resources.length > 0) {
			for (int i = 0; i < resources.length; i++) {
				//System.out.println("\t to update: " + resources[i].getFullPath());
				decoratorNeedsUpdating.add(resources[i]);
			}
			notify();
		}
	}

	/** 
	 * Answers null if a provider does not exist or the provider is not a CVS provider. These resources
	 * will be ignored by the decorator.
	 */
	private static CVSTeamProvider getCVSProviderFor(IResource resource) {
		ITeamProvider p = TeamPlugin.getManager().getProvider(resource);
		if (p == null || !(p instanceof CVSTeamProvider)) {
			return null;
		}
		return (CVSTeamProvider) p;
	}
}