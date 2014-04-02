/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 396902
 ******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.common.xml.EMFDocumentResourceMediator;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * Adds additional functionality to TreeViewer
 *
 * @author Steven Spungin
 *
 */
public class TreeViewerEx extends TreeViewer {

	private ArrayList<String> elementsIds;
	private ArrayList<String> selectedIds;

	public TreeViewerEx(Composite parent, int style, EMFDocumentResourceMediator emfDocumentProvider, final IModelResource modelProvider) {
		super(parent, style);

		emfDocumentProvider.getDocument().addDocumentListener(new IDocumentListener() {

			@Override
			public void documentChanged(DocumentEvent event) {
				try {
					// restore Nodes from XmiIds
					E4XMIResource xmiResource = (E4XMIResource) ((EObject) modelProvider.getRoot().get(0)).eResource();
					ArrayList<Object> newElements = new ArrayList<Object>();
					ObservableListTreeContentProvider provider = (ObservableListTreeContentProvider) getContentProvider();
					for (String id : elementsIds) {
						EObject eObject = xmiResource.getEObject(id);
						if (eObject != null) {
							newElements.add(eObject);
							// force tree node creation
							getFirstMatchingItem(eObject, provider, provider.getChildren(getInput()));
						}
					}
					ArrayList<Object> newSelected = new ArrayList<Object>();
					for (String id : selectedIds) {
						EObject eObject = xmiResource.getEObject(id);
						if (eObject != null) {
							newSelected.add(eObject);
							// force tree node creation
							getFirstMatchingItem(eObject, provider, provider.getChildren(getInput()));
						}
					}
					setExpandedElements(newElements.toArray(new Object[0]));
					setSelection(new StructuredSelection(newSelected));

					// update our stored id values
					documentAboutToBeChanged(event);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
				try {
					// Stash XmiIds
					Object[] elements = getExpandedElements();
					List<?> selected = ((IStructuredSelection) getSelection()).toList();
					E4XMIResource xmiResource = (E4XMIResource) ((EObject) modelProvider.getRoot().get(0)).eResource();
					elementsIds = new ArrayList<String>();
					selectedIds = new ArrayList<String>();
					for (Object obj : elements) {
						if (obj instanceof EObject) {
							elementsIds.add(xmiResource.getID((EObject) obj));
						}
					}
					for (Object obj : selected) {
						if (obj instanceof EObject) {
							selectedIds.add(xmiResource.getID((EObject) obj));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// This will ensure the provider has created the tree node (so we can reveal
	// it).
	static public Object getFirstMatchingItem(EObject target, ObservableListTreeContentProvider provider, Object[] items) {
		for (int i = 0; i < items.length; i++) {
			if (items[i] == target) {
				return items[i];
			}
			Object found = getFirstMatchingItem(target, provider, provider.getChildren(items[i]));
			if (found != null) {
				return found;
			}
		}
		return null;
	}
}
