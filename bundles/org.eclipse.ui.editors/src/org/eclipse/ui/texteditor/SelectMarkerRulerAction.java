/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.views.markers.MarkerViewUtil;
import org.osgi.framework.Bundle;


/**
 * A ruler action which can select the textual range of a marker that has a visual representation in
 * a vertical ruler.
 * <p>
 * This class may be instantiated but is not intended for sub-classing.
 * </p>
 *
 * @since 2.0, allowed to be subclassed since 3.5
 */
public class SelectMarkerRulerAction extends ResourceAction implements IUpdate {

	/** The vertical ruler info of the action's editor. */
	private final IVerticalRulerInfo fRuler;
	/** The associated editor. */
	private final ITextEditor fTextEditor;
	/** The action's resource bundle. */
	private final ResourceBundle fBundle;
	/** The prefix for resource bundle lookups. */
	private final String fPrefix;

	/**
	 * Creates a new action for the given ruler and editor. The action configures
	 * its visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or  <code>null</code> if none
	 * @param editor the editor
	 * @param ruler the ruler
	 *
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 */
	public SelectMarkerRulerAction(ResourceBundle bundle, String prefix, ITextEditor editor, IVerticalRulerInfo ruler) {
		super(bundle, prefix);
		fRuler= ruler;
		fTextEditor= editor;

		fBundle= bundle;
		fPrefix= prefix;
	}

	/**
	 * Creates a new action for the given ruler and editor. The action configures
	 * its visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 * @param ruler the ruler
	 * @param editor the editor
	 * @deprecated As of 3.0, replaced by {@link #SelectMarkerRulerAction(ResourceBundle, String, ITextEditor, IVerticalRulerInfo)}
	 */
	@Deprecated
	public SelectMarkerRulerAction(ResourceBundle bundle, String prefix, IVerticalRuler ruler, ITextEditor editor) {
		this(bundle, prefix, editor, ruler);
	}

	@Override
	public void update() {
		setEnabled(hasMarkers());
	}

	@Override
	public void run() {

		IMarker marker= chooseMarker(getMarkers());
		if (marker == null) {
			return;
		}

		IWorkbenchPage page= fTextEditor.getSite().getPage();
		MarkerViewUtil.showMarker(page, marker, false);

		gotoMarker(marker);
	}

	private void gotoMarker(IMarker marker) {

		// Use the provided adapter if any
		IGotoMarker gotoMarkerAdapter= fTextEditor.getAdapter(IGotoMarker.class);
		if (gotoMarkerAdapter != null) {
			gotoMarkerAdapter.gotoMarker(marker);
			return;
		}

		int start= MarkerUtilities.getCharStart(marker);
		int end= MarkerUtilities.getCharEnd(marker);

		boolean selectLine= start < 0 || end < 0;

		IDocumentProvider documentProvider= fTextEditor.getDocumentProvider();
		IEditorInput editorInput= fTextEditor.getEditorInput();

		// look up the current range of the marker when the document has been edited
		IAnnotationModel model= documentProvider.getAnnotationModel(editorInput);
		if (model instanceof AbstractMarkerAnnotationModel markerModel) {

			Position pos= markerModel.getMarkerPosition(marker);
			if (pos != null && !pos.isDeleted()) {
				// use position instead of marker values
				start= pos.getOffset();
				end= pos.getOffset() + pos.getLength();
			}

			if (pos != null && pos.isDeleted()) {
				// do nothing if position has been deleted
				return;
			}
		}

		IDocument document= documentProvider.getDocument(editorInput);

		if (selectLine) {
			int line;
			try {
				if (start >= 0) {
					line= document.getLineOfOffset(start);
				} else {
					line= MarkerUtilities.getLineNumber(marker);
					// Marker line numbers are 1-based
					-- line;
				}
				end= start + document.getLineLength(line) - 1;
			} catch (BadLocationException e) {
				return;
			}
		}

		int length= document.getLength();
		if (end - 1 < length && start < length) {
			fTextEditor.selectAndReveal(start, end - start);
		}
	}


	/**
	 * Chooses the marker with the highest layer. If there are multiple
	 * markers at the found layer, the first marker is taken.
	 *
	 * @param markers the list of markers to choose from
	 * @return the chosen marker or <code>null</code> if none of the given markers has a marker annotation in the model
	 */
	protected final IMarker chooseMarker(List<? extends IMarker> markers) {

		AbstractMarkerAnnotationModel model= getAnnotationModel();
		IAnnotationAccessExtension access= getAnnotationAccessExtension();

		IMarker marker= null;
		int maxLayer= 0;

		Iterator<? extends IMarker> iter= markers.iterator();
		while (iter.hasNext()) {
			IMarker m= iter.next();
			Annotation a= model.getMarkerAnnotation(m);
			if (a != null) {
				if (access == null) {
					marker= m;
					break;
				}
				int l= access.getLayer(a);
				if (l == maxLayer) {
					if (marker == null) {
						marker= m;
					}
				} else if (l > maxLayer) {
					maxLayer= l;
					marker= m;
				}
			}
		}

		return marker;
	}

	/**
	 * Returns the annotation access extension.
	 *
	 * @return the annotation access extension or <code>null</code> if
	 * 			this action's editor has no such extension
	 * @since 3.0
	 */
	protected final IAnnotationAccessExtension getAnnotationAccessExtension() {
		Object adapter= fTextEditor.getAdapter(IAnnotationAccess.class);
		if (adapter instanceof IAnnotationAccessExtension annotationExtension) {
			return annotationExtension;
		}

		return null;
	}

	/**
	 * Returns the resource for which to create the marker,
	 * or <code>null</code> if there is no applicable resource.
	 *
	 * @return the resource for which to create the marker or <code>null</code>
	 */
	protected final IResource getResource() {
		IEditorInput input= fTextEditor.getEditorInput();

		IResource resource= input.getAdapter(IFile.class);

		if (resource == null) {
			resource= input.getAdapter(IResource.class);
		}

		return resource;
	}

	/**
	 * Returns the <code>AbstractMarkerAnnotationModel</code> of the editor's input.
	 *
	 * @return the marker annotation model or <code>null</code> if there's none
	 */
	protected final AbstractMarkerAnnotationModel getAnnotationModel() {
		IDocumentProvider provider= fTextEditor.getDocumentProvider();
		IAnnotationModel model= provider.getAnnotationModel(fTextEditor.getEditorInput());
		if (model instanceof AbstractMarkerAnnotationModel annotationModel) {
			return annotationModel;
		}
		return null;
	}

	/**
	 * Returns the <code>IDocument</code> of the editor's input.
	 *
	 * @return the document of the editor's input
	 */
	protected final IDocument getDocument() {
		IDocumentProvider provider= fTextEditor.getDocumentProvider();
		return provider.getDocument(fTextEditor.getEditorInput());
	}

	/**
	 * Checks whether a position includes the ruler's line of activity.
	 *
	 * @param position the position to be checked
	 * @param document the document the position refers to
	 * @return <code>true</code> if the line is included by the given position
	 */
	protected final boolean includesRulerLine(Position position, IDocument document) {

		if (position != null) {
			try {
				int markerLine= document.getLineOfOffset(position.getOffset());
				int line= fRuler.getLineOfLastMouseButtonActivity();
				if (line == markerLine) {
					return true;
				// commented because of "1GEUOZ9: ITPJUI:ALL - Confusing UI for multi-line Bookmarks and Tasks"
				// return (markerLine <= line && line <= document.getLineOfOffset(position.getOffset() + position.getLength()));
				}
			} catch (BadLocationException x) {
			}
		}

		return false;
	}

	/**
	 * Checks whether a position includes the ruler's line of activity.
	 *
	 * @param position the position to be checked
	 * @param document the document the position refers to
	 * @param line the line of the last ruler activity
	 * @return <code>true</code> if the line is included by the given position
	 * @since 3.3
	 */
	private boolean includesLine(Position position, IDocument document, int line) {

		if (position != null) {
			try {
				int markerLine= document.getLineOfOffset(position.getOffset());
				if (line == markerLine) {
					return true;
				// commented because of "1GEUOZ9: ITPJUI:ALL - Confusing UI for multi-line Bookmarks and Tasks"
				// return (markerLine <= line && line <= document.getLineOfOffset(position.getOffset() + position.getLength()));
				}
			} catch (BadLocationException x) {
			}
		}

		return false;
	}

	/**
	 * Handles core exceptions. This implementation logs the exceptions
	 * with the workbench plug-in and shows an error dialog.
	 *
	 * @param exception the exception to be handled
	 * @param message the message to be logged with the given exception
	 */
	protected final void handleCoreException(CoreException exception, String message) {
		IStatus status;
		if (message != null) {
			status= new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, message, exception);
		} else {
			status= exception.getStatus();
		}
		logException(status);

