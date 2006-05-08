/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.workbench;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 */
public class ResourceExtensionLabelProvider extends WorkbenchLabelProvider implements ICommonLabelProvider {
 
 
	public void init(ICommonContentExtensionSite aConfig) {
		//init
	}

 
	public String getDescription(Object anElement) {

		if (anElement instanceof IResource) {
			return ((IResource) anElement).getFullPath().makeRelative().toString();
		}
		return null;
	}

	public void restoreState(IMemento aMemento) { 
		
	}

	public void saveState(IMemento aMemento) { 
	}
}
