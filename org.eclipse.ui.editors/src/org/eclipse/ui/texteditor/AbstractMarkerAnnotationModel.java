/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.Bundle;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceStatus;

import org.eclipse.core.filebuffers.IPersistableAnnotationModel;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationMap;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.editors.text.EditorsUI;


/**
 * Abstract  implementation of a marker-based annotation model.
 * <p>
 * Markers are provided by an underlying source (a subclass responsibility).
 * Markers whose textual range gets deleted during text editing are removed
 * from the model on save. The {@link #updateMarkers(IDocument)} method can be used
 * to force the model to update the source's markers with any changes to their
 * locations due to edits. Clients can register a {@link org.eclipse.ui.texteditor.IMarkerUpdater}
 * objects in order to define the process of marker updating. Registration can be done
 * using the <code>"org.eclipse.ui.markerUpdaters"</code> extension point.
 * </p>
 * <p>
 * Subclasses must implement the following methods:
 * <ul>
 *   <li><code>retrieveMarkers</code></li>
 *   <li><code>isAcceptable</code></li>
 *   <li><code>deleteMarkers</code></li>
 *   <li><code>listenToMarkerChanges</code></li>
 * </ul>
 * </p>
 */
public abstract class AbstractMarkerAnnotationModel extends AnnotationModel implements IPersistableAnnotationModel {

	/** List of annotations whose text range became invalid because of document changes */
	private List fDeletedAnnotations= new ArrayList(2);
	/** List of registered and instantiated marker updaters */
	private List fInstantiatedMarkerUpdaters= null;
	/** List of registered but not yet instantiated marker updaters */
	private List fMarkerUpdaterSpecifications= null;


	/**
	 * Retrieves all markers from this model.
	 * <p>
	 * Subclasses must implement this method.</p>
	 *
	 * @return the list of markers
	 * @throws CoreException if there is a problem getting the markers
	 */
	protected abstract IMarker[] retrieveMarkers() throws CoreException;

	/**
	 * Deletes the given markers from this model.
	 * <p>
	 * Subclasses must implement this method.</p>
	 *
	 * @param markers the array of markers
	 * @throws CoreException if there are problems deleting the markers
	 */
	protected abstract void deleteMarkers(IMarker[] markers) throws CoreException;

	/**
	 * Tells the model whether it should listen for marker changes.
	 * <p>
	 * Subclasses must implement this method.</p>
	 *
	 * @param listen <code>true</code> if this model should listen, and
	 *   <code>false</code> otherwise
	 */
	protected abstract void listenToMarkerChanges(boolean listen);

	/**
	 * Determines whether the marker is acceptable as an addition to this model.
	 * If the marker, say, represents an aspect or range of no interest to this
	 * model, the marker is rejected.
	 * <p>
	 * Subclasses must implement this method.</p>
	 *
	 * @param marker the marker
	 * @return <code>true</code> if the marker is acceptable
	 */
	protected abstract boolean isAcceptable(IMarker marker);

	/**
	 * Creates a new annotation model. The annotation model does not manage any
	 * annotations and is not connected to any document.
	 */
	protected AbstractMarkerAnnotationModel() {
	}

	/**
	 * Adds the given marker updater to this annotation model.
	 * It is the client's responsibility to ensure the consistency
	 * of the set of registered marker updaters.
	 *
	 * @param markerUpdater the marker updater to be added
	 */
	protected void addMarkerUpdater(IMarkerUpdater markerUpdater) {
		if (!fInstantiatedMarkerUpdaters.contains(markerUpdater))
			fInstantiatedMarkerUpdaters.add(markerUpdater);
	}

	/**
	 * Removes the given marker updater from this annotation model.
	 *
	 * @param markerUpdater the marker updater to be removed
	 */
	protected void removeMarkerUpdater(IMarkerUpdater markerUpdater) {
		fInstantiatedMarkerUpdaters.remove(markerUpdater);
	}

