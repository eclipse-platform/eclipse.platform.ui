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
package org.eclipse.welcome.internal.portal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;
/**
 * A simple editor input for the welcome editor
 */
public class WelcomePortalEditorInput implements IEditorInput {
	private final static String FACTORY_ID = "org.eclipse.welcome.internal.portal.WelcomePortalEditorInputFactory"; //$NON-NLS-1$
	/**
	 * WelcomeEditorInput constructor comment.
	 */
	public WelcomePortalEditorInput() {
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
		return "Welcome";
	}
	public boolean equals(Object obj) {
		if (obj==null) return false;
		if (obj == this) return true;
		if (obj instanceof WelcomePortalEditorInput) {
			WelcomePortalEditorInput wobj = (WelcomePortalEditorInput)obj;
			return true;
		}
		return false;
	}
	public IPersistableElement getPersistable() {
		return new IPersistableElement() {
			public String getFactoryId() {
				return FACTORY_ID;
			}
			public void saveState(IMemento memento) {
			}
		};
	}
	public String getToolTipText() {
		return getName();
	}
}
