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


class ElementChange implements IElementChange {
	private IElement element;
	private int kind;
	ElementChange(IElement element, int kind) {
		this.element = element;
		this.kind = kind;
	}
	public Object getVersionId() {
		return element.getVersionId();
	}
	public int getKind() {
		return kind;
	}
	public int getNewStatus() {
		return getKind() & 0x0F;
	}
	public int getPreviousStatus() {
		return getKind() >> 4;
	}
	public IElement getElement() {
		return element;
	}
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(element.getId());
		result.append('_');
		result.append(getVersionId());
		result.append(" ("); //$NON-NLS-1$
		result.append(getStatusName(getPreviousStatus()) + " >>>" + getStatusName(getNewStatus())); //$NON-NLS-1$
		result.append(')');
		return result.toString();
	}
	private String getStatusName(int status) {
		return (status == RESOLVED) ? "RESOLVED" : ((status == UNRESOLVED) ? "UNRESOLVED" : "UNKNOWN");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	void setKind(int kind) {
		this.kind = kind;
	}
}