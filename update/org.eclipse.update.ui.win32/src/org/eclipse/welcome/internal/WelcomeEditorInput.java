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
package org.eclipse.welcome.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;
/**
 * A simple editor input for the welcome editor
 */
public class WelcomeEditorInput implements IEditorInput {
	private final static String FACTORY_ID = "org.eclipse.welcome.internal.WelcomeEditorInputFactory"; //$NON-NLS-1$
	private String url;
	private String name;
	/**
	 * WelcomeEditorInput constructor comment.
	 */
	public WelcomeEditorInput(String name, String url) {
		this.name = name;
		this.url = url;
	}
	public boolean exists() {
		return false;
	}
	public Object getAdapter(Class adapter) {
		return null;
	}
	public ImageDescriptor getImageDescriptor() {
		return null;
	}
	public String getName() {
		return name;
	}
	public String getURL() {
		return url;
	}
	public boolean equals(Object obj) {
		if (obj==null) return false;
		if (obj == this) return true;
		if (obj instanceof WelcomeEditorInput) {
			WelcomeEditorInput wobj = (WelcomeEditorInput)obj;
			return wobj.getURL().equals(getURL());
		}
		return false;
	}
	public IPersistableElement getPersistable() {
		return new IPersistableElement() {
			public String getFactoryId() {
				return FACTORY_ID;
			}
			public void saveState(IMemento memento) {
				memento.putString("name", name);
				memento.putString("url", url);
			}
		};
	}
	public String getToolTipText() {
		return name;
	}
}
