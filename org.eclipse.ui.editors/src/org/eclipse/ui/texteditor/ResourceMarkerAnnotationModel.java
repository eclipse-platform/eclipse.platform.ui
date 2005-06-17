/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.Assert;


/**
 * A marker annotation model whose underlying source of markers is
 * a resource in the workspace.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.</p>
 */
public class ResourceMarkerAnnotationModel extends AbstractMarkerAnnotationModel {


	/**
	 * Internal resource change listener.
	 */
	class ResourceChangeListener implements IResourceChangeListener {
		/*
		 * @see IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
		 */
		public void resourceChanged(IResourceChangeEvent e) {
			IResourceDelta delta= e.getDelta();
			if (delta != null && fResource != null) {
				IResourceDelta child= delta.findMember(fResource.getFullPath());
				if (child != null)
					update(child.getMarkerDeltas());
			}
		}
	}

	/** The workspace. */
	private IWorkspace fWorkspace;
	/** The resource. */
	private IResource fResource;
	/** The resource change listener. */
	private IResourceChangeListener fResourceChangeListener= new ResourceChangeListener();


	/**
	 * Creates a marker annotation model with the given resource as the source
	 * of the markers.
	 *
	 * @param resource the resource
	 */
	public ResourceMarkerAnnotationModel(IResource resource) {
		Assert.isNotNull(resource);
		fResource= resource;
		fWorkspace= resource.getWorkspace();
	}

	/*
	 * @see AbstractMarkerAnnotationModel#isAcceptable(IMarker)
	 */
	protected boolean isAcceptable(IMarker marker) {
		return marker != null && fResource.equals(marker.getResource());
	}

	/**
	 * Updates this model to the given marker deltas.
	 *
	 * @param markerDeltas the array of marker deltas
	 */
	protected void update(IMarkerDelta[] markerDeltas) {

		if (markerDeltas.length ==  0)
			return;

		for (int i= 0; i < markerDeltas.length; i++) {
			IMarkerDelta delta= markerDeltas[i];
			switch (delta.getKind()) {
				case IResourceDelta.ADDED :
					addMarkerAnnotation(delta.getMarker());
					break;
				case IResourceDelta.REMOVED :
					removeMarkerAnnotation(delta.getMarker());
					break;
				case IResourceDelta.CHANGED :
					modifyMarkerAnnotation(delta.getMarker());
					break;
			}
		}

		fireModelChanged();
	}

	/*
	 * @see AbstractMarkerAnnotationModel#listenToMarkerChanges(boolean)
	 */
	protected void listenToMarkerChanges(boolean listen) {
		if (listen)
			fWorkspace.addResourceChangeListener(fResourceChangeListener);
		else
			fWorkspace.removeResourceChangeListener(fResourceChangeListener);
	}

	/*
	 * @see AbstractMarkerAnnotationModel#deleteMarkers(IMarker[])
	 */
	protected void deleteMarkers(final IMarker[] markers) throws CoreException {
		fWorkspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i= 0; i < markers.length; ++i) {
					markers[i].delete();
				}
			}
		}, null, IWorkspace.AVOID_UPDATE, null);
	}

	/*
	 * @see AbstractMarkerAnnotationModel#retrieveMarkers()
	 */
	protected IMarker[] retrieveMarkers() throws CoreException {
		return fResource.findMarkers(IMarker.MARKER, true, IResource.DEPTH_ZERO);
	}

	/**
	 * Returns the resource serving as the source of markers for this annotation model.
	 *
	 * @return the resource serving as the source of markers for this annotation model
	 * @since 2.0
	 */
	protected IResource getResource() {
		return fResource;
	}
}
