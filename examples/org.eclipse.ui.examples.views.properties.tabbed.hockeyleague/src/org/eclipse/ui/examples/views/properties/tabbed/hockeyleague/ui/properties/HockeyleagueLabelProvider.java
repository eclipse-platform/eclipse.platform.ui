/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties;

import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.editor.HockeyleagueEditor;

/**
 * Label provider for the title bar for the tabbed property sheet page.
 * 
 * @author Anthony Hunter
 */
public class HockeyleagueLabelProvider
	extends LabelProvider {

	private AdapterFactoryLabelProvider adapterFactoryLabelProvider;

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if (element == null || element.equals(StructuredSelection.EMPTY)) {
			return null;
		}
		if (element instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) element;
			if (areDifferentTypes(structuredSelection)) {
				return null;
			}
			element = structuredSelection.getFirstElement();
		}
		if (element instanceof EObject || element instanceof Resource) {
			return getAdapterFactoryLabelProvider().getImage(element);
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element == null || element.equals(StructuredSelection.EMPTY)) {
			return null;
		}
		int size = 1;
		if (element instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) element;
			if (areDifferentTypes(structuredSelection)) {
				return structuredSelection.size() + " items selected";//$NON-NLS-1$
			}
			element = structuredSelection.getFirstElement();
			size = structuredSelection.size();
		}
		if (element instanceof EObject) {
			String split[] = getAdapterFactoryLabelProvider().getText(element)
				.split(" ");//$NON-NLS-1$
			StringBuffer type = new StringBuffer();
			StringBuffer name = new StringBuffer();
			for (int i = 0; i < split.length; i++) {
				if (i == 0) {
					type.append('\u00AB');
					type.append(split[i]);
					if (!(element instanceof PlayerStats || element instanceof GoalieStats)) {
						type.append('\u00BB');
					}
				} else if ((i == 1 && (element instanceof PlayerStats || element instanceof GoalieStats))) {
					type.append(' ');
					type.append(split[i]);
					type.append('\u00BB');
				} else {
					name.append(split[i]);
					name.append(' ');
				}
			}
			if (size == 1) {
				type.append(' ');
				type.append(name);
			} else {
				type.append(' ');
				type.append(Integer.toString(size));
				type.append(" selected");//$NON-NLS-1$
			}
			return type.toString();
		} else if (element instanceof Resource) {
			return "\u00ABResource\u00BB";//$NON-NLS-1$
		}
		return null;
	}

	/**
	 * @return
	 */
	private AdapterFactoryLabelProvider getAdapterFactoryLabelProvider() {
		if (adapterFactoryLabelProvider == null) {
			adapterFactoryLabelProvider = new AdapterFactoryLabelProvider(
				((HockeyleagueEditor) PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor()).getAdapterFactory());
		}
		return adapterFactoryLabelProvider;
	}

	/**
	 * Determine there are objects in the structured selection of different
	 * types.
	 * 
	 * @param structuredSelection
	 *            the structured selection.
	 * @return true if there are objects of different types in the selection.
	 */
	private boolean areDifferentTypes(IStructuredSelection structuredSelection) {
		if (structuredSelection.size() == 1) {
			return false;
		}
		Iterator i = structuredSelection.iterator();
		Object element = i.next();
		for (; i.hasNext();) {
			if (i.next().getClass() != element.getClass()) {
				return true;
			}
		}

		return false;
	}
}