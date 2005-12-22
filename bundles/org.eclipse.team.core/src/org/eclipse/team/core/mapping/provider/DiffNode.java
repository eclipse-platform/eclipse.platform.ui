/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.mapping.provider;

import org.eclipse.core.runtime.IPath;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.internal.core.Messages;

/**
 * Abstract implementation of {@link IDiffNode} that can be subclassed by
 * clients.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see ITwoWayDiff
 * @see IThreeWayDiff
 * @since 3.2
 */
public abstract class DiffNode implements IDiffNode {

	/**
	 * Constant (bit mask) that defines the area of the status that is reserved
	 * for use by this abstract class for encoding the kind of the diff.
	 * 
	 * @see #getStatus()
	 */
	public static final int KIND_MASK = 0xFF;

	private final IPath path;

	private final int status;

	/**
	 * Create a diff node.
	 * 
	 * @param path the path of the diff
	 * @param status the status of the diff. The kind should be encoded in the
	 *            status along with any additional flags required by a subclass.
	 */
	protected DiffNode(IPath path, int status) {
		this.path = path;
		this.status = status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.synchronize.ISyncDelta#getFullPath()
	 */
	public IPath getPath() {
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.synchronize.ISyncDelta#getKind()
	 */
	public int getKind() {
		return getStatus() & KIND_MASK;
	}

	/**
	 * Return the status of the diff node. The status is a bit field that
	 * contains the kind and any additional status information that subclasses
	 * need to encode. The first byte of the status is reserved for use by this
	 * abstract class as indicated by the <code>KIND_MASK</code>.
	 * 
	 * @return the status of the diff node
	 */
	public final int getStatus() {
		return status;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IDiffNode#toDiffString()
	 */
	public String toDiffString() {
		int kind = getKind();
		String label = ""; //$NON-NLS-1$
		if(kind==IDiffNode.NO_CHANGE) {
			label = Messages.RemoteSyncElement_insync; 
		} else {
			switch(kind) {
				case IDiffNode.CHANGE: label = Messages.RemoteSyncElement_change ; break;
				case IDiffNode.ADD: label = Messages.RemoteSyncElement_addition; break;
				case IDiffNode.REMOVE: label = Messages.RemoteSyncElement_deletion; break; 
			}
		}
		return label; 
	}
}
