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

import org.eclipse.ui.internal.registry.*;
import java.util.*;

/**
 * A fake action set category for the action set dialog.
 */
public class FakeActionSetCategory extends ActionSetCategory {
	private HashMap map = new HashMap(10);
/**
 * FakeActionSetCategory constructor comment.
 * @param id java.lang.String
 * @param label java.lang.String
 */
public FakeActionSetCategory(String id, String label) {
	super(id, label);
}
/**
 * Adds an action set.
 */
public void addActionSet(IActionSetDescriptor desc) {
	super.addActionSet(desc);
	map.put(desc.getId(), desc);
}
/**
 * Returns the action set with a given id.
 */
public IActionSetDescriptor findActionSet(String id) {
	return (IActionSetDescriptor)map.get(id);
}
}
