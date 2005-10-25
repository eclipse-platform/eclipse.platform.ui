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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.team.ui.mapping.ITeamViewerContext;

/**
 * A model content provider is a tree content provider that is used to display
 * specific elements of a logical model. The root of this content provider
 * will be the model provider from which the content provider was obtained.
 * 
 * TODO: decribe how to obtain a content provider from a model provider
 */
public interface IResourceMappingContentProvider extends ITreeContentProvider {

	/**
	 * Provide the content provider with additional context
	 * related to the team-based operation that owns this content provider.
	 * This method must be invoked by clients before any other
	 * methods of this content provider is invoked.
	 * @param context a team context
	 */
	public void init(ITeamViewerContext context);
}
