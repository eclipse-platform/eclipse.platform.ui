/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.operations;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.mapping.ResourceMapping;

/**
 * @deprecated use {@link org.eclipse.team.core.mapping.MergeStatus}
 */
public class MergeStatus extends org.eclipse.team.core.mapping.MergeStatus {

	public MergeStatus(String pluginId, String message, ResourceMapping[] conflictingMappings) {
		super(pluginId, message, conflictingMappings);
	}

	public MergeStatus(String pluginId, String message, IFile[] files) {
		super(pluginId, message, files);
	}
	
	

}
