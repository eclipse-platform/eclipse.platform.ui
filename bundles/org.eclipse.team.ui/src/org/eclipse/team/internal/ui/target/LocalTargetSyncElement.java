/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ui.target;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.LocalSyncElement;

public class LocalTargetSyncElement extends LocalSyncElement {

	private IResource local;
	
	public LocalTargetSyncElement(IResource local) {
		this.local = local;
	}
	
	/**
	 * @see LocalSyncElement#create(IResource, IRemoteResource, Object)
	 */
	public ILocalSyncElement create(IResource local, IRemoteResource base, Object data) {
		return new LocalTargetSyncElement(local);
	}

	/**
	 * @see LocalSyncElement#getData()
	 */
	protected Object getData() {
		return null;
	}

	/**
	 * @see LocalSyncElement#isIgnored(IResource)
	 */
	protected boolean isIgnored(IResource resource) {
		return false;
	}

	/**
	 * @see ILocalSyncElement#getLocal()
	 */
	public IResource getLocal() {
		return local;
	}

	/**
	 * @see ILocalSyncElement#getBase()
	 */
	public IRemoteResource getBase() {
		return null;
	}

	/**
	 * @see ILocalSyncElement#isCheckedOut()
	 */
	public boolean isCheckedOut() {
		return false;
	}

	/**
	 * @see ILocalSyncElement#hasRemote()
	 */
	public boolean hasRemote() {
		return false;
	}
}
