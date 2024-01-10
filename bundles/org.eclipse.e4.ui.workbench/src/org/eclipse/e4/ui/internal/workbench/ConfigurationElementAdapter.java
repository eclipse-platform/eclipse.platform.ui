/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 460405
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.ui.workbench.modeling.ModelHandlerBase;

public class ConfigurationElementAdapter extends ModelHandlerBase implements IAdapterFactory {

	public static final String CLASS_IMPL = "classImpl"; //$NON-NLS-1$

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType.isInstance(this)) {
			return adapterType.cast(this);
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { ModelHandlerBase.class };
	}

	@Override
	public Object[] getChildren(Object element, String id) {
		IConfigurationElement ice = (IConfigurationElement) element;
		return ice.getChildren(id);
	}

	@Override
	public Object getProperty(Object element, String id) {
		IConfigurationElement ice = (IConfigurationElement) element;

		// Construct a meaningful 'label'
		if ("label".equals(id)) { //$NON-NLS-1$
			String idVal = ice.getAttribute("id"); //$NON-NLS-1$
			String nameVal = ice.getAttribute("name"); //$NON-NLS-1$

			String constructedName = Util.ZERO_LENGTH_STRING;
			if (nameVal != null) {
				constructedName = nameVal;
				if (idVal != null)
					constructedName += " [" + idVal + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			} else if (idVal != null) {
				constructedName = idVal;
			} else
				constructedName = ice.getName();

			return constructedName;
		} else if (CLASS_IMPL.equals(id)) {
			try {
				return ice.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return null;
		}

		return ice.getAttribute(id);
	}

	@Override
	public String[] getPropIds(Object element) {
		IConfigurationElement ice = (IConfigurationElement) element;
		return ice.getAttributeNames();
	}

	@Override
	public void setProperty(Object element, String id, Object value) {
		super.setProperty(element, id, value);
	}
}
