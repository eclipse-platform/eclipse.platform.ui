/*****************************************************************
 * Copyright (c) 2009, 2013 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial API and implementation (Bug 286310)
 *     IBM Corporation - bug fixing
 *****************************************************************/
package org.eclipse.debug.examples.ui.midi.adapters;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.internal.ui.viewers.model.provisional.ICheckboxModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.jface.viewers.TreePath;

public class MidiEventModelProxy extends AbstractModelProxy implements ICheckboxModelProxy {
	static Map gChecked = new HashMap(); 
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ICheckboxModelProxy#setChecked(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object, org.eclipse.jface.viewers.TreePath, boolean)
	 */
	public boolean setChecked(IPresentationContext context, Object viewerInput, TreePath path, boolean checked) {
		System.out.println("TrackCheckListener.setChecked() element = " + path.getLastSegment() + " checked = " + checked); //$NON-NLS-1$ //$NON-NLS-2$
		gChecked.put(path, Boolean.valueOf(checked));
		return true;
	}

}