		Shell shell= fTextEditor.getSite().getShell();
		String title= getString(fBundle, fPrefix + "error.dialog.title", fPrefix + "error.dialog.title"); //$NON-NLS-2$ //$NON-NLS-1$
		String msg= getString(fBundle, fPrefix + "error.dialog.message", fPrefix + "error.dialog.message"); //$NON-NLS-2$ //$NON-NLS-1$

		ErrorDialog.openError(shell, title, msg, exception.getStatus());
	}

	/**
	 * Log status.
	 *
	 * @param status the status to log
	 * @since 3.4
	 */
	private void logException(IStatus status) {
		Bundle bundle = Platform.getBundle(PlatformUI.PLUGIN_ID);
		ILog log= ILog.of(bundle);
		log.log(status);
	}

	/**
	 * Returns all markers which include the ruler's line of activity.
	 *
	 * @return an unmodifiable list with all markers which include the ruler's line of activity
	 */
	protected final List<IMarker> getMarkers() {
		final IResource resource= getResource();
		if (resource == null || !resource.exists()) {
			return Collections.emptyList();
		}

		final IDocument document= getDocument();
		if (document == null) {
			return Collections.emptyList();
		}

		final AbstractMarkerAnnotationModel model= getAnnotationModel();
		if (model == null) {
			return Collections.emptyList();
		}

		final IMarker[] allMarkers;
		try {
			allMarkers= resource.findMarkers(null, true, IResource.DEPTH_ZERO);
		} catch (CoreException x) {
			handleCoreException(x, TextEditorMessages.SelectMarkerRulerAction_getMarker);
			return Collections.emptyList();
		}

		if (allMarkers.length == 0) {
			return Collections.emptyList();
		}

		final int activeLine= fRuler.getLineOfLastMouseButtonActivity();
		if (activeLine == -1) {
			return Collections.emptyList();
		}

		Iterator<Annotation> it;
		try {
			IRegion line= document.getLineInformation(activeLine);
			it= model.getAnnotationIterator(line.getOffset(), line.getLength() + 1, true, true);
		} catch (BadLocationException e) {
			Bundle bundle= Platform.getBundle(PlatformUI.PLUGIN_ID);
			ILog.of(bundle).log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, e.getLocalizedMessage(), e));

			it= model.getAnnotationIterator();
		}

		List<IMarker> markers= null;
		while (it.hasNext()) {
			Annotation annotation= it.next();
			if (annotation instanceof MarkerAnnotation markerAnnotation) {
				Position position= model.getPosition(annotation);
				if (includesLine(position, document, activeLine)) {
					if (markers == null) {
						markers= new ArrayList<>(10);
					}

					markers.add(markerAnnotation.getMarker());
				}
			}
		}

		if (markers == null) {
			return Collections.emptyList();
		}

		return Collections.unmodifiableList(markers);
	}

	/**
	 * Returns <code>true</code> iff there are any markers which include the ruler's line of activity.
	 *
	 * @return <code>true</code> iff there are any markers which include the ruler's line of activity.
	 * @since 3.3
	 */
	protected final boolean hasMarkers() {
		final IResource resource= getResource();
		if (resource == null || !resource.exists()) {
			return false;
		}

		final IDocument document= getDocument();
		if (document == null) {
			return false;
		}

		final AbstractMarkerAnnotationModel model= getAnnotationModel();
		if (model == null) {
			return false;
		}

		final IMarker[] allMarkers;
		try {
			allMarkers= resource.findMarkers(null, true, IResource.DEPTH_ZERO);
		} catch (CoreException x) {
			handleCoreException(x, TextEditorMessages.SelectMarkerRulerAction_getMarker);
			return false;
		}

		if (allMarkers.length == 0) {
			return false;
		}

		final int activeLine= fRuler.getLineOfLastMouseButtonActivity();
		if (activeLine == -1) {
			return false;
		}

		Iterator<Annotation> it;
		try {
			IRegion line= document.getLineInformation(activeLine);
			it= model.getAnnotationIterator(line.getOffset(), line.getLength() + 1, true, true);
		} catch (BadLocationException e) {
			logException(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, e.getLocalizedMessage(), e));
			it= model.getAnnotationIterator();
		}

		while (it.hasNext()) {
			Annotation annotation= it.next();
			if (annotation instanceof MarkerAnnotation) {
				Position position= model.getPosition(annotation);
				if (includesLine(position, document, activeLine)) {
					return true;
				}
			}
		}

		return false;
	}
}
