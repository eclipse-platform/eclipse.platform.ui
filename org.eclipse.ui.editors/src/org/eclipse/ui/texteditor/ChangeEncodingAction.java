/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

import org.eclipse.ui.editors.text.IEncodingSupport;

import org.eclipse.ui.ide.dialogs.AbstractEncodingFieldEditor;
import org.eclipse.ui.ide.dialogs.EncodingFieldEditor;
import org.eclipse.ui.ide.dialogs.ResourceEncodingFieldEditor;

/**
 * Action for changing the the encoding of the editor's
 * input element.
 * <p>
 * The following keys, prepended by the given option prefix,
 * are used for retrieving resources from the given bundle:
 * <ul>
 *   <li><code>"dialog.title"</code> - the input dialog's title</li>
 * </ul>
 * This class may be instantiated but is not intended to be subclassed.
 * </p>
 * 
 * @since 3.1
 */
public class ChangeEncodingAction extends TextEditorAction {

	private static final int APPLY_ID= IDialogConstants.OK_ID + IDialogConstants.CANCEL_ID + 1;
	
	private String fDialogTitle;
	private static final String ENCODING_PREF_KEY= "encoding"; //$NON-NLS-1$

	/**
	 * Creates a new action for the given text editor. The action configures its
	 * visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @see TextEditorAction#TextEditorAction(ResourceBundle, String, ITextEditor)
	 */
	public ChangeEncodingAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
		
		String key= "dialog.title"; //$NON-NLS-1$;
		if (prefix != null && prefix.length() > 0)
			key= prefix + key;

		fDialogTitle= getString(bundle, key, null); 
	}
	
	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		final IResource resource= getResource();
		final Shell parentShell= getTextEditor().getSite().getShell();
		final IEncodingSupport encodingSupport= getEncodingSupport();
		if (resource == null && encodingSupport == null) {
			MessageDialog.openInformation(parentShell, fDialogTitle, "No encoding support installed");
			return;
		}
		
		Dialog dialog= new Dialog(parentShell) {
			private AbstractEncodingFieldEditor fEncodingEditor;
			private IPreferenceStore store= null;
			
			/*
			 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
			 */
			protected void configureShell(Shell newShell) {
				super.configureShell(newShell);
				newShell.setText(fDialogTitle);
			}
			
			/*
			 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
			 */
			protected Control createDialogArea(Composite parent) {
				Control composite= super.createDialogArea(parent);
				if (!(composite instanceof Composite)) {
					composite.dispose();
					composite= new Composite(parent, SWT.NONE);
				}
				
				GridLayout layout= new GridLayout();
				layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
				layout.marginWidth= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
				layout.verticalSpacing= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
				layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
				parent.setLayout(layout);
				
				GridData data = new GridData(GridData.FILL_BOTH);
				composite.setLayoutData(data);
				composite.setFont(parent.getFont());
				
				if (resource != null) {
					fEncodingEditor= new ResourceEncodingFieldEditor("", (Composite)composite, resource); //$NON-NLS-1$
				} else {
					fEncodingEditor= new EncodingFieldEditor(ENCODING_PREF_KEY, "", (Composite)composite); //$NON-NLS-1$
					store= new PreferenceStore();
					store.setDefault(ENCODING_PREF_KEY, encodingSupport.getDefaultEncoding());
					fEncodingEditor.setPreferenceStore(store);
					String encoding= encodingSupport.getEncoding();
					if (encoding != null)
						store.setValue(ENCODING_PREF_KEY, encoding);
				}
				fEncodingEditor.load();
				
				return composite;
			}
			
			/*
			 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
			 */
			protected void createButtonsForButtonBar(Composite parent) {
				createButton(parent, APPLY_ID, "Apply", false);
				super.createButtonsForButtonBar(parent);
			}
			
			/*
			 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
			 */
			protected void buttonPressed(int buttonId) {
				if (buttonId == APPLY_ID)
					apply();
				else
					super.buttonPressed(buttonId);
			}
			
			/*
			 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
			 */
			protected void okPressed() {
				apply();
				super.okPressed();
			}
			
			private void apply() {
				fEncodingEditor.store();
				
				if (resource == null) {
					String encoding= fEncodingEditor.getPreferenceStore().getString(fEncodingEditor.getPreferenceName());
					encodingSupport.setEncoding(encoding);
				}
			}
		};
		dialog.open();
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setEnabled((getResource() != null || getEncodingSupport() != null) && !getTextEditor().isDirty());
	}
	
	/**
	 * Gets the resource which is being edited in the editor.
	 * 
	 * @return the resource being edited or <code>null</code>s
	 */
	private IResource getResource() {
		if (getTextEditor() != null && getTextEditor().getEditorInput() != null)
			return (IResource)getTextEditor().getEditorInput().getAdapter(IResource.class);
		
		return null;
	}
	
	/**
	 * Gets the editor's encoding support.
	 * 
	 * @return the resource being edited or <code>null</code>s
	 */
	private IEncodingSupport getEncodingSupport() {
		if (getTextEditor() != null)
			return (IEncodingSupport)getTextEditor().getAdapter(IEncodingSupport.class);

		return null;
	}
}
