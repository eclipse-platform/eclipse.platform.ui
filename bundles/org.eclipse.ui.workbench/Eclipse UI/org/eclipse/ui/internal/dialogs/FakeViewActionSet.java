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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.registry.IViewDescriptor;

/**
 * A fake view action set.
 */
public class FakeViewActionSet extends FakeActionSetDescriptor {
/**
 * Constructs a new action set.
 */
public FakeViewActionSet(IViewDescriptor desc) {
	super(desc.getID(), desc);
}
/**
 * Returns the action image descriptor.
 */
protected ImageDescriptor getActionImageDescriptor() {
	return getView().getImageDescriptor();
}
/**
 * Returns the action text.
 */
protected String getActionLabel() {
	return getView().getLabel();
}
/**
 * Returns the descriptor
 */
public IViewDescriptor getView() {
	return (IViewDescriptor)getData();
}
}
