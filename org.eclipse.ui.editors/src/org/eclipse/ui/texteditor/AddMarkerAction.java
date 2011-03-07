/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.osgi.framework.Bundle;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateMarkersOperation;


/**
 * Action for creating a marker of a specified type for the editor's
 * input element based on the editor's selection. If required, the
 * action asks the user to provide a marker label. The action is initially
 * associated with a text editor via the constructor, but that can be
 * subsequently changed using <code>setEditor</code>.
 * <p>
 * The following keys, prepended by the given option prefix,
 * are used for retrieving resources from the given bundle:
 * <ul>
 *   <li><code>"dialog.title"</code> - the input dialog's title</li>
 *   <li><code>"dialog.message"</code> - the input dialog's message</li>
 *   <li><code>"error.dialog.title"</code> - the error dialog's title</li>
 *   <li><code>"error.dialog.message"</code> - the error dialog's message</li>
 * </ul>
 * This class may be instantiated but is not intended to be subclassed.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class AddMarkerAction extends TextEditorAction {


	/** The maximum length of an proposed label. */
	private static final int MAX_LABEL_LENGTH= 80;
	/** The type for newly created markers. */
	private String fMarkerType;
	/** Should the user be asked for a label? */
	private boolean fAskForLabel;
	/** The action's resource bundle. */
	private ResourceBundle fBundle;
	/** The prefix used for resource bundle lookup. */
	private String fPrefix;


	/**
	 * Creates a new action for the given text editor. The action configures its
	 * visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param textEditor the text editor
	 * @param markerType the type of marker to add
	 * @param askForLabel <code>true</code> if the user should be asked for
	 *   a label for the new marker
	 * @see TextEditorAction#TextEditorAction(ResourceBundle, String, ITextEditor)
	 */
	public AddMarkerAction(ResourceBundle bundle, String prefix, ITextEditor textEditor, String markerType, boolean askForLabel) {
		super(bundle, prefix, textEditor);
		fBundle= bundle;
		fPrefix= prefix;
		fMarkerType= markerType;
		fAskForLabel= askForLabel;
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
	 * @see IAction#run()
	 */
	public void run() {
		IResource resource= getResource();
		if (resource == null)
			return;
		Map attributes= getInitialAttributes();
		if (fAskForLabel) {
			if (!askForLabel(attributes))
				return;
		}

		String name= getToolTipText();
		name= name == null ? TextEditorMessages.AddMarkerAction_addMarker : name;

		final Shell shell= getTextEditor().getSite().getShell();
		IAdaptable context= new IAdaptable() {
			public Object getAdapter(Class adapter) {
				if (adapter == Shell.class)
					return shell;
				return null;
			}
		};

		IUndoableOperation operation= new CreateMarkersOperation(fMarkerType, attributes, resource, name);
		IOperationHistory operationHistory= PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
		try {
			operationHistory.execute(operation, null, context);
		} catch (ExecutionException x) {
			Bundle bundle= Platform.getBundle(PlatformUI.PLUGIN_ID);
			ILog log= Platform.getLog(bundle);
			String msg= getString(fBundle, fPrefix + "error.dialog.message", fPrefix + "error.dialog.message"); //$NON-NLS-2$ //$NON-NLS-1$
			log.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, msg, x));
		}
	}

	/*
	 * @see TextEditorAction#update()
	 */
	public void update() {
		setEnabled(getResource() != null);
	}

	/**
	 * Asks the user for a marker label. Returns <code>true</code> if a label
	 * is entered, <code>false</code> if the user cancels the input dialog.
	 * The value for the attribute <code>message</code> is modified in the given
	 * attribute map.
	 *
	 * @param attributes the attributes map
	 * @return <code>true</code> if a label has been entered
	 */
	protected boolean askForLabel(Map attributes) {

		Object o= attributes.get("message"); //$NON-NLS-1$
		String proposal= (o instanceof String) ? (String) o : ""; //$NON-NLS-1$
		String title= getString(fBundle, fPrefix + "dialog.title", fPrefix + "dialog.title"); //$NON-NLS-2$ //$NON-NLS-1$
		String message= getString(fBundle, fPrefix + "dialog.message", fPrefix + "dialog.message"); //$NON-NLS-2$ //$NON-NLS-1$
		IInputValidator inputValidator= new IInputValidator() {
			public String isValid(String newText) {
				return  (newText == null || newText.trim().length() == 0) ? " " : null;  //$NON-NLS-1$
			}
		};
		InputDialog dialog= new InputDialog(getTextEditor().getSite().getShell(), title, message, proposal, inputValidator);

		String label= null;
		if (dialog.open() != Window.CANCEL)
			label= dialog.getValue();

		if (label == null)
			return false;

		label= label.trim();
		if (label.length() == 0)
			return false;

		attributes.put("message", label); //$NON-NLS-1$
		return true;
	}

	/**
	 * Returns the attributes the new marker will be initialized with.
	 * <p>
	 * Subclasses may extend or replace this method.</p>
	 *
	 * @return the attributes the new marker will be initialized with
	 */
	protected Map getInitialAttributes() {

		Map attributes= new HashMap(11);

		ITextSelection selection= (ITextSelection) getTextEditor().getSelectionProvider().getSelection();
		if (!selection.isEmpty()) {

			int start= selection.getOffset();
			int length= selection.getLength();

			if (length < 0) {
				length= -length;
				start -= length;
			}

			MarkerUtilities.setCharStart(attributes, start);
			MarkerUtilities.setCharEnd(attributes, start + length);

			// marker line numbers are 1-based
			int line= selection.getStartLine();
			MarkerUtilities.setLineNumber(attributes, line == -1 ? -1 : line + 1);

			IDocument document= getTextEditor().getDocumentProvider().getDocument(getTextEditor().getEditorInput());
			MarkerUtilities.setMessage(attributes, getLabelProposal(document, start, length));

		}

		return attributes;
	}

	/**
	 * Returns the initial label for the marker.
	 *
	 * @param document the document from which to extract a label proposal
	 * @param offset the document offset of the range from which to extract the label proposal
	 * @param length the length of the range from which to extract the label proposal
	 * @return the label proposal
	 */
	protected String getLabelProposal(IDocument document, int offset, int length) {


		try {


			if (length > 0) {

				// find first white char but skip leading white chars
				int i= 0;
				boolean skip= true;
				while (i < length) {
					boolean isWhitespace= Character.isWhitespace(document.getChar(offset + i));
					if (!skip && isWhitespace)
						break;
					if (skip && !isWhitespace)
						skip= false;
					i++;
				}

				String label= document.get(offset, i);
				return label.trim();
			}


			char ch;

			// Get the first white char before the selection.
			int left= offset;

			int line= document.getLineOfOffset(offset);
			int limit= document.getLineOffset(line);

			while (left > limit) {
				ch= document.getChar(left);
				if (Character.isWhitespace(ch))
					break;
				--left;
			}

			limit += document.getLineLength(line);

			// Now get the first letter.
			while (left <= limit) {
				ch= document.getChar(left);
				if (!Character.isWhitespace(ch))
					break;
				++left;
			}

			if (left > limit)
				return null;

			limit= Math.min(limit, left + MAX_LABEL_LENGTH);

			// Get the next white char.
			int right= (offset + length > limit ? limit : offset + length);
			while (right < limit) {
				ch= document.getChar(right);
				if (Character.isWhitespace(ch))
					break;
				++right;
			}

			// Trim the string and return it.
			if (left != right) {
				String label= document.get(left, right - left);
				return label.trim();
			}

		} catch (BadLocationException x) {
			// don't proposal label then
		}

		return null;
	}

	/**
	 * Returns the resource on which to create the marker,
	 * or <code>null</code> if there is no applicable resource. This
	 * queries the editor's input using <code>getAdapter(IResource.class)</code>.
	 * Subclasses may override this method.
	 *
	 * @return the resource to which to attach the newly created marker
	 */
	protected IResource getResource() {
		ITextEditor editor= getTextEditor();
		if (editor != null) {
			IEditorInput input= editor.getEditorInput();
			return (IResource) ((IAdaptable) input).getAdapter(IResource.class);
		}
		return null;
	}
}
