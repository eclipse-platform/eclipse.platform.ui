package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.ui.internal.DecoratorDefinition;
import org.eclipse.ui.internal.DecoratorManager;
import org.eclipse.ui.internal.WorkbenchPlugin;

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
public class CVSDecorator extends LabelProvider implements ILabelDecorator, IResourceChangeListener, IResourceStateChangeListener, IDecorationNotifier {
	
	// Resources that need an icon and text computed for display to the user
	private List decoratorNeedsUpdating = new ArrayList();

	// When decorations are computed they are added to this cache via decorated() method
	private Map cache = Collections.synchronizedMap(new HashMap());

	// Updater thread, computes decoration labels and images
	private Thread decoratorUpdateThread;

	private boolean shutdown = false;
	
	private OverlayIconCache iconCache = new OverlayIconCache();
	
	// Keep track of deconfigured projects
	private Set deconfiguredProjects = new HashSet();
	
	private static class DecoratorOverlayIcon extends OverlayIcon {
		private static final int HEIGHT = 16;
		private static final int WIDTH = 16;
		public DecoratorOverlayIcon(Image base, ImageDescriptor[] overlays) {
			super(base, overlays, new Point(WIDTH, HEIGHT));
		}
		protected void drawOverlays(ImageDescriptor[] overlays) {
			for (int i = overlays.length - 1; i >= 0; --i) {
				ImageData imageData = overlays[i].getImageData();
				drawImage(imageData, 0, 0);
			}
		}
	}
	
