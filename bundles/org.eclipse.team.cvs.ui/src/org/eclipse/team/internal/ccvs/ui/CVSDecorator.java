package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.IResourceStateChangeListener;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.CVSProvider;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.ResourceDeltaVisitor;

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
public class CVSDecorator extends LabelProvider implements ILabelDecorator, IResourceStateChangeListener, IDecorationNotifier {

	private static CVSDecorator theDecorator = null;
	
	// Resources that need an icon and text computed for display to the user
	private List decoratorNeedsUpdating = new ArrayList();

	// When decorations are computed they are added to this cache via decorated() method
	private Map cache = Collections.synchronizedMap(new HashMap());

	// Updater thread, computes decoration labels and images
	private Thread decoratorUpdateThread;

	private boolean shutdown = false;
	
	private Hashtable imageCache = new Hashtable();
	
	private ChangeListener changeListener;
	
	private class ChangeListener extends ResourceDeltaVisitor {
		List changedResources = new ArrayList();
		protected void handleAdded(IResource[] resources) {
		}
		protected void handleRemoved(IResource[] resources) {
		}
		protected void handleChanged(IResource[] resources) {
			changedResources.addAll(Arrays.asList(resources));
		}
		protected void finished() {
			resourceStateChanged((IResource[])changedResources.toArray(new IResource[changedResources.size()]));
		}
		protected int getEventMask() {
			return IResourceChangeEvent.PRE_AUTO_BUILD;
		}
	}
	
	public CVSDecorator() {
		// The decorator is a singleton, there should never be more than one instance.
		// temporary until the UI component properly calls dispose when the workbench shutsdown
		// UI Bug 9633
		Assert.isTrue(theDecorator==null);
		theDecorator = this;
		
		decoratorUpdateThread = new Thread(new CVSDecorationRunnable(this), "CVS"); //$NON-NLS-1$
		decoratorUpdateThread.start();
		TeamPlugin.getManager().addResourceStateChangeListener(this);
		changeListener = new ChangeListener();
		changeListener.register();
	}

	public String decorateText(String text, Object o) {
		IResource resource = getResource(o);
		if (resource == null || text == null || resource.getType() == IResource.ROOT)
			return text;
		if (getCVSProviderFor(resource) == null)
			return text;

		CVSDecoration decoration = (CVSDecoration) cache.get(resource);

		if (decoration != null) {
			String format = decoration.getFormat();
			if (format == null) {
				return text;
			} else {
				Map bindings = decoration.getBindings();
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
		if (resource == null || image == null || resource.getType() == IResource.ROOT)
			return image;
		if (getCVSProviderFor(resource) == null)
			return image;

		CVSDecoration decoration = (CVSDecoration) cache.get(resource);

		if (decoration != null) {
			List overlays = decoration.getOverlays();
			return overlays == null ? image : getCachedImage(image, overlays);
		} else {
			addResourcesToBeDecorated(new IResource[] { resource });
			return image;
		}
	}

	/**
	 * Get the composite image for the given image and overlays. Return one from the cache if
	 * it exists. If not, create it, cache it, and return it.
	 */
	private Image getCachedImage(Image image, List overlays) {
		Hashtable overlayTable = (Hashtable)imageCache.get(image);
		if (overlayTable == null) {
			overlayTable = new Hashtable();
			imageCache.put(image, overlayTable);
		}
		Image cachedImage = (Image)overlayTable.get(overlays);
		if (cachedImage == null) {
			cachedImage = new OverlayIcon(image.getImageData(), new ImageDescriptor[][] {(ImageDescriptor[])overlays.toArray(new ImageDescriptor[overlays.size()])}, new Point(16, 16)).createImage();
			overlayTable.put(overlays, cachedImage);
		}
		return cachedImage;
	}
	
	/*
	 * @see IDecorationNotifier#next()
	 */
	public synchronized IResource next() {
		try {
			if (shutdown) return null;
			
			if (decoratorNeedsUpdating.isEmpty()) {
				wait();
			}
			// We were awakened.
			if (shutdown) {
				// The decorator was awakened by the plug-in as it was shutting down.
				return null;
			}
			IResource resource = (IResource) decoratorNeedsUpdating.remove(0);

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
		// ignore resources that aren't in the workbench anymore.
		if(resource.exists() && !shutdown) {
			cache.put(resource, decoration);
			postLabelEvents(new LabelProviderChangedEvent[] { new LabelProviderChangedEvent(this, resource)});
		}
	}

	/*
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */

	/*
	 * @see IResourceStateChangeListener#resourceStateChanged(IResource[])
	 */
	public void resourceStateChanged(IResource[] changedResources) {
		// add depth first so that update thread processes parents first.
		//System.out.println(">> State Change Event");
		List resources = new ArrayList();
		List noProviderResources = new ArrayList();
		for (int i = 0; i < changedResources.length; i++) {
			// ignore subtrees that aren't associated with a provider, this can happen on import
			// of a new project to CVS.
			IResource resource = changedResources[i];
			if (getCVSProviderFor(resource) == null) {
				// post a changed event but forget any cached information about this resource
				noProviderResources.add(resource);
			}
			resources.addAll(computeParents(resource));
		}
		
		addResourcesToBeDecorated((IResource[]) resources.toArray(new IResource[resources.size()]));
		
		// post label events for resources that cannot or should not be decorated by CVS
		if(!noProviderResources.isEmpty()) {
			List events = new ArrayList();
			for (Iterator it = resources.iterator(); it.hasNext();) {
				IResource element = (IResource) it.next();
				events.add(new LabelProviderChangedEvent(this, element));
			}
			postLabelEvents((LabelProviderChangedEvent[]) events.toArray(new LabelProviderChangedEvent[events.size()]));
		}
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
	
	private synchronized void addResourcesToBeDecorated(IResource[] resources) {
		if (resources.length > 0) {
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				//System.out.println("\t to update: " + resource.getFullPath());
				if(!decoratorNeedsUpdating.contains(resource)) {
					//System.out.println("\t adding: " + resource.getFullPath());
					decoratorNeedsUpdating.add(resource);
				}
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
	
	/**
	 * Returns the resource for the given input object, or
	 * null if there is no resource associated with it.
	 * 
	 * @param object  the object to find the resource for
	 * @return the resource for the given object, or null
	 */
	private IResource getResource(Object object) {
		if (object instanceof IResource) {
			return (IResource)object;
		}
		if (object instanceof IAdaptable) {
			return (IResource)((IAdaptable)object).getAdapter(IResource.class);
		}
		return null;
	}

	/**
	 * Post the label events to the UI thread
	 * 
	 * @param events  the events to post
	 */
	private void postLabelEvents(final LabelProviderChangedEvent[] events) {
		// now post the change events to the UI thread
		if (events.length > 0) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					for (int i = 0; i < events.length; i++) {
						fireLabelProviderChanged(events[i]);
					}
				}
			});
		}
	} 
	
	private void shutdown() {
		shutdown = true;
		// Wake the thread up if it is asleep.
		synchronized (this) {
			notifyAll();
		}
		try {
			// Wait for the decorator thread to finish before returning.
			decoratorUpdateThread.join();
		} catch (InterruptedException e) {
		}
	}

	public static void shutdownAll() {
		if(theDecorator!=null) {
			theDecorator.dispose();
		}
	}
	
	/*
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		
		// terminate decoration thread
		shutdown();
		
		// unregister change listeners
		changeListener.register();
		TeamPlugin.getManager().removeResourceStateChangeListener(this);
		
		// dispose of images created as overlays
		decoratorNeedsUpdating.clear();
		cache.clear();
		Iterator it = imageCache.values().iterator();
		while (it.hasNext()) {
			Hashtable overlayTable = (Hashtable)it.next();
			Iterator it2 = overlayTable.values().iterator();
			while (it2.hasNext()) {
				((Image)it2.next()).dispose();
			}
		}
		imageCache = new Hashtable();
		theDecorator = null;
	}
}