package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.tasklist.TaskPropertiesDialog;



/**
 * A ruler action which can add and remove markers which have a visual 
 * representation in the ruler.
 * <p>
 * This class may be instantiated but is not intended for subclassing.
 * </p>
 */
public class MarkerRulerAction extends ResourceAction implements IUpdate {

	private IVerticalRuler fRuler;
	private ITextEditor fTextEditor;
	private String fMarkerType;
	private List fMarkers;
	private boolean fAskForLabel;

	private ResourceBundle fBundle;
	private String fPrefix;

	private String fAddLabel;
	private String fRemoveLabel;

	/**
	 * Creates a new action for the given ruler and editor. The action configures
	 * its visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param ruler the ruler
	 * @param editor the editor
	 * @param markerType the type of marker
	 * @param askForLabel <code>true</code> if the user should be asked for 
	 *   a label when a new marker is created 
	 * @see ResourceAction#ResourceAction
	 */
	public MarkerRulerAction(ResourceBundle bundle, String prefix, IVerticalRuler ruler, ITextEditor editor, String markerType, boolean askForLabel) {
		super(bundle, prefix);
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
	 * Returns this action's editor.
	 *
	 * @return this action's editor
	 */
	protected ITextEditor getTextEditor() {
		return fTextEditor;
	}
	
	/**
	 * Returns this action's vertical ruler.
	 *
	 * @return this action's vertical ruler
	 */
	protected IVerticalRuler getVerticalRuler() {
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
		fMarkers= getMarkers();
		setText(fMarkers.isEmpty() ? fAddLabel : fRemoveLabel);
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
				// commented because of "1GEUOZ9: ITPJUI:ALL - Confusing UI for multiline Bookmarks and Tasks"
				// return (markerLine <= line && line <= document.getLineOfOffset(position.getOffset() + position.getLength()));
			} catch (BadLocationException x) {
			}
		}
		
		return false;
	}

	/**
	 * Handles core exceptions. This implementation logs the exceptions
	 * with the workbech plugin.
	 *
	 * @param exception the exception to be handled
	 * @param message the message to be logged with the given exception
	 */
	protected void handleCoreException(CoreException exception, String message) {
		ILog log= Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog();
		
		if (message != null)
			log.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, null));
		
		log.log(exception.getStatus());
		
		
		Shell shell= getTextEditor().getSite().getShell();
		String title= getString(fBundle, fPrefix + "error.dialog.title", fPrefix + "error.dialog.title"); //$NON-NLS-2$ //$NON-NLS-1$
		String msg= getString(fBundle, fPrefix + "error.dialog.message", fPrefix + "error.dialog.message"); //$NON-NLS-2$ //$NON-NLS-1$
		
		ErrorDialog.openError(shell, title, msg, exception.getStatus());		
	}

	/**
	 * Returns all markers which include the ruler's line of activity.
	 *
	 * @returns all markers which include the ruler's line of activity
	 */
	protected List getMarkers() {

		List markers= new ArrayList();

		IResource resource= getResource();
		IDocument document= getDocument();
		AbstractMarkerAnnotationModel model= getAnnotationModel();

		if (resource != null && model != null) {
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
				handleCoreException(x, EditorMessages.getString("MarkerRulerAction.getMarker")); //$NON-NLS-1$
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
			if (IMarker.TASK.equals(fMarkerType)) {
                TaskPropertiesDialog dialog = new TaskPropertiesDialog(getTextEditor().getSite().getShell());
                dialog.setResource(resource);
                dialog.setInitialAttributes(attributes);
                dialog.open();
                return;
	        }
			if (!askForLabel(attributes))
				return;
		}
		
		try {
			MarkerUtilities.createMarker(resource, attributes, fMarkerType);
		} catch (CoreException x) {
			handleCoreException(x, EditorMessages.getString("MarkerRulerAction.addMarker")); //$NON-NLS-1$
		}
	}
	
	/**
	 * Removes the given markers.
	 *
	 * @param markers the markers to be deleted
	 */
	protected void removeMarkers(final List markers) {
		try {
			getResource().getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					for (int i= 0; i < markers.size(); ++i) {
						IMarker marker= (IMarker) markers.get(i);
						marker.delete();
					}
				}
			}, null);
		} catch (CoreException x) {
			handleCoreException(x, EditorMessages.getString("MarkerRulerAction.removeMarkers")); //$NON-NLS-1$
		}
	}
	
	/**
	 * Asks the user for a marker label. Returns <code>true</code> if a label
	 * is entered, <code>false</code> if the user cancels the input dialog.
	 * Sets the value of the attribute <code>message</code> in the given
	 * map of attributes.
	 *
	 * @param attributes the map of attributes
	 */
	protected boolean askForLabel(Map attributes) {

		Object o= attributes.get("message"); //$NON-NLS-1$
		String proposal= (o instanceof String) ? (String) o : ""; //$NON-NLS-1$
		if (proposal == null)
			proposal= ""; //$NON-NLS-1$

		String title= getString(fBundle, fPrefix + "add.dialog.title", fPrefix + "add.dialog.title"); //$NON-NLS-2$ //$NON-NLS-1$
		String message= getString(fBundle, fPrefix + "add.dialog.message", fPrefix + "add.dialog.message"); //$NON-NLS-2$ //$NON-NLS-1$
		IInputValidator inputValidator = new IInputValidator() {
			public String isValid(String newText) {
				return (newText == null || newText.length() == 0) ? " " : null; //$NON-NLS-1$
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
	 * @return the initial marker attributes
	 */
	protected Map getInitialAttributes() {
		
		Map attributes= new HashMap(11);
		
		IDocumentProvider provider= fTextEditor.getDocumentProvider();
		IDocument document= provider.getDocument(fTextEditor.getEditorInput());
		int line= fRuler.getLineOfLastMouseButtonActivity();
		int start= -1;
		int end= -1;

		try {
			
			IRegion lineInformation= document.getLineInformation(line);
			start= lineInformation.getOffset();
			int length= lineInformation.getLength();
				
			end= start + length;
		
		} catch (BadLocationException x) {
		}
		
		// marker line numbers are 1-based
		MarkerUtilities.setLineNumber(attributes, line + 1);
		MarkerUtilities.setCharStart(attributes, start);
		MarkerUtilities.setCharEnd(attributes, end);

		return attributes;
	}
}

