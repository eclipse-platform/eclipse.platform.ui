package org.eclipse.team.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.IResourceStateChangeListener;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.ui.ITeamDecorator;
import org.eclipse.team.ui.TeamUIPlugin;

/**
 * TeamResourceDecorator is a general decorator for team items in a view.
 */
public class TeamResourceDecorator extends LabelProvider implements ILabelDecorator, IResourceChangeListener, IResourceStateChangeListener {
	// Constants
	protected final static String TAG_DECORATOR = "decorator";
	protected final static String ATT_CLASS = "class";
	protected final static String ATT_NATUREID = "natureId";

	// The shell this decorator is in
	private Shell shell;
	
	// The decorator registry.
	// key = natureId
	// value = decorator instance
	Map decorators = new HashMap();
	
	public TeamResourceDecorator() {
		initializeDecorators();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_AUTO_BUILD);
		TeamPlugin.getManager().addResourceStateChangeListener(this);
	}
	
	/**
	 * Initialize the decorators table
	 */
	void initializeDecorators() {
		IPluginRegistry registry = Platform.getPluginRegistry();
		IExtensionPoint point = registry.getExtensionPoint(TeamUIPlugin.ID, UIConstants.PT_DECORATORS);
		if (point != null) {
			IExtension[] extensions = point.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] elements = extensions[i].getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
					IConfigurationElement element = elements[j];
					if (element.getName().equals(TAG_DECORATOR)) {						
						try {
							String natureId = element.getAttribute(ATT_NATUREID);
							ITeamDecorator decorator = (ITeamDecorator)TeamUIPlugin.createExtension(element, ATT_CLASS);
							decorators.put(natureId, decorator);
						} catch(ClassCastException e) {
							TeamUIPlugin.log(new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, Policy.bind("TeamResourceDecorator.badClassType"), e));
						} catch(CoreException e) {
							TeamUIPlugin.log(new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, Policy.bind("TeamResourceDecorator.coreException"), e));
						}
					}
				}
			}
		}
	}

	/**
	 * Generates label change events for the entire subtree rooted
	 * at the given resource.
	 * 
	 * @param parent  the parent resource to recursively visit
	 * @param events  the list to add decorator events to
	 */
	private void createEventsForSubtree(IResource parent, final ArrayList events) {
		try {
			parent.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) {
					events.add(createLabelEvent(resource));
					return true;
				}
			});
		} catch (CoreException e) {
			//this is never thrown in the above visitor
		}
	}

	/**
	 * Returns a new change event to be fired for updates to the given resource.
	 */
	protected LabelProviderChangedEvent createLabelEvent(IResource resource) {
		return new LabelProviderChangedEvent(this, resource);
	}

	/*
	 * Method declared on ILabelDecorator.
	 */
	public Image decorateImage(Image image, Object object) {
		IResource resource = getResource(object);
		if (resource == null) {
			return image;
		}

		ITeamDecorator decorator = getDecorator(resource);
		if(decorator==null) { 
			return image;
		}

		ImageDescriptor[][] overlays = decorator.getImage(resource);
		
		if(overlays==null) {
			return image;
		}
			
		ImageDescriptor overlayImage = new OverlayIcon(image.getImageData(), overlays, new Point(16, 16));
		return overlayImage.createImage();	
	}
	
	/**
	 * Return a decorator for the given resource
	 * 
	 * @param resource  the resource to return a decorator for
	 * @return a decorator for the given resource
	 */
	protected ITeamDecorator getDecorator(IResource resource) {
		try {
			String[] natureIds = resource.getProject().getDescription().getNatureIds();
			for (int i = 0; i < natureIds.length; i++) {
				if(decorators.containsKey(natureIds[i])) {
					return (ITeamDecorator)decorators.get(natureIds[i]);
				}			
			}
		} catch(CoreException e) {
		}
		return null;
	}
	
	/*
	 * Method declared on ILabelDecorator.
	 */
	public String decorateText(String text, Object o) {
		IResource resource = getResource(o);
		if (resource == null) {
			//don't annotate things we don't know about
			return text;
		}

		ITeamDecorator decorator = getDecorator(resource);
		if(decorator==null) { 
			return text;
		}
		
		return decorator.getText(text, resource);
	}

	/*
	 * Method declared on IBaseLabelProvider.
	 */
	public void dispose() {
		super.dispose();
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
	 * Returns true if the given object is an outgoing change, and
	 * false otherwise.
	 * 
	 * @param object  the object to examine for outgoing changes
	 * @return whether the object has an outgoing change
	 */
	private boolean isOutgoing(Object object) {
		return false;
	}
	
 	/**
	 * Process a resource delta.  Returns all label provider changed
	 * events that were generated by this delta.
	 * 
	 * @param delta  the delta to process
	 * @return all label provider changed events
	 */
	protected LabelProviderChangedEvent[] processDelta(IResourceDelta delta) {
		final ArrayList events = new ArrayList();
		try {
			delta.accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource resource = delta.getResource();
					// skip workspace root
					if (resource.getType() == IResource.ROOT) {
						return true;
					}
					// don't care about deletions
					if (delta.getKind() == IResourceDelta.REMOVED) {
						return false;
					}
					// if project association info has changed, need to update whole tree
					if (resource.getType() == IResource.PROJECT && ((delta.getFlags() & IResourceDelta.DESCRIPTION) != 0)) {
						createEventsForSubtree(resource, events);
						return false;
					}
					// ignore subtrees that aren't associated with a provider
					ITeamProvider p = TeamPlugin.getManager().getProvider(resource);		
					if (p == null) {
						return false;
					}
					// ignore subtrees that are ignored by team
					if (!p.hasRemote(resource)) {
						return false;
					}
					// chances are the team outgoing bit needs to be updated
					// if any child has changed.
					events.add(createLabelEvent(delta.getResource()));
					return true;
				}
			});
		} catch (CoreException e) {
			TeamUIPlugin.log(e.getStatus());
		}
		// convert event list to array
		LabelProviderChangedEvent[] result = new LabelProviderChangedEvent[events.size()];
		events.toArray(result);
		return result;
	}
	
	/**
	 * Post the label events to the UI thread
	 * 
	 * @param events  the events to post
	 */
	protected void postLabelEvents(final LabelProviderChangedEvent[] events) {
		// now post the change events to the UI thread
		fireLabelUpdates(events);		
	} 
	
	/**
	 * Trigger label updates for the given events
	 * 
	 * @param events  the events to trigger label updates for
	 */
	void fireLabelUpdates(final LabelProviderChangedEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			fireLabelProviderChanged(events[i]);
		}
	} 	 
	
	/*
	 * Method declared on IResourceChangedListener.
	 */	 
	public void resourceChanged(IResourceChangeEvent event) {
		//first collect the label change events
		final LabelProviderChangedEvent[] events = processDelta(event.getDelta());		
		postLabelEvents(events);
	}

	/**
	 * Create a label event for the parents of the resource
	 * 
	 * @param resource  the resoure to create label events for
	 * @return a list of events
	 */
	public List createLabelEventForParents(IResource resource) {
		List events = new ArrayList(5);
		IResource current = resource;
		while(current.getType() != IResource.ROOT) {
			events.add(createLabelEvent(current));
			current = current.getParent();
		}
		return events;			
	}
		
	/*
	 * Method declared on IResourceStateChangeListener.
	 */
	public void resourceStateChanged(IResource[] changedResources) {
		List events = new ArrayList(10);
		for(int i=0;i<changedResources.length;i++) {
			events.addAll(createLabelEventForParents(changedResources[i]));
		}
		postLabelEvents((LabelProviderChangedEvent[])events.toArray(new LabelProviderChangedEvent[events.size()]));
	}
}