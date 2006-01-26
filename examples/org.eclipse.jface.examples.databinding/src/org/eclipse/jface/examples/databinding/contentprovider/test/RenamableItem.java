/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.contentprovider.test;

import java.util.ArrayList;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;

/**
 * @since 3.2
 *
 */
public class RenamableItem {
	private String name;
	private ArrayList listeners = new ArrayList();

	public RenamableItem() {
		name = "RenamableItem";
	}
	
	public void addListener(IChangeListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(IChangeListener toRemove) {
		listeners.remove(toRemove);
	}
	
	public void setName(String newName) {
		this.name = newName;
		
		ChangeEvent e = new ChangeEvent(this, ChangeEvent.DIRTY, null, null);
		
		IChangeListener[] l = (IChangeListener[]) listeners.toArray(new IChangeListener[listeners.size()]);
		for (int i = 0; i < l.length; i++) {
			IChangeListener listener = l[i];
			
			listener.handleChange(e);
		}
	}
	
	public String getName() {
		return name;
	}
}
