/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.cdt;

import java.util.Comparator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * A sorter to sort the file and the folders in the C viewer in the following
 * order: 1 Project 2 BinaryContainer 3 ArchiveContainer 4 LibraryContainer 5
 * IncludeContainer 6 Source roots 5 C Elements 6 non C Elements
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CElementSorter extends ViewerSorter {

	protected static final int CMODEL = 0;
	protected static final int PROJECTS = 10;
	protected static final int BINARYCONTAINER = 12;
	protected static final int ARCHIVECONTAINER = 13;
	protected static final int INCLUDEREFCONTAINER = 14;
	protected static final int LIBRARYREFCONTAINER = 15;
	protected static final int SOURCEROOTS = 16;
	protected static final int CCONTAINERS = 17;
	protected static final int LIBRARYREFERENCES = 18;
	protected static final int INCLUDEREFERENCES = 19;
	protected static final int TRANSLATIONUNIT_HEADERS = 20;
	protected static final int TRANSLATIONUNIT_SOURCE = 21;
	protected static final int TRANSLATIONUNITS = 22;
	protected static final int BINARIES = 23;
	protected static final int ARCHIVES = 24;

	protected static final int INCLUDES = 28;
	protected static final int MACROS = 29;
	protected static final int USINGS = 30;
	protected static final int NAMESPACES = 32;
	protected static final int NAMESPACES_RESERVED = 33;
	protected static final int NAMESPACES_SYSTEM = 34;
	/**
	 * @since 5.1
	 */
	protected static final int TYPES = 35;
	protected static final int VARIABLEDECLARATIONS = 36;
	protected static final int FUNCTIONDECLARATIONS = 37;
	protected static final int VARIABLES = 38;
	protected static final int VARIABLES_RESERVED = 39;
	protected static final int VARIABLES_SYSTEM = 40;
	protected static final int FUNCTIONS = 41;
	protected static final int FUNCTIONS_RESERVED = 42;
	protected static final int FUNCTIONS_SYSTEM = 43;
	protected static final int METHODDECLARATIONS = 44;

	protected static final int CELEMENTS = 100;
	protected static final int CELEMENTS_RESERVED = 101;
	protected static final int CELEMENTS_SYSTEM = 102;

	protected static final int RESOURCEFOLDERS = 200;
	protected static final int RESOURCES = 201;
	protected static final int STORAGE = 202;
	protected static final int OTHERS = 500;

	/**
	 * Default constructor for use as executable extension.
	 */
	public CElementSorter() {
	}

	@Override
	public int category(Object element) {
		if (element instanceof CElement) {
			if (element instanceof CRoot)
				return CMODEL;
			else if (element instanceof CProject)
				return PROJECTS;
			else if (element instanceof CContainer)
				return CCONTAINERS;
			else
				return TYPES;
		} else if (element instanceof IResource) {
			IResource resource = (IResource) element;
			switch (resource.getType()) {
			case IResource.PROJECT:
				return PROJECTS;
			case IResource.FOLDER:
				return RESOURCEFOLDERS;
			default:
				return RESOURCES;
			}
		} else if (element instanceof IStorage) {
			return STORAGE;
		}
		return OTHERS;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int cat1 = category(e1);
		int cat2 = category(e2);

		if (cat1 != cat2)
			return cat1 - cat2;

		// cat1 == cat2

		final Comparator comparator = getComparator();
		if (cat1 == PROJECTS) {
			IWorkbenchAdapter a1 = (IWorkbenchAdapter) ((IAdaptable) e1)
					.getAdapter(IWorkbenchAdapter.class);
			IWorkbenchAdapter a2 = (IWorkbenchAdapter) ((IAdaptable) e2)
					.getAdapter(IWorkbenchAdapter.class);
			return comparator.compare(a1.getLabel(e1), a2.getLabel(e2));
		}

		// non - c resources are sorted using the label from the viewers label
		// provider
		if (cat1 == RESOURCES || cat1 == RESOURCEFOLDERS || cat1 == STORAGE
				|| cat1 == OTHERS) {
			return compareWithLabelProvider(viewer, e1, e2);
		}

		String ns1 = ""; //$NON-NLS-1$
		String ns2 = ns1;

		String name1;
		String name2;

		if (e1 instanceof CElement) {
			name1 = ((CElement) e1).getElementName();
			int idx = name1.lastIndexOf("::"); //$NON-NLS-1$
			if (idx >= 0) {
				ns1 = name1.substring(0, idx);
				name1 = name1.substring(idx + 2);
			}
			if (name1.length() > 0 && name1.charAt(0) == '~') {
				name1 = name1.substring(1);
			}
		} else {
			name1 = e1.toString();
		}
		if (e2 instanceof CElement) {
			name2 = ((CElement) e2).getElementName();
			int idx = name2.lastIndexOf("::"); //$NON-NLS-1$
			if (idx >= 0) {
				ns2 = name2.substring(0, idx);
				name2 = name2.substring(idx + 2);
			}
			if (name2.length() > 0 && name2.charAt(0) == '~') {
				name2 = name2.substring(1);
			}
		} else {
			name2 = e2.toString();
		}

		// compare namespace
		int result = comparator.compare(ns1, ns2);
		if (result != 0) {
			return result;
		}

		if (result != 0) {
			return result;
		}

		// compare simple name
		result = comparator.compare(name1, name2);
		if (result != 0) {
			return result;
		}
		return result;
	}

	private int compareWithLabelProvider(Viewer viewer, Object e1, Object e2) {
		if (viewer instanceof ContentViewer) {
			IBaseLabelProvider prov = ((ContentViewer) viewer)
					.getLabelProvider();
			if (prov instanceof ILabelProvider) {
				ILabelProvider lprov = (ILabelProvider) prov;
				String name1 = lprov.getText(e1);
				String name2 = lprov.getText(e2);
				if (name1 != null && name2 != null) {
					final Comparator comparator = getComparator();
					return comparator.compare(name1, name2);
				}
			}
		}
		return 0; // can't compare
	}

}
