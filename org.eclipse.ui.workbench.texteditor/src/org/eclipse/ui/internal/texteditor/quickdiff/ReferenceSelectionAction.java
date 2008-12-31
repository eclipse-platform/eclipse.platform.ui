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

package org.eclipse.ui.internal.texteditor.quickdiff;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.action.Action;

import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IChangeRulerColumn;

import org.eclipse.ui.IEditorInput;

import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension3;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider;
import org.eclipse.ui.texteditor.quickdiff.ReferenceProviderDescriptor;


/**
 * Action to set the quick diff reference for the document displayed in the editor. An instance of
 * this class is created for every extension to the extension point <code>quickdiff.referenceprovider</code>, and for
 * every editor. It acts as a proxy; its <code>run</code> method installs the reference provider
 * specified by the extension with the quick diff differ on the current document.
 *
 * @since 3.0
 */
public class ReferenceSelectionAction extends Action implements IUpdate {

	/** The editor we get the document from. */
	private ITextEditor fEditor= null;
	/** The descriptor of the managed extension. */
	private final ReferenceProviderDescriptor fDescriptor;
	/** The implementation of the extension, after it has been loaded. */
	private IQuickDiffReferenceProvider fProvider;

	/**
	 * Creates a new instance that will lazily create the implementation provided by the extension.
	 *
	 * @param descriptor describes the extension.
	 * @param editor the editor for which this action is created.
	 */
	public ReferenceSelectionAction(ReferenceProviderDescriptor descriptor, ITextEditor editor) {
		super("", AS_RADIO_BUTTON); //$NON-NLS-1$
		setChecked(false);
		setEnabled(true);
		Assert.isLegal(descriptor != null);
		fDescriptor= descriptor;
		fEditor= editor;
		update();
	}

	/**
	 * Creates an instance of the implementation provided by the extension, if none has been created
	 * before. Otherwise, the cached implementation is returned.
	 * @return The <code>IQuickDiffProviderImplementation</code> instance provided by the extension.
	 */
	private IQuickDiffReferenceProvider getProvider() {
		if (fProvider == null) {
			fProvider= fDescriptor.createProvider();
		}
		return fProvider;
	}

	/*
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {

		DocumentLineDiffer differ= getDiffer(true); // create if needed, so the user does not have to toggle display when he selects a reference
		if (differ == null)
			return;

		if (fEditor instanceof ITextEditorExtension3) {
			ITextEditorExtension3 extension= (ITextEditorExtension3) fEditor;
			IQuickDiffReferenceProvider provider= getProvider();
			if (provider != null) {
				provider.setActiveEditor(fEditor);
				if (provider.isEnabled()) {
					differ.setReferenceProvider(provider);
					extension.showChangeInformation(true);
					setEnabled(true);
				} else
					setEnabled(false);
			}
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		/* two things happen here:
		 * 1: checked state setting - if a provider is already installed, and its id matches
		 * our id, we are in checked state.
		 * 2: enablement - if the extending plugin has been loaded, we check the provider for
		 * enablement and take it as our own.
		 */
		setText(fDescriptor.getLabel());
		DocumentLineDiffer differ= getDiffer(false); // don't create it if we're not showing
		setChecked(false);
		if (differ != null) {
			IQuickDiffReferenceProvider provider= differ.getReferenceProvider();
			if (provider != null && provider.getId().equals(fDescriptor.getId())) {
				setChecked(true);
			}
		}

		if (fDescriptor.isPluginLoaded()) {
			getProvider();
			if (fProvider == null) {
				setEnabled(false);
			} else {
				fProvider.setActiveEditor(fEditor);
				setEnabled(fProvider.isEnabled());
			}
		} else {
			// optimistically enable it
			setEnabled(true);
		}
	}

	/**
	 * Fetches the differ installed with the current editor's document's annotation model. If none
	 * is installed yet, and <code>createIfNeeded</code> is true, one is created and attached to the
	 * model.
	 *
	 * @param createIfNeeded when set to <code>true</code>, a new differ will be created if needed.
	 * @return the differ installed with the annotation model, or <code>null</code>.
	 */
	private DocumentLineDiffer getDiffer(boolean createIfNeeded) {
		// get annotation model
		if (fEditor == null)
			return null;

		IDocumentProvider provider= fEditor.getDocumentProvider();
		IEditorInput editorInput= fEditor.getEditorInput();
		if (provider == null || editorInput == null)
			return null;

		IAnnotationModel m= provider.getAnnotationModel(editorInput);
		IAnnotationModelExtension model= null;
		if (m instanceof IAnnotationModelExtension) {
			model= (IAnnotationModelExtension)m;
		} else {
			return null;
		}

		// get differ
		DocumentLineDiffer differ= (DocumentLineDiffer)model.getAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID);

		// create if needed
		if (differ == null && createIfNeeded) {
			differ= new DocumentLineDiffer();
			model.addAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID, differ);
		}

		return differ;
	}
}
