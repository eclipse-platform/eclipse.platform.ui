/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.pessimistic.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.examples.pessimistic.*;

/**
 * The <code>PessimisticDecorator</code> is a label provider
 * that decorates resources controlled by a <code>PessimisticFilesystemProvider</code>.
 */
public class PessimisticDecorator extends LabelProvider implements ILabelDecorator, IResourceStateListener {

	/**
	 * Constructor needed for extension
	 */
	public PessimisticDecorator() {
		PessimisticFilesystemProviderPlugin.getInstance().addProviderListener(this);
	}

	/*
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(String, Object)
	 */
	public String decorateText(String text, Object element) {
		IResource resource= getResource(element);
		if (resource == null)
			return text;
		PessimisticFilesystemProvider provider= getProvider(resource);
		if (provider == null) {
			return text;
		}
		if (provider.isControlled(resource)) {
			if (provider.isCheckedout(resource)) {
				return ">" + text;
			} else {
				return text;
			}
		} else {
			if (provider.isIgnored(resource)) {
				return "[ignored] " + text;
			} else {
				return "(not controlled) " + text;
			}
		}
	}

	/*
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(Image, Object)
	 */
	public Image decorateImage(Image image, Object element) {
		return image;
	}

	/*
	 * Convenience method to get the provider of a resource
	 */
	private PessimisticFilesystemProvider getProvider(IResource resource) {
		IProject project= resource.getProject();
		if (project != null) {
			return (PessimisticFilesystemProvider) RepositoryProvider.getProvider(project, PessimisticFilesystemProviderPlugin.NATURE_ID);
		}
		return null;
	}

	/*
	 * Convenience method to get a resource from an object
	 */
	private IResource getResource(Object object) {
		if (object instanceof IResource) {
			return (IResource) object;
		}
		if (object instanceof IAdaptable) {
			return (IResource) ((IAdaptable) object).getAdapter(IResource.class);
		}
		return null;
	}

	/*
	 * Fires label events
	 */
	private void postLabelEvents(final LabelProviderChangedEvent[] events) {
		if (events != null && events.length > 0) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					for (int i= 0; i < events.length; i++) {
						fireLabelProviderChanged(events[i]);
					}
				}
			});
		}
	}

	/*
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		PessimisticFilesystemProviderPlugin.getInstance().removeProviderListener(this);
		super.dispose();
	}

	/*
	 * @see org.eclipse.team.examples.pessimistic.IResourceStateListener#resourcesChanged(IResource[])
	 */
	public void stateChanged(IResource[] resources) {
		if (resources.length > 0) {
			LabelProviderChangedEvent[] events= new LabelProviderChangedEvent[resources.length];
			for (int i= 0; i < resources.length; i++) {
				events[i]= new LabelProviderChangedEvent(this, resources[i]);
			}
			postLabelEvents(events);
		}
	}

}