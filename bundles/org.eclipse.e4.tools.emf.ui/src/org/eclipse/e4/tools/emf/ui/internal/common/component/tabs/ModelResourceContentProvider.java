/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import java.util.ArrayList;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ModelResourceContentProvider implements IStructuredContentProvider {

	private Object[] items = new Object[0];
	private Viewer viewer;

	public ModelResourceContentProvider() {
		// adapter = new EContentAdapter() {
		// @Override
		// public void notifyChanged(Notification notification) {
		// switch (notification.getEventType()) {
		// case Notification.REMOVING_ADAPTER:
		// return;
		// }
		//
		// if (viewer != null && viewer.getControl().isDisposed() == false) {
		// viewer.refresh();
		// }
		// super.notifyChanged(notification);
		//
		// // boolean optimize = false;
		// // switch (notification.getEventType()) {
		// //
		// // case Notification.SET:
		// // // optimized to only update row if SET and not elementId
		// // // if the id is modified, we need to reindex our id map
		// // // (by forcing input)
		//				//						if (notification.getFeature() != EmfUtil.getAttribute((EObject) notification.getFeature(), "elementId")) { //$NON-NLS-1$
		// // if (viewer instanceof TableViewer) {
		// // optimize = true;
		// // }
		// // }
		// // break;
		// // default:
		// // break;
		// // }
		// // if (optimize) {
		// // EClass eClass = (EClass) ((EAttribute)
		// // notification.getFeature()).eContainer();
		// // ((TableViewer) viewer).update(eClass, null);
		// // } else {
		// // viewer.setInput(viewer.getInput());
		// // }
		// }
		// };
	}

	@Override
	public Object[] getElements(Object object) {
		return items;
	}

	@Override
	public void inputChanged(final Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		ArrayList<EObject> list = new ArrayList<EObject>();
		IModelResource modelProvider = (IModelResource) newInput;
		if (newInput != oldInput && newInput != null) {
			// ((EObject)
			// modelProvider.getRoot().get(0)).eAdapters().add(adapter);
		}
		if (newInput != null) {
			TreeIterator<Object> itTree = EcoreUtil.getAllContents(modelProvider.getRoot());
			while (itTree.hasNext()) {
				Object next = itTree.next();
				EObject eObject = (EObject) next;
				EAttribute att = EmfUtil.getAttribute(eObject, "elementId"); //$NON-NLS-1$
				if (att != null) {
					list.add(eObject);
				}
			}
		}

		items = list.toArray(new Object[0]);
	}

	@Override
	public void dispose() {
	}
}
