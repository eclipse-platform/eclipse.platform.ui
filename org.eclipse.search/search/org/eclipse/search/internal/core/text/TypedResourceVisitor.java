/*
 * (c) Copyright IBM Corp. 2000, 2003.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.core.text;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;

import org.eclipse.jface.util.Assert;

public class TypedResourceVisitor implements IResourceProxyVisitor {

	private MultiStatus fStatus;

	TypedResourceVisitor(MultiStatus status) {
		Assert.isNotNull(status);
		fStatus= status;
	}
	
	protected boolean visitFile(IResourceProxy proxy) throws CoreException {
		return true;
	}

	protected void addToStatus(CoreException ex) {
		fStatus.add(ex.getStatus());
	}

	/* 
	 * @see org.eclipse.core.resources.IResourceProxyVisitor#visit(org.eclipse.core.resources.IResourceProxy)
	 */
	public boolean visit(IResourceProxy proxy) {
		try {
			switch(proxy.getType()) {
				case IResource.FILE:
					return visitFile(proxy);
				case IResource.FOLDER:
					return true;
				case IResource.PROJECT:
					return true;
				default:
					Assert.isTrue(false, "unknown resource type"); //$NON-NLS-1$
			}
			return false;
		} catch (CoreException ex) {
			addToStatus(ex);
			return false;
		}
	}
}