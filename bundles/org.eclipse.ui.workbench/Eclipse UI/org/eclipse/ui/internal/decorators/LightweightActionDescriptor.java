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
package org.eclipse.ui.internal.decorators;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.ActionDescriptor;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.ui.internal.model.WorkbenchAdapter;
import org.eclipse.ui.model.*;
import org.eclipse.jface.resource.*;

/**
 * Represent the description of an action within
 * an action set. It does not create an action.
 *
 * [Issue: This class overlaps with ActionDescriptor
 *		and should be reviewed to determine if code
 *		reuse if possible.]
 */
public class LightweightActionDescriptor extends WorkbenchAdapter
	implements IAdaptable
{
	private String id;
	private String label;
	private String description;
	private ImageDescriptor image;
public LightweightActionDescriptor(IConfigurationElement actionElement) {
	super();

	this.id = actionElement.getAttribute(ActionDescriptor.ATT_ID);
	this.label = actionElement.getAttribute(ActionDescriptor.ATT_LABEL);
	this.description = actionElement.getAttribute(ActionDescriptor.ATT_DESCRIPTION);

	String iconName = actionElement.getAttribute(ActionDescriptor.ATT_ICON);
	if (iconName != null) {
		this.image = WorkbenchImages.getImageDescriptorFromExtension(actionElement.getDeclaringExtension(), iconName);
	}
}
/**
 * Returns an object which is an instance of the given class
 * associated with this object. Returns <code>null</code> if
 * no such object can be found.
 */
public Object getAdapter(Class adapter) {
	if (adapter == IWorkbenchAdapter.class) 
		return this;
	return null;
}
/**
 * Returns the action's description.
 */
public String getDescription() {
	return description;
}
/**
 * Returns the action's id.
 */
public String getId() {
	return id;
}
/**
 * Returns the action's image descriptor.
 */
public ImageDescriptor getImageDescriptor() {
	return image;
}
/**
 * @see IWorkbenchAdapter#getImageDescriptor
 */
public ImageDescriptor getImageDescriptor(Object o) {
	if (o == this)
		return getImageDescriptor();
	return super.getImageDescriptor(o);
}
/**
 * Returns the action's label.
 */
public String getLabel() {
	return label;
}
/**
 * @see IWorkbenchAdapter#getLabel
 */
public String getLabel(Object o) {
	if (o == this) {
		String text = getLabel();
		int end = text.lastIndexOf('@');
		if(end >= 0)
			text = text.substring(0, end);
		return DialogUtil.removeAccel(text);		
	}
	return super.getLabel(o);
}
}
