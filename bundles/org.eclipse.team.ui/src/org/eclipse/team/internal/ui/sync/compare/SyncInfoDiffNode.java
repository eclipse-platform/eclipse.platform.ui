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

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.sync.IRemoteResource;

public class SyncInfoDiffNode extends DiffNode {
		
		/**
		 * Creates a new file node.
		 */	
		public SyncInfoDiffNode(ITypedElement base, ITypedElement local, ITypedElement remote, int syncKind) {
			super(syncKind, base, local, remote);
		}

		/**
		 * Returns the typed element for this sync element. The returned elements
		 * are [0] base, [1] local, [2] remote.
		 */
		static public ITypedElement[] getTypedElements(final SyncInfo sync) {
			IRemoteResource baseResource = sync.getBase();
			IRemoteResource remoteResource = sync.getRemote();
			IResource localResource = sync.getLocal();
			
			ITypedElement te[] = new ITypedElement[3];
			
			if(baseResource != null) {
				te[0] = new RemoteResourceTypedElement(baseResource);
			}
			if(remoteResource != null) {
				te[2] = new RemoteResourceTypedElement(remoteResource);
			}
			
			if(localResource != null && localResource.exists()) {
				te[1] = new LocalResourceTypedElement(localResource) {
				public boolean isEditable() {
						int kind = sync.getKind();
						if(SyncInfo.getDirection(kind) == SyncInfo.OUTGOING && SyncInfo.getChange(kind) == SyncInfo.DELETION) {
							return false;
						}
						return super.isEditable();
					}
				};
			}
			return te;
		}
}
