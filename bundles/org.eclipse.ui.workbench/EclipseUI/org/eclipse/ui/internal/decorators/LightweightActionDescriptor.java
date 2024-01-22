/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548799
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Represent the description of an action within an action set. It does not
 * create an action.
 *
 * [Issue: This class overlaps with ActionDescriptor and should be reviewed to
 * determine if code reuse if possible.]
 */
public class LightweightActionDescriptor implements IAdaptable, IWorkbenchAdapter {
	private static final Object[] NO_CHILDREN = new Object[0];

	private String id;

	private String label;

	private String description;

	private ImageDescriptor image;

	/**
	 * Create a new instance of <code>LightweightActionDescriptor</code>.
	 *
	 * @param actionElement the configuration element
	 */
	public LightweightActionDescriptor(IConfigurationElement actionElement) {
		super();

		this.id = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		this.label = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
		this.description = actionElement.getAttribute(IWorkbenchRegistryConstants.TAG_DESCRIPTION);

		String iconName = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
		if (iconName != null) {
			IExtension extension = actionElement.getDeclaringExtension();
			ResourceLocator.imageDescriptorFromBundle(extension.getContributor().getName(), iconName)
					.ifPresent(d -> this.image = d);
		}
	}

	/**
	 * Returns an object which is an instance of the given class associated with
	 * this object. Returns <code>null</code> if no such object can be found.
	 */
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return adapter.cast(this);
		}
		return null;
	}

	/**
	 * Returns the action's description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the action's id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the action's image descriptor.
	 *
	 * @return the image descriptor
	 */
	public ImageDescriptor getImageDescriptor() {
		return image;
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object o) {
		if (o == this) {
			return getImageDescriptor();
		}
		return null;
	}

	/**
	 * Returns the action's label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	@Override
	public String getLabel(Object o) {
		if (o == this) {
			String text = getLabel();
			int end = text.lastIndexOf('@');
			if (end >= 0) {
				text = text.substring(0, end);
			}
			return LegacyActionTools.removeMnemonics(text);
		}
		return o == null ? "" : o.toString();//$NON-NLS-1$
	}

	@Override
	public Object[] getChildren(Object o) {
		return NO_CHILDREN;
	}

	@Override
	public Object getParent(Object o) {
		return null;
	}
}
