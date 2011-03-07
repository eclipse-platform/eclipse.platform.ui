/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> bug 38745
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.osgi.framework.Bundle;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateMarkersOperation;
import org.eclipse.ui.ide.undo.DeleteMarkersOperation;

/**
 * A ruler action which can add and remove markers which have a visual
 * representation in the ruler.
 * <p>
 * This class may be instantiated but is not intended for sub-classing.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MarkerRulerAction extends ResourceAction implements IUpdate {

	/** The maximum length of an proposed label. */
	private static final int MAX_LABEL_LENGTH= 80;

	/** The vertical ruler info of the editor. */
	private IVerticalRulerInfo fRuler;
	/** The associated editor */
	private ITextEditor fTextEditor;
	/** The of the marker to be created/removed. */
	private String fMarkerType;
	/** The cached list of markers covering a particular vertical ruler position. */
	private List fMarkers;
	/** The flag indicating whether user interaction is required. */
	private boolean fAskForLabel;
	/** The action's resource bundle. */
	private ResourceBundle fBundle;
	/** The prefix used for resource bundle look ups. */
	private String fPrefix;
	/** The cached action label when adding a marker. */
	private String fAddLabel;
	/** The cached action label when removing a marker. */
	private String fRemoveLabel;


	/**
	 * Creates a new action for the given ruler and editor. The action configures
	 * its visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in {@link org.eclipse.ui.texteditor.ResourceAction} constructor), or  <code>null</code> if none
	 * @param editor the editor
	 * @param ruler the ruler
	 * @param markerType the type of marker
	 * @param askForLabel <code>true</code> if the user should be asked for a label when a new marker is created
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 * @since 2.0
	 */
	public MarkerRulerAction(ResourceBundle bundle, String prefix,  ITextEditor editor, IVerticalRulerInfo ruler, String markerType, boolean askForLabel) {
		super(bundle, prefix);
		Assert.isLegal(editor != null);

		fRuler= ruler;
		fTextEditor= editor;
		fMarkerType= markerType;
		fAskForLabel= askForLabel;

		fBundle= bundle;
		fPrefix= prefix;

		fAddLabel= getString(bundle, prefix + "add.label", prefix + "add.label"); //$NON-NLS-2$ //$NON-NLS-1$
		fRemoveLabel= getString(bundle, prefix + "remove.label", prefix + "remove.label"); //$NON-NLS-2$ //$NON-NLS-1$
	}

	/**
	 * Creates a new action for the given ruler and editor. The action configures
	 * its visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 * @param ruler the ruler
	 * @param editor the editor
	 * @param markerType the type of the marker
	 * @param askForLabel <code>true</code> if the user should be asked for a label
	 * @deprecated use <code>MarkerRulerAction(ResourceBundle, String,  ITextEditor, IVerticalRulerInfo, String, boolean)</code> instead
	 */
	public MarkerRulerAction(ResourceBundle bundle, String prefix, IVerticalRuler ruler, ITextEditor editor, String markerType, boolean askForLabel) {
		this(bundle, prefix, editor, ruler, markerType, askForLabel);
	}


	/**
	 * Returns this action's text editor.
	 *
	 * @return this action's text editor
	 */
	protected ITextEditor getTextEditor() {
		return fTextEditor;
	}

	/**
	 * Returns this action's vertical ruler.
	 *
	 * @return this action's vertical ruler
	 * @deprecated use <code>getVerticalRulerInfo</code> instead
	 */
	protected IVerticalRuler getVerticalRuler() {
		if (fRuler instanceof IVerticalRuler)
			return (IVerticalRuler) fRuler;
		return null;
	}

	/**
	 * Returns this action's vertical ruler info.
	 *
	 * @return this action's vertical ruler info
	 * @since 2.0
	 */
	protected IVerticalRulerInfo getVerticalRulerInfo() {
		return fRuler;
	}

	/**
	 * Returns this action's resource bundle.
	 *
	 * @return this action's resource bundle
	 */
	protected ResourceBundle getResourceBundle() {
		return fBundle;
	}

	/**
	 * Returns this action's resource key prefix.
	 *
	 * @return this action's resource key prefix
	 */
	protected String getResourceKeyPrefix() {
		return fPrefix;
	}

	/*
	 * @see IUpdate#update()
	 */
	public void update() {
		//bug 38745
		IDocument document= getDocument();
		if (document != null) {
			int line= getVerticalRuler().getLineOfLastMouseButtonActivity() + 1;
			if (line > document.getNumberOfLines()) {
				setEnabled(false);
				setText(fAddLabel);
			} else {
				fMarkers= getMarkers();
				setEnabled(getResource() != null && (fMarkers.isEmpty() || markersUserEditable(fMarkers)));
				setText(fMarkers.isEmpty() ? fAddLabel : fRemoveLabel);
			}
		}
	}

	/**
	 * Returns whether the given markers are all editable by the user.
	 *
	 * @param markers the list of markers to test
	 * @return boolean <code>true</code> if they are all editable
	 * @since 3.2
	 */
	private boolean markersUserEditable(List markers) {
		Iterator iter= markers.iterator();
		while (iter.hasNext()) {
			if (!isUserEditable((IMarker)iter.next()))
				return false;
		}
		return true;
	}

	/**
	 * Returns whether the given marker is editable by the user.
	 *
	 * @param marker the marker to test
	 * @return boolean <code>true</code> if it is editable
	 * @since 3.2
	 */
	private boolean isUserEditable(IMarker marker) {
		return marker != null && marker.exists() && marker.getAttribute(IMarker.USER_EDITABLE, true);
	}

	/*
	 * @see Action#run()
	 */
	public void run() {
		if (fMarkers.isEmpty())
			addMarker();
		else
			removeMarkers(fMarkers);
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
	 * @return the marker annotation model
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
		Bundle bundle= Platform.getBundle(PlatformUI.PLUGIN_ID);
		ILog log= Platform.getLog(bundle);

		if (message != null)
			log.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, message, exception));
		else
			log.log(exception.getStatus());


		Shell shell= getTextEditor().getSite().getShell();
		String title= getString(fBundle, fPrefix + "error.dialog.title", fPrefix + "error.dialog.title"); //$NON-NLS-2$ //$NON-NLS-1$
		String msg= getString(fBundle, fPrefix + "error.dialog.message", fPrefix + "error.dialog.message"); //$NON-NLS-2$ //$NON-NLS-1$

		ErrorDialog.openError(shell, title, msg, exception.getStatus());
	}

	/**
	 * Returns all markers which include the ruler's line of activity.
	 *
	 * @return all a list of markers which include the ruler's line of activity
	 */
	protected List getMarkers() {

		List markers= new ArrayList();

		IResource resource= getResource();
		IDocument document= getDocument();
		AbstractMarkerAnnotationModel model= getAnnotationModel();

		if (resource != null && model != null && resource.exists()) {
			try {
				IMarker[] allMarkers= resource.findMarkers(fMarkerType, true, IResource.DEPTH_ZERO);
				if (allMarkers != null) {
					for (int i= 0; i < allMarkers.length; i++) {
						if (includesRulerLine(model.getMarkerPosition(allMarkers[i]), document)) {
							markers.add(allMarkers[i]);
						}
					}
				}
			} catch (CoreException x) {
				handleCoreException(x, TextEditorMessages.MarkerRulerAction_getMarker);
			}
		}

		return markers;
	}

	/**
	 * Creates a new marker according to the specification of this action and
	 * adds it to the marker resource.
	 */
	protected void addMarker() {
		IResource resource= getResource();
		if (resource == null)
			return;
		Map attributes= getInitialAttributes();
		if (fAskForLabel) {
			if (!askForLabel(attributes))
				return;
		}
		execute(new CreateMarkersOperation(fMarkerType, attributes, resource, getOperationName()));
	}

	/**
	 * Removes the given markers.
	 *
	 * @param markers the markers to be deleted
	 */
	protected void removeMarkers(final List markers) {
		IMarker[] markersArray= (IMarker[])markers.toArray(new IMarker[markers.size()]);
		execute(new DeleteMarkersOperation(markersArray, getOperationName()));
	}

	/**
	 * Asks the user for a marker label. Returns <code>true</code> if a label
	 * is entered, <code>false</code> if the user cancels the input dialog.
	 * Sets the value of the attribute <code>message</code> in the given
	 * map of attributes.
	 *
	 * @param attributes the map of attributes
	 * @return <code>true</code> if the map of attributes has successfully been initialized
	 */
	protected boolean askForLabel(Map attributes) {

		Object o= attributes.get("message"); //$NON-NLS-1$
		String proposal= (o instanceof String) ? (String) o : ""; //$NON-NLS-1$
		String title= getString(fBundle, fPrefix + "add.dialog.title", fPrefix + "add.dialog.title"); //$NON-NLS-2$ //$NON-NLS-1$
		String message= getString(fBundle, fPrefix + "add.dialog.message", fPrefix + "add.dialog.message"); //$NON-NLS-2$ //$NON-NLS-1$
		IInputValidator inputValidator= new IInputValidator() {
			public String isValid(String newText) {
				return (newText == null || newText.trim().length() == 0) ? " " : null; //$NON-NLS-1$
			}
		};
		InputDialog dialog= new InputDialog(fTextEditor.getSite().getShell(), title, message, proposal, inputValidator);

		String label= null;
		if (dialog.open() != Window.CANCEL)
			label= dialog.getValue();

		if (label == null)
			return false;

		label= label.trim();
		if (label.length() == 0)
			return false;

		MarkerUtilities.setMessage(attributes, label);
		return true;
	}

	/**
	 * Returns the attributes with which a newly created marker will be
	 * initialized.
	 *
	 * @return the initial marker attributes (key type: <code>String</code>, value type:
	 *         <code>Object</code>)
	 */
	protected Map getInitialAttributes() {

		Map attributes= new HashMap(11);

		IDocumentProvider provider= fTextEditor.getDocumentProvider();
		IDocument document= provider.getDocument(fTextEditor.getEditorInput());
		int line= fRuler.getLineOfLastMouseButtonActivity();
		int start= -1;
		int end= -1;
		int length= 0;

		try {

			IRegion lineInformation= document.getLineInformation(line);
			start= lineInformation.getOffset();
			length= lineInformation.getLength();

			end= start + length;


		} catch (BadLocationException x) {
		}

		// marker line numbers are 1-based
		MarkerUtilities.setMessage(attributes, getLabelProposal(document, start, length));
		MarkerUtilities.setLineNumber(attributes, line + 1);
		MarkerUtilities.setCharStart(attributes, start);
		MarkerUtilities.setCharEnd(attributes, end);

		return attributes;
	}

	/**
	 * Returns the initial label for the marker.
	 *
	 * @param document the document from which to extract a label proposal
	 * @param offset the document offset of the range from which to extract the label proposal
	 * @param length the length of the range from which to extract the label proposal
	 * @return the label proposal
	 * @since 3.0
	 */
	protected String getLabelProposal(IDocument document, int offset, int length) {
		try {
			String label= document.get(offset, length).trim();
			if (label.length() <= MAX_LABEL_LENGTH)
				return label;
			return label.substring(0, MAX_LABEL_LENGTH);
		} catch (BadLocationException x) {
			// don't propose label then
			return null;
		}
	}

	/**
	 * Returns the name to be used for the operation.
	 *
	 * @return the operation name
	 * @since 3.3
	 */
	private String getOperationName() {
		String name= getText();
		return name == null ? TextEditorMessages.AddMarkerAction_addMarker : name;
	}

	/**
	 * Execute the specified undoable operation.
	 *
	 * @param operation the operation to execute
	 * @since 3.3
	 */
	private void execute(IUndoableOperation operation) {
		final Shell shell= getTextEditor().getSite().getShell();
		IAdaptable context= new IAdaptable() {
			public Object getAdapter(Class adapter) {
				if (adapter == Shell.class)
					return shell;
				return null;
			}
		};

		IOperationHistory operationHistory= PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
		try {
			operationHistory.execute(operation, null, context);
		} catch (ExecutionException e) {
			Bundle bundle= Platform.getBundle(PlatformUI.PLUGIN_ID);
			ILog log= Platform.getLog(bundle);
			String msg= getString(fBundle, fPrefix + "error.dialog.message", fPrefix + "error.dialog.message"); //$NON-NLS-2$ //$NON-NLS-1$
			log.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, msg, e));
		}
	}
}
