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
package org.eclipse.team.internal.ui.sync.compare;

import java.io.InputStream;

import org.eclipse.compare.BufferedContent;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;

/**
 * RemoteResourceTypedElement
 */
public class RemoteResourceTypedElement extends BufferedContent implements ITypedElement, IEditableContent {

	private IRemoteResource remote;
	private boolean editable;

	/**
	 * Creates a new content buffer for the given team node.
	 */
	RemoteResourceTypedElement(IRemoteResource remote) {
		this.remote = remote;
		this.editable = false;
	}

	public Image getImage() {
		return CompareUI.getImage(getType());
	}

	public String getName() {
		return remote.getName();
	}

	public String getType() {
		if (remote.isContainer()) {
			return ITypedElement.FOLDER_TYPE;
		}
		String name = getName();
		if (name != null) {
			int index = name.lastIndexOf('.');
			if (index == -1)
				return ""; //$NON-NLS-1$
			if (index == (name.length() - 1))
				return ""; //$NON-NLS-1$
			return name.substring(index + 1);
		}
		return ITypedElement.FOLDER_TYPE;		
	}

	/**
	 * Returns true if this object can be modified.
	 * If it returns <code>false</code> the other methods must not be called.
	 * 
	 * @return <code>true</code> if this object can be modified.
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * This is not the definitive API!
	 * This method is called on a parent to
	 * - add a child,
	 * - remove a child,
	 * - copy the contents of a child
	 * 
	 * What to do is encoded in the two arguments as follows:
	 * add:	child == null		other != null
	 * remove:	child != null		other == null
	 * copy:	child != null		other != null
	 */
	public ITypedElement replace(ITypedElement child, ITypedElement other) {
		return null;
	}

	/* (non-Javadoc)
	 * @see BufferedContent#createStream()
	 */
	protected InputStream createStream() throws CoreException {
		if (remote != null && !remote.isContainer()) {
			try {
				return remote.getContents(new NullProgressMonitor());
			} catch (TeamException exception) {
				// The remote resource has gone.
				return null;
			}
		}
		return null;
	}
}