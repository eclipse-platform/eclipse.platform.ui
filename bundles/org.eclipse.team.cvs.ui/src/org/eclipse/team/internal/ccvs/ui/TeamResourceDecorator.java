package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.IResourceStateChangeListener;
import org.eclipse.team.core.TeamPlugin;

/**
 * TeamResourceDecorator is a general decorator for team items in a view.
 */
abstract public class TeamResourceDecorator extends LabelProvider implements ILabelDecorator, IResourceChangeListener, IResourceStateChangeListener {
	
	public TeamResourceDecorator() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_AUTO_BUILD);
		TeamPlugin.getManager().addResourceStateChangeListener(this);
	}
		
	/*
	 * Method declared on ILabelDecorator.
	 */
	abstract public Image decorateImage(Image image, Object o);	

	/*
	 * Method declared on ILabelDecorator.
	 */
	abstract public String decorateText(String text, Object o);	

	/*
	 * Method declared on IResourceChangedListener.
	 */	 
	abstract public void resourceChanged(IResourceChangeEvent event);

	/*
	 * Method declared on IResourceStateChangeListener.
	 */
	abstract public void resourceStateChanged(IResource[] changedResources);
	
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
	protected IResource getResource(Object object) {
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
	protected void postLabelEvents(final LabelProviderChangedEvent[] events) {
		// now post the change events to the UI thread
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
}