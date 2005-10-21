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
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.team.ui.mapping.ITeamViewerContext;

/**
 * Factory for creating a NavigatorContentProvider for
 * a given team context.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public interface INavigatorContentExtensionFactory {

	public NavigatorContentExtension createProvider(ITeamViewerContext context);
	
	/**
	 * TODO: Should not need this but I added it to make it work
	 */
	public ILabelProvider getLabelProvider();
}
