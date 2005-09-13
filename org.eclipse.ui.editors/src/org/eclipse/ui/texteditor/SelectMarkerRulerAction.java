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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.osgi.framework.Bundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;


/**
 * A ruler action which can select the textual range of a marker
 * that has a visual representation in a vertical ruler.
 * <p>
 * This class may be instantiated but is not intended for sub-classing.
 * </p>
 * @since 2.0
 */
public class SelectMarkerRulerAction extends ResourceAction implements IUpdate {

	/** The vertical ruler info of the action's editor. */
	private IVerticalRulerInfo fRuler;
	/** The associated editor. */
	private ITextEditor fTextEditor;
	/** The cached list of markers including a given vertical ruler location. */
	private List fMarkers;
	/** The action's resource bundle. */
	private ResourceBundle fBundle;
	/** The prefix for resource bundle lookups. */
	private String fPrefix;

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
	public SelectMarkerRulerAction(ResourceBundle bundle, String prefix, IVerticalRuler ruler, ITextEditor editor) {
		this(bundle, prefix, editor, ruler);
	}

	/*
	 * @see IUpdate#update()
	 */
	public void update() {
		fMarkers= getMarkers();
		setEnabled(!fMarkers.isEmpty());
	}

	/*
	 * @see Action#run()
	 */
	public void run() {

		IMarker marker= chooseMarker(fMarkers);
		if (marker == null)
			return;

		boolean isProblemMarker= MarkerUtilities.isMarkerType(marker, IMarker.PROBLEM);
		boolean isTaskMarker= MarkerUtilities.isMarkerType(marker, IMarker.TASK);
		if (isProblemMarker || isTaskMarker) {
			IWorkbenchPage page= fTextEditor.getSite().getPage();
			IViewPart view= page.findView(isProblemMarker ? IPageLayout.ID_PROBLEM_VIEW: IPageLayout.ID_TASK_LIST);
			if (view != null) {
				boolean selectionSet= false;
				try {
					Method method= view.getClass().getMethod("setSelection", new Class[] { IStructuredSelection.class, boolean.class}); //$NON-NLS-1$
					method.invoke(view, new Object[] {new StructuredSelection(marker), Boolean.TRUE });
					selectionSet= true;
				} catch (NoSuchMethodException x) {
					selectionSet= false;
				} catch (IllegalAccessException x) {
					selectionSet= false;
				} catch (InvocationTargetException x) {
					selectionSet= false;
				}

				if (selectionSet)
					return;
			}
		}
		// Select and reveal in editor
		int offset= MarkerUtilities.getCharStart(marker);
		int endOffset= MarkerUtilities.getCharEnd(marker);
		if (offset > -1 && endOffset > -1)
			fTextEditor.selectAndReveal(offset, endOffset - offset);

	}

	/**
	 * Chooses the marker with the highest layer. If there are multiple
	 * markers at the found layer, the first marker is taken.
	 *
	 * @param markers the list of markers to choose from
	 * @return the chosen marker or <code>null</code> if none of the given markers has a marker annotation in the model
	 */
	protected IMarker chooseMarker(List markers) {

		AbstractMarkerAnnotationModel model= getAnnotationModel();
		IAnnotationAccessExtension access= getAnnotationAccessExtension();

		IMarker marker= null;
		int maxLayer= 0;

		Iterator iter= markers.iterator();
		while (iter.hasNext()) {
			IMarker m= (IMarker) iter.next();
			Annotation a= model.getMarkerAnnotation(m);
			if (a != null) {
				if (access == null) {
					marker= m;
					break;
				}
				int l= access.getLayer(a);
				if (l == maxLayer) {
					if (marker == null)
						marker= m;
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
	protected IAnnotationAccessExtension  getAnnotationAccessExtension() {
		Object adapter= fTextEditor.getAdapter(IAnnotationAccess.class);
		if (adapter instanceof IAnnotationAccessExtension)
			return (IAnnotationAccessExtension)adapter;

		return null;
	}

	/**
	 * Returns the resource for which to create the marker,
	 * or <code>null</code> if there is no applicable resource.
	 *
	 * @return the resource for which to create the marker or <code>null</code>
	 */
	protected IResource getResource() {
		IEditorInput input= fTextEditor.getEditorInput();

		IResource resource= (IResource) input.getAdapter(IFile.class);

		if (resource == null)
			resource= (IResource) input.getAdapter(IResource.class);

		return resource;
	}

	/**
	 * Returns the <code>AbstractMarkerAnnotationModel</code> of the editor's input.
	 *
	 * @return the marker annotation model or <code>null</code> if there's none
	 */
	protected AbstractMarkerAnnotationModel getAnnotationModel() {
		IDocumentProvider provider= fTextEditor.getDocumentProvider();
		IAnnotationModel model= provider.getAnnotationModel(fTextEditor.getEditorInput());
		if (model instanceof AbstractMarkerAnnotationModel)
			return (AbstractMarkerAnnotationModel) model;
		return null;
	}

	/**
	 * Returns the <code>IDocument</code> of the editor's input.
	 *
	 * @return the document of the editor's input
	 */
	protected IDocument getDocument() {
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
	protected boolean includesRulerLine(Position position, IDocument document) {

		if (position != null) {
			try {
				int markerLine= document.getLineOfOffset(position.getOffset());
				int line= fRuler.getLineOfLastMouseButtonActivity();
				if (line == markerLine)
					return true;
				// commented because of "1GEUOZ9: ITPJUI:ALL - Confusing UI for multi-line Bookmarks and Tasks"
				// return (markerLine <= line && line <= document.getLineOfOffset(position.getOffset() + position.getLength()));
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
	protected void handleCoreException(CoreException exception, String message) {
		Bundle bundle = Platform.getBundle(PlatformUI.PLUGIN_ID);
		ILog log= Platform.getLog(bundle);

		if (message != null)
			log.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, exception));
		else
			log.log(exception.getStatus());


		Shell shell= fTextEditor.getSite().getShell();
		String title= getString(fBundle, fPrefix + "error.dialog.title", fPrefix + "error.dialog.title"); //$NON-NLS-2$ //$NON-NLS-1$
		String msg= getString(fBundle, fPrefix + "error.dialog.message", fPrefix + "error.dialog.message"); //$NON-NLS-2$ //$NON-NLS-1$

		ErrorDialog.openError(shell, title, msg, exception.getStatus());
	}

	/**
	 * Returns all markers which include the ruler's line of activity.
	 *
	 * @return a list with all markers which include the ruler's line of activity
	 */
	protected List getMarkers() {

		List markers= new ArrayList();

		IResource resource= getResource();
		IDocument document= getDocument();
		AbstractMarkerAnnotationModel model= getAnnotationModel();

		if (resource != null && model != null && resource.exists()) {
			try {
				IMarker[] allMarkers= resource.findMarkers(null, true, IResource.DEPTH_ZERO);
				if (allMarkers != null) {
					for (int i= 0; i < allMarkers.length; i++) {
						if (includesRulerLine(model.getMarkerPosition(allMarkers[i]), document)) {
							markers.add(allMarkers[i]);
						}
					}
				}
			} catch (CoreException x) {
				handleCoreException(x, TextEditorMessages.SelectMarkerRulerAction_getMarker);
			}
		}

		return markers;
	}
}
