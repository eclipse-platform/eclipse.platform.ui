/**********************************************************************
 * Copyright (c) 2003, 2004 Geoff Longman and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * Geoff Longman - Initial API and implementation
 * IBM - Tightening integration with existing Platform
 **********************************************************************/
package org.eclipse.core.tools.resources.markers;

import java.util.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.tools.resources.CoreResourcesToolsPlugin;
import org.eclipse.ui.views.properties.*;

/**
 * A read-only IPropertySource for Marker attributes.
 */
public class ReadOnlyMarkerPropertySource implements IPropertySource {

	private PropertyDescriptor persistentDescriptor = new PropertyDescriptor("persistent", "persistent");
	protected MarkerExtensionModel model;
	protected MarkerView view;
	protected IMarker marker;
	protected MarkerExtensionModel.MarkerInfo info;

	public ReadOnlyMarkerPropertySource(MarkerView view, MarkerExtensionModel model) {
		super();
		this.model = model;
		this.view = view;
	}

	/**
	 * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
	 */
	public Object getEditableValue() {
		return null;
	}

	/**
	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		ArrayList descriptors = new ArrayList();
		findPropertyDescriptors(descriptors);
		return (IPropertyDescriptor[]) descriptors.toArray(new IPropertyDescriptor[descriptors.size()]);
	}

	private void findPropertyDescriptors(ArrayList descriptorList) {
		try {
			Set attributesWithoutDescriptors = new HashSet(marker.getAttributes().keySet());
			findDeclaredPropertyDescriptorsFor(info, descriptorList, attributesWithoutDescriptors);
			if (!attributesWithoutDescriptors.isEmpty()) {
				// we have extra, undeclared attributes.
				// we will create the correct property descriptor
				for (Iterator iter = attributesWithoutDescriptors.iterator(); iter.hasNext();) {
					String name = (String) iter.next();
					PropertyDescriptor desc = new PropertyDescriptor(name, name);
					desc.setCategory("undeclared attributes");
					descriptorList.add(desc);
				}
			}
		} catch (CoreException e) {
			// ignore
		}
	}

	private void findDeclaredPropertyDescriptorsFor(MarkerExtensionModel.MarkerInfo anInfo, ArrayList descriptorList, Set actualAttributeSet) {
		if (anInfo == null)
			return;
		try {
			if (anInfo.id.equals(marker.getType())) {
				persistentDescriptor.setCategory(anInfo.id);
				descriptorList.add(persistentDescriptor);
			}
		} catch (CoreException e) {
			// ignore
		}
		for (Iterator iter = anInfo.declaredAttributes.iterator(); iter.hasNext();) {
			String attr = (String) iter.next();
			PropertyDescriptor desc = new PropertyDescriptor(attr, attr);
			desc.setCategory(anInfo.id);
			descriptorList.add(desc);
			actualAttributeSet.remove(attr);
		}
		for (Iterator iter = anInfo.declaredSupers.iterator(); iter.hasNext();) {
			String superId = (String) iter.next();
			MarkerExtensionModel.MarkerInfo superInfo = model.getInfo(superId);
			if (superInfo == null) {
				CoreResourcesToolsPlugin.logProblem("internal error. could not find supertype" + superId + "of marker" + info.id, IStatus.ERROR);
				continue;
			}
			findDeclaredPropertyDescriptorsFor(superInfo, descriptorList, actualAttributeSet);
		}
	}

	/**
	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(Object)
	 */
	public Object getPropertyValue(Object id) {
		String name = (String) id;
		if ("persistent".equals(name))
			return info.persistent ? Boolean.TRUE : Boolean.FALSE;
		try {
			return marker.getAttribute(name);
		} catch (CoreException e) {
			CoreResourcesToolsPlugin.logProblem(e);
			return "exception occured accessing: " + name;
		}
	}

	/**
	 * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(Object)
	 */
	public boolean isPropertySet(Object id) {
		String name = (String) id;
		if ("persistent".equals(name))
			return info != null;
		try {
			return marker.getAttribute(name) != null;
		} catch (CoreException e) {
			CoreResourcesToolsPlugin.logProblem(e);
			return false;
		}
	}

	/**
	 * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(Object)
	 */
	public void resetPropertyValue(Object id) {
		// do nothing
	}

	/**
	 * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(Object, Object)
	 */
	public void setPropertyValue(Object id, Object value) {
		// do nothing
	}

	/**
	 * sets the Marker this source will act on behalf of
	 */
	public void setSourceMarker(IMarker marker) {
		this.marker = marker;
		this.info = null;
		try {
			this.info = model.getInfo(marker.getType());
		} catch (CoreException e) {
			CoreResourcesToolsPlugin.logProblem(e);
		}
	}
}