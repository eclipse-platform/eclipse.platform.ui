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

package org.eclipse.core.internal.dependencies;

import java.util.HashMap;
import java.util.Map;

public class ResolutionDelta implements IResolutionDelta {
	private Map changes;
	ResolutionDelta() {
		this.changes = new HashMap();
	}
	public IElementChange[] getAllChanges() {
		return (IElementChange[]) changes.values().toArray(new IElementChange[changes.size()]);
	}
	public IElementChange getChange(Object id, Object versionId) {
		return (IElementChange) changes.get(new ElementIdentifier(id, versionId));
	}
	class ElementIdentifier {
		private Object id;
		private Object versionId;
		ElementIdentifier(Object id, Object versionId) {
			this.id = id;
			this.versionId = versionId;
		}
		public int hashCode() {
			return (id.hashCode() << 16) | (versionId.hashCode() & 0xFFFF);
		}
		public boolean equals(Object anObject) {
			if (!(anObject instanceof ElementIdentifier))
				return false;
			ElementIdentifier change = (ElementIdentifier) anObject;
			return change.id.equals(this.id) && change.versionId.equals(this.versionId);
		}
	}
	/**
	 * Record a new status change.
	 */
	void recordChange(IElement element, int kind) {
		// check if a change has already been recorded for the element
		ElementChange existingChange = (ElementChange) this.getChange(element.getId(), element.getVersionId());
		// if not, just record it and we are done
		if (existingChange == null) {
			this.changes.put(new ElementIdentifier(element.getId(), element.getVersionId()), new ElementChange(element, kind));
			return;
		}

		// does the new change cancel the previous one? 
		if (existingChange.getPreviousStatus() == (kind & 0x0F)) {
			// if so, just remove the change object
			this.changes.remove(new ElementIdentifier(element.getId(), element.getVersionId()));
			return;
		}
		// otherwise, just update the new status for the existing change object 
		existingChange.setKind((existingChange.getKind() & 0xF0) | kind & 0x0F);
	}
	public String toString() {
		return changes.values().toString();
	}
}