	/**
	 * Creates a new annotation for the given marker.
	 * <p>
	 * Subclasses may override.</p>
	 *
	 * @param marker the marker
	 * @return the new marker annotation
	 */
	protected MarkerAnnotation createMarkerAnnotation(IMarker marker) {
		return new MarkerAnnotation(marker);
	}

	/**
	 * Handles an unanticipated <code>CoreException</code> in
	 * a standard manner.
	 *
	 * @param exception the exception
	 * @param message a message to aid debugging
	 */
	protected void handleCoreException(CoreException exception, String message) {

		Bundle bundle = Platform.getBundle(PlatformUI.PLUGIN_ID);
		ILog log= Platform.getLog(bundle);
		if (message != null)
			log.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, message, exception));
		else
			log.log(exception.getStatus());
	}

	/**
	 * Creates and returns the character position of the given marker based
	 * on its attributes.
	 * <p>
	 * Subclasses may override.</p>
	 *
	 * @param marker the marker
	 * @return the new position or <code>null</code> if the marker attributes do not specify a valid position
	 */
	protected Position createPositionFromMarker(IMarker marker) {

		int start= MarkerUtilities.getCharStart(marker);
		int end= MarkerUtilities.getCharEnd(marker);

		if (start > end) {
			end= start + end;
			start= end - start;
			end= end - start;
		}

		if (start == -1 && end == -1) {
			// marker line number is 1-based
			int line= MarkerUtilities.getLineNumber(marker);
			if (line > 0 && fDocument != null) {
				try {
					start= fDocument.getLineOffset(line - 1);
					end= start;
				} catch (BadLocationException x) {
				}
			}
		}

		if (start > -1 && end > -1)
			return new Position(start, end - start);

		return null;
	}

	/**
	 * Creates an annotation for the given marker and adds it to this model.
	 * Does nothing if the marker is not acceptable to this model.
	 *
	 * @param marker the marker
	 * @see #isAcceptable(IMarker)
	 */
	protected final void addMarkerAnnotation(IMarker marker) {

		if (isAcceptable(marker)) {
			Position p= createPositionFromMarker(marker);
			if (p != null)
				try {
					MarkerAnnotation annotation= createMarkerAnnotation(marker);
					if (annotation != null)
						addAnnotation(annotation, p, false);
				} catch (BadLocationException e) {
					// ignore invalid position
				}
		}
	}

	/**
	 * Connects to the source of markers as marker change listener.
	 * @see AnnotationModel#connected()
	 */
	protected void connected() {

		listenToMarkerChanges(true);

		try {
			catchupWithMarkers();
		} catch (CoreException x) {
			if (x.getStatus().getCode() != IResourceStatus.RESOURCE_NOT_FOUND)
				handleCoreException(x, TextEditorMessages.AbstractMarkerAnnotationModel_connected);
		}

		fireModelChanged();
	}

	/**
	 * Installs all marker updaters for this marker annotation model.
	 */
	private void installMarkerUpdaters() {

		// initialize lists - indicates that the initialization happened
		fMarkerUpdaterSpecifications= new ArrayList(2);
		fInstantiatedMarkerUpdaters= new ArrayList(2);

		// populate list
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(EditorsUI.PLUGIN_ID, "markerUpdaters"); //$NON-NLS-1$
		if (extensionPoint != null) {
			IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
			for (int i= 0; i < elements.length; i++)
				fMarkerUpdaterSpecifications.add(elements[i]);
		}
	}

	/**
	 * Uninstalls all marker updaters.
	 */
	private void uninstallMarkerUpdaters() {
		if (fInstantiatedMarkerUpdaters != null) {
			fInstantiatedMarkerUpdaters.clear();
			fInstantiatedMarkerUpdaters= null;
		}

		if (fMarkerUpdaterSpecifications != null) {
			fMarkerUpdaterSpecifications.clear();
			fMarkerUpdaterSpecifications= null;
		}
	}

	/**
	 * Removes the marker change listener.
	 * @see AnnotationModel#disconnected()
	 */
	protected void disconnected() {
		listenToMarkerChanges(false);
		uninstallMarkerUpdaters();
	}

	/**
	 * Returns the position known to this annotation model for the given marker.
	 *
	 * @param marker the marker
	 * @return the position, or <code>null</code> if none
	 */
	public Position getMarkerPosition(IMarker marker) {
		MarkerAnnotation a= getMarkerAnnotation(marker);
		if (a != null) {
			return (Position) getAnnotationMap().get(a);
		}
		return null;
	}

	/**
	 * Updates the annotation corresponding to the given marker which has changed
	 * in some way.
	 * <p>
	 * Subclasses may override.</p>
	 *
	 * @param marker the marker
	 */
	protected void modifyMarkerAnnotation(IMarker marker) {
		MarkerAnnotation a= getMarkerAnnotation(marker);
		if (a != null) {
			Position p= createPositionFromMarker(marker);
			if (p != null) {
				a.update();
				modifyAnnotationPosition(a, p, false);
			}
		} else
			addMarkerAnnotation(marker);
	}

	/*
	 * @see AnnotationModel#removeAnnotations(List, boolean, boolean)
	 */
	protected void removeAnnotations(List annotations, boolean fireModelChanged, boolean modelInitiated) {
		if (annotations != null && annotations.size() > 0) {

			List markerAnnotations= new ArrayList();
			for (Iterator e= annotations.iterator(); e.hasNext();) {
				Annotation a= (Annotation) e.next();
				if (a instanceof MarkerAnnotation)
					markerAnnotations.add(a);

				// remove annotations from annotation model
				removeAnnotation(a, false);
			}

			if (markerAnnotations.size() > 0) {

				if (modelInitiated) {
					// if model initiated also remove it from the marker manager

					listenToMarkerChanges(false);
					try {

						IMarker[] m= new IMarker[markerAnnotations.size()];
						for (int i= 0; i < m.length; i++) {
							MarkerAnnotation ma = (MarkerAnnotation) markerAnnotations.get(i);
							m[i]= ma.getMarker();
						}
						deleteMarkers(m);

					} catch (CoreException x) {
						handleCoreException(x, TextEditorMessages.AbstractMarkerAnnotationModel_removeAnnotations);
					}
					listenToMarkerChanges(true);

				} else {
					// remember deleted annotations in order to remove their markers later on
					fDeletedAnnotations.addAll(markerAnnotations);
				}
			}

			if (fireModelChanged)
				fireModelChanged();
		}
	}

	/**
	 * Removes the annotation corresponding to the given marker. Does nothing
	 * if there is no annotation for this marker.
	 *
	 * @param marker the marker
	 */
	protected final void removeMarkerAnnotation(IMarker marker) {
		MarkerAnnotation a= getMarkerAnnotation(marker);
		if (a != null) {
			removeAnnotation(a, false);
		}
	}

	/**
	 * Re-populates this model with annotations for all markers retrieved
	 * from the maker source via <code>retrieveMarkers</code>.
	 *
	 * @throws CoreException if there is a problem getting the markers
	 */
	private void catchupWithMarkers() throws CoreException {

		for (Iterator e=getAnnotationIterator(false); e.hasNext();) {
			Annotation a= (Annotation) e.next();
			if (a instanceof MarkerAnnotation)
				removeAnnotation(a, false);
		}

		IMarker[] markers= retrieveMarkers();
		if (markers != null) {
			for (int i= 0; i < markers.length; i++)
				addMarkerAnnotation(markers[i]);
		}
	}

	/**
	 * Returns this model's annotation for the given marker.
	 *
	 * @param marker the marker
	 * @return the annotation, or <code>null</code> if none
	 */
	public final MarkerAnnotation getMarkerAnnotation(IMarker marker) {
		Iterator e= getAnnotationIterator(false);
		while (e.hasNext()) {
			Object o= e.next();
			if (o instanceof MarkerAnnotation) {
				MarkerAnnotation a= (MarkerAnnotation) o;
				if (marker.equals(a.getMarker())) {
					return a;
				}
			}
		}
		return null;
	}

	/**
	 * Creates a marker updater as specified in the given configuration element.
	 *
	 * @param element the configuration element
	 * @return the created marker updater or <code>null</code> if none could be created
	 */
	private IMarkerUpdater createMarkerUpdater(IConfigurationElement element) {
		try {
			return (IMarkerUpdater) element.createExecutableExtension("class"); //$NON-NLS-1$
		} catch (CoreException x) {
			handleCoreException(x, TextEditorMessages.AbstractMarkerAnnotationModel_createMarkerUpdater);
		}

		return null;
	}

	/**
	 * Checks whether a marker updater is registered for the type of the
	 * given marker but not yet instantiated. If so, the method instantiates
	 * the marker updater and registers it with this model.
	 *
	 * @param marker the marker for which to look for an updater
	 * @since 2.0
	 */
	private void checkMarkerUpdaters(IMarker marker) {
		List toBeDeleted= new ArrayList();
		for (int i= 0; i < fMarkerUpdaterSpecifications.size(); i++) {
			IConfigurationElement spec= (IConfigurationElement) fMarkerUpdaterSpecifications.get(i);
			String markerType= spec.getAttribute("markerType"); //$NON-NLS-1$
			if (markerType == null || MarkerUtilities.isMarkerType(marker, markerType)) {
				toBeDeleted.add(spec);
				IMarkerUpdater updater= createMarkerUpdater(spec);
				if (updater != null)
					addMarkerUpdater(updater);
			}
		}

		for (int i= 0; i < toBeDeleted.size(); i++)
			fMarkerUpdaterSpecifications.remove(toBeDeleted.get(i));
	}

	/**
	 * Updates the given marker according to the given position in the given
	 * document. If the given position is <code>null</code>, the marker is
	 * assumed to carry the correct positional information. If it is detected
	 * that the marker is invalid and should thus be deleted, this method
	 * returns <code>false</code>.
	 * <p>
	 * <strong>Note:</strong> This implementation queries the registered
	 * {@linkplain IMarkerUpdater}s. If any of these updaters returns
	 * <code>false</code> this method also returns <code>false</code>.
	 * </p>
	 *
	 * @param marker the marker to be updated
	 * @param document the document into which the given position points
	 * @param position the current position of the marker inside the given document
	 * @return <code>false</code> if the marker is invalid
	 * @throws CoreException if there is a problem updating the marker
	 * @since 2.0
	 * @deprecated use <code>updateMarker(IDocument, IMarker, Position)</code> instead. This method will be changed to protected.
	 */
	public boolean updateMarker(IMarker marker, IDocument document, Position position) throws CoreException {

		if (fMarkerUpdaterSpecifications == null)
			installMarkerUpdaters();

		if (!fMarkerUpdaterSpecifications.isEmpty())
			checkMarkerUpdaters(marker);

		boolean isOK= true;

		for (int i= 0; i < fInstantiatedMarkerUpdaters.size();  i++) {
			IMarkerUpdater updater= (IMarkerUpdater) fInstantiatedMarkerUpdaters.get(i);
			String markerType= updater.getMarkerType();
			if (markerType == null || MarkerUtilities.isMarkerType(marker, markerType)) {

				if (position == null) {
					/* compatibility code */
					position= createPositionFromMarker(marker);
				}

				isOK= (isOK && updater.updateMarker(marker, document, position));
			}
		}

		return isOK;
	}

	/**
	 * Updates the given marker according to the given position in the given
	 * document. If the given position is <code>null</code>, the marker is
	 * assumed to carry the correct positional information. If it is detected
	 * that the marker is invalid and should thus be deleted, this method
	 * returns <code>false</code>.
	 *
	 * @param marker the marker to be updated
	 * @param document the document into which the given position points
	 * @param position the current position of the marker inside the given document
	 * @return <code>false</code> if the marker is invalid
	 * @throws CoreException if there is a problem updating the marker
	 * @since 3.0
	 */
	public boolean updateMarker(IDocument document, IMarker marker, Position position) throws CoreException {
		listenToMarkerChanges(false);
		try {
			return updateMarker(marker, document, position);
		} finally {
			listenToMarkerChanges(true);
		}
	}

	/**
	 * Updates the markers managed by this annotation model by calling
	 * all registered marker updaters (<code>IMarkerUpdater</code>).
	 *
	 * @param document the document to which this model is currently connected
	 * @throws CoreException if there is a problem updating the markers
	 */
	public void updateMarkers(IDocument document) throws CoreException {

		Assert.isTrue(fDocument == document);

		IAnnotationMap annotationMap= getAnnotationMap();

		if (annotationMap.size() == 0 && fDeletedAnnotations.size() == 0)
			return;

		if (fMarkerUpdaterSpecifications == null)
			installMarkerUpdaters();

		listenToMarkerChanges(false);

		try {

			// update all markers with the positions known by the annotation model
			for (Iterator e= getAnnotationIterator(false); e.hasNext();) {
				Object o= e.next();
				if (o instanceof MarkerAnnotation) {
					MarkerAnnotation a= (MarkerAnnotation) o;
					IMarker marker= a.getMarker();
					Position position= (Position) annotationMap.get(a);
					if ( !updateMarker(marker, document, position)) {
						if ( !fDeletedAnnotations.contains(a))
							fDeletedAnnotations.add(a);
					}
				}
			}

			if (!fDeletedAnnotations.isEmpty()) {
				removeAnnotations(fDeletedAnnotations, true, true);
				fDeletedAnnotations.clear();
			}

		} finally {

			listenToMarkerChanges(true);

		}
	}

	/**
	 * Resets all the markers to their original state.
	 */
	public void resetMarkers() {

		// re-initializes the positions from the markers
		for (Iterator e= getAnnotationIterator(false); e.hasNext();) {
			Object o= e.next();
			if (o instanceof MarkerAnnotation) {
				MarkerAnnotation a= (MarkerAnnotation) o;
				Position p= createPositionFromMarker(a.getMarker());
				if (p != null) {
					removeAnnotation(a, false);
					try {
						addAnnotation(a, p, false);
					} catch (BadLocationException e1) {
						// ignore invalid position
					}
				}
			}
		}

		// add the markers of deleted positions back to the annotation model
		for (Iterator e= fDeletedAnnotations.iterator(); e.hasNext();) {
			Object o= e.next();
			if (o instanceof MarkerAnnotation) {
				MarkerAnnotation a= (MarkerAnnotation) o;
				Position p= createPositionFromMarker(a.getMarker());
				if (p != null)
					try {
						addAnnotation(a, p, false);
					} catch (BadLocationException e1) {
						// ignore invalid position
					}
			}
		}
		fDeletedAnnotations.clear();

		// fire annotation model changed
		fireModelChanged();
	}

	/*
	 * @see org.eclipse.jface.text.source.IPersistableAnnotationModel#commit(org.eclipse.jface.text.IDocument)
	 */
	public void commit(IDocument document) throws CoreException {
		updateMarkers(document);
	}

	/*
	 * @see org.eclipse.jface.text.source.IPersistableAnnotationModel#revert(org.eclipse.jface.text.IDocument)
	 */
	public void revert(IDocument document) {
		resetMarkers();
	}

	/*
	 * @see org.eclipse.jface.text.source.IPersistableAnnotationModel#reinitialize(org.eclipse.jface.text.IDocument)
	 */
	public void reinitialize(IDocument document) {
		resetMarkers();
	}
}
