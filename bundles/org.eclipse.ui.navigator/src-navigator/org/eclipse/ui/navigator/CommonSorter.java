/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.navigator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptorRegistry;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentExtension;

// TODO Fix up Common Sorter

/**
 * <p>
 * Provides generic, extensible sorting strategies for the Common Navigator.
 * </p>
 * <p>
 * The following class is experimental until fully documented.
 * </p>
 */
public class CommonSorter extends ViewerSorter {

	private static final NavigatorContentDescriptorRegistry CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorRegistry.getInstance();
 
	private Comparator comparator = null;
	private NavigatorContentService contentService;

	/**
	 * 
	 */
	public CommonSorter(NavigatorContentService aContentService) {
		super();
		contentService = aContentService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
	 */
	public int category(Object anElement) {
		List descriptors = CONTENT_DESCRIPTOR_REGISTRY.getEnabledContentDescriptors(anElement);
		if (descriptors.size() > 0)
			return ((NavigatorContentDescriptor) descriptors.get(0)).getPriority();
		return Priority.NORMAL_PRIORITY_VALUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object lvalue, Object rvalue) {
		int rank = category(rvalue) - category(lvalue);
		if(rank == 0) { 
			IStructuredSelection selection = new StructuredSelection(new Object[] {lvalue, rvalue});
			NavigatorContentExtension[] descriptorInstances = contentService.findRelevantContentExtensions(selection);
			if(descriptorInstances.length > 0)
				return descriptorInstances[0].getComparator().compare(lvalue, rvalue);
			return rank;
			
		}
		return rank;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerSorter#sort(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object[])
	 */
	public void sort(Viewer viewer, Object[] elements) {
		Arrays.sort(elements, getComparator());
	}
	 

	protected Comparator getComparator() {
		if (comparator == null) {
			comparator = new Comparator() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
				 */
				public int compare(Object o1, Object o2) {
					return CommonSorter.this.compare(null, o1, o2);
				}
			};
		}
		return comparator;
	}


}