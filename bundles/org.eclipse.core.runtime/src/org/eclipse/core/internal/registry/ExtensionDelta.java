/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import org.eclipse.core.runtime.*;

public class ExtensionDelta implements IExtensionDelta {
	private int kind;
	private int extension;
	private int extensionPoint;
	private RegistryDelta containingDelta;

	void setContainingDelta(RegistryDelta containingDelta) {
		this.containingDelta = containingDelta;
	}
	int getExtensionId() {
		return extension;
	}
	
	int getExtensionPointId() {
		return extensionPoint;
	}
	
	public IExtensionPoint getExtensionPoint() {
		return new ExtensionPointHandle(containingDelta.getObjectManager(), extensionPoint);
	}

	public void setExtensionPoint(int extensionPoint) {
		this.extensionPoint = extensionPoint;
	}

	public int getKind() {
		return kind;
	}

	public IExtension getExtension() {
		return new ExtensionHandle(containingDelta.getObjectManager(), extension);
	}

	public void setExtension(int extension) {
		this.extension = extension;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public String toString() {
		return "\n\t\t" + getExtensionPoint().getUniqueIdentifier() + " - " + getExtension().getNamespace() + '.' + getExtension().getSimpleIdentifier() + " (" + getKindString(this.getKind()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$		
	}

	public static String getKindString(int kind) {
		switch (kind) {
			case ADDED :
				return "ADDED"; //$NON-NLS-1$
			case REMOVED :
				return "REMOVED"; //$NON-NLS-1$
		}
		return "UNKNOWN"; //$NON-NLS-1$
	}
}