	public CVSDecorator() {
		// thread that calculates the decoration for a resource
		decoratorUpdateThread = new Thread(new CVSDecorationRunnable(this), "CVS"); //$NON-NLS-1$
		decoratorUpdateThread.start();
		CVSProviderPlugin.addResourceStateChangeListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.PRE_AUTO_BUILD);
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
			if (overlays != null) {
				return iconCache.getImageFor(new DecoratorOverlayIcon(image,
					(ImageDescriptor[]) overlays.toArray(new ImageDescriptor[overlays.size()])));
			}
		} else {
			addResourcesToBeDecorated(new IResource[] { resource });
		}
		return image;
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
	 * @see IDecorationNotifier#decorated(IResource[], CVSDecoration[])
	 */
	public synchronized void decorated(IResource[] resources, CVSDecoration[] decorations) {
		List events = new ArrayList();
		if(!shutdown) {
			for (int i = 0; i < resources.length; i++) {
				IResource resource= resources[i];
				if(resource.exists()) {
					cache.put(resource, decorations[i]);
					events.add(new LabelProviderChangedEvent(this, resource));
				}
			}
			postLabelEvents((LabelProviderChangedEvent[]) events.toArray(new LabelProviderChangedEvent[events.size()]));
		}
	}

	/*
	 * @see IDecorationNotifier#remaining()
	 */
	public int remaining() {
		return decoratorNeedsUpdating.size();
	}
	/*
	 * Handle resource changes and project description changes
	 * 
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			final List changedResources = new ArrayList();
			event.getDelta().accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource resource = delta.getResource();
					
					if(resource.getType()==IResource.ROOT) {
						// continue with the delta
						return true;
					}
					
					if (resource.getType() == IResource.PROJECT) {
						// If there is no CVS nature, don't continue
						if (RepositoryProvider.getProvider((IProject)resource, CVSProviderPlugin.getTypeId()) == null) {
							// deconfigure if appropriate (see CVSDecorator#projectDeconfigured(IProject))
							if (deconfiguredProjects.contains(resource)) {
								refresh((IProject)resource);
								deconfiguredProjects.remove(resource);
							}
							return false;
						}
					}
					
					switch (delta.getKind()) {
						case IResourceDelta.REMOVED:
							// remove the cached decoration for any removed resource
							cache.remove(resource);
							break;
						case IResourceDelta.CHANGED:
							// for changed resources we have to update the decoration
							changedResources.add(resource);	
					}
					
					return true;
				}
			});
			resourceStateChanged((IResource[])changedResources.toArray(new IResource[changedResources.size()]));
			changedResources.clear();	
		} catch (CoreException e) {
			CVSProviderPlugin.log(e.getStatus());
		}
	}
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
			resources.addAll(computeParents(resource));  //todo: should only add parents if deep preference on: bug #11400
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

	/*
	 * Return the ILabelDecorator for our CVS decorator, null if its not active.
	 */	 
	private static ILabelDecorator findCVSDecorator() {
		DecoratorManager decoratorManager = WorkbenchPlugin.getDefault().getDecoratorManager();
		DecoratorDefinition[] definitions = decoratorManager.getDecoratorDefinitions();
		for (int i = 0; i < definitions.length; i++) {
			DecoratorDefinition decoratorDefinition = definitions[i];
			if(decoratorDefinition.getId().equals(CVSUIPlugin.DECORATOR_ID))
				try {
					return decoratorDefinition.getDecorator();
				} catch (CoreException e) {
					CVSUIPlugin.log(e.getStatus()); //todo: need to inform user too
					return null;
				}
		}
		return null;
	}
			
	private void clearCache() {
		cache.clear();
	}
	
	/*
	 * Blanket refresh the displaying of our decorator.
	 */
	 
	public static void refresh() {
		CVSDecorator cvsDecorator = (CVSDecorator) findCVSDecorator();
		if(cvsDecorator == null)
			return;	//nothing to do, our decorator isn't active
		cvsDecorator.clearCache();  //clear the cache of previous decorations so we can compute them anew
		
		WorkbenchPlugin.getDefault().getDecoratorManager().reset();
	}

	public void refresh(IProject project) {
		final List resources = new ArrayList();
		try {
			project.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) {
					resources.add(resource);
					return true;
				}
			});
			resourceStateChanged((IResource[]) resources.toArray(new IResource[resources.size()]));	
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
		RepositoryProvider p = RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId());
		if (p == null) {
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
		
	/*
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		
		// terminate decoration thread
		shutdown();
		
		// unregister change listeners
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		CVSProviderPlugin.removeResourceStateChangeListener(this);
		
		// dispose of images created as overlays
		decoratorNeedsUpdating.clear();
		clearCache();
		iconCache.disposeAll();
	}
	/**
	 * @see IResourceStateChangeListener#projectConfigured(IProject)
	 */
	public void projectConfigured(IProject project) {
		refresh(project);
	}

	/**
	 * @see IResourceStateChangeListener#projectDeconfigured(IProject)
	 */
	public void projectDeconfigured(IProject project) {
		// Unfortunately, the nature is still associated with the project at this point.
		// Therefore, we will remember that the project has been deconfigured and we will 
		// refresh the decorators in the resource delta listener
		deconfiguredProjects.add(project);
	}
	
	public static boolean isDirty(ICVSFile cvsFile) {
		try {
			// file is dirty or file has been merged by an update
			if(!cvsFile.isIgnored()) {
				return cvsFile.isModified();
			} else {
				return false;
			} 
		} catch (CVSException e) {
			//if we get an error report it to the log but assume dirty
			CVSUIPlugin.log(e.getStatus());
			return true;
		}
	}

	public static boolean isDirty(IFile file) {
		return isDirty(CVSWorkspaceRoot.getCVSFileFor(file));
	}

	public static boolean isDirty(IResource resource) {
		if(resource.getType() == IResource.FILE) {
			return isDirty((IFile) resource);
		}
		
		final CoreException DECORATOR_EXCEPTION = new CoreException(new Status(IStatus.OK, "id", 1, "", null)); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			resource.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {

					// a project can't be dirty, continue with its children
					if (resource.getType() == IResource.PROJECT) {
						return true;
					}
					
					// if the resource does not exist in the workbench or on the file system, stop searching.
					if(!resource.exists()) {
						return false;
					}

					ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);

					if (!cvsResource.isManaged()) {
						if (cvsResource.isIgnored()) {
							return false;
						} else {
							// new resource, show as dirty
							throw DECORATOR_EXCEPTION;
						}
					}
					if (!cvsResource.isFolder()) {
						if(isDirty((ICVSFile) cvsResource)) {
							throw DECORATOR_EXCEPTION;
						}
					}
					// no change -- keep looking in children
					return true;
				}
			}, IResource.DEPTH_INFINITE, true);
		} catch (CoreException e) {
			//if our exception was caught, we know there's a dirty child
			return e == DECORATOR_EXCEPTION;
		}
		return false;
	}
}