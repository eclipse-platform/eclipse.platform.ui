/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.editors.quickdiff;

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.jface.action.Action;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ILineDiffer;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.IVerticalRulerInfoExtension;
import org.eclipse.jface.text.source.LineNumberRulerColumn;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.quickdiff.*;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

import org.eclipse.ui.internal.editors.quickdiff.engine.DocumentLineDiffer;

/**
 * Action to set the quick diff reference for the document displayed in the editor. An instance of
 * this class is created for every extension to the extension point <code>quickdiff.referenceprovider</code>, and for
 * every editor. It acts as a proxy; its <code>run</code> method installs the reference provider
 * specified by the extension with the quick diff differ on the current document.
 * @since 3.0
 */
public class ReferenceSelectionAction extends Action implements IUpdate {

	/** The editor we get the document from. */
	private ITextEditor fEditor= null;
	/** The descriptor of the managed extension. */
	private final ReferenceProviderDescriptor fDescriptor;
	/** The implementation of the extension, after it has been loaded. */
	private IQuickDiffProviderImplementation fProvider;

	/**
	 * Creates a new instance that will lazily create the implementation provided by the extension.
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
	private IQuickDiffProviderImplementation getProvider() {
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
		IQuickDiffProviderImplementation provider= getProvider();
		if (provider != null) {
			provider.setActiveEditor(fEditor);
			if (provider.isEnabled())
				differ.setReferenceProvider(provider);
			else
				setEnabled(false);
		}
		if (!isConnected()) {
			IVerticalRulerColumn column= getColumn();
			if (column != null)
				column.setModel(differ);
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
	 * @param createIfNeeded when set to <code>true</code>, a new differ will be created if needed.
	 * @return the differ installed with the annotation model, or <code>null</code>.
	 */
	private DocumentLineDiffer getDiffer(boolean createIfNeeded) {
		if (fEditor == null)
			return null;
		IDocumentProvider provider= fEditor.getDocumentProvider();
		IEditorInput editorInput= fEditor.getEditorInput();
		IAnnotationModel m= provider.getAnnotationModel(editorInput);
		IAnnotationModelExtension model= null;
		if (m instanceof IAnnotationModelExtension) {
			model= (IAnnotationModelExtension)m;
		} else {
			return null;
		}

		DocumentLineDiffer differ= (DocumentLineDiffer)model.getAnnotationModel(ILineDiffer.ID);

		if (differ == null && createIfNeeded) {
			differ= new DocumentLineDiffer();
			model.addAnnotationModel(ILineDiffer.ID, differ);
		}
		return differ;
	}

	/**
	 * States whether an incremental differ has been
	 * installed with the line number bar.
	 * @return <code>true</code> if a differ has been installed on <code>fEditor</code>.
	 */
	private boolean isConnected() {
		IVerticalRulerColumn column= getColumn();
		if (column instanceof IVerticalRulerInfoExtension) {
			IAnnotationModel m= ((IVerticalRulerInfoExtension)column).getModel();
			if (m instanceof DocumentLineDiffer)
				return true;
		}
		return false;
	}

	/**
	 * Returns the linenumber ruler of <code>fEditor</code>, or <code>null</code> if it cannot be
	 * found.
	 * @return an instance of <code>LineNumberRulerColumn</code> or <code>null</code>.
	 */
	private IVerticalRulerColumn getColumn() {
		// HACK: we get the IVerticalRulerInfo and assume its a CompositeRuler.
		// will get broken if IVerticalRulerInfo implementation changes
		if (fEditor instanceof IAdaptable) {
			IVerticalRulerInfo info= (IVerticalRulerInfo) ((IAdaptable)fEditor).getAdapter(IVerticalRulerInfo.class);
			if (info instanceof CompositeRuler) {
				for (Iterator it= ((CompositeRuler)info).getDecoratorIterator(); it.hasNext();) {
					IVerticalRulerColumn c= (IVerticalRulerColumn)it.next();
					if (c instanceof LineNumberRulerColumn)
						return c;
				}
			}
		}
		return null;
	}

}
