/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.extensions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;

/**
 * 
 * Drag assistants allow clients to provide new TransferTypes to a particular
 * viewer.
 * 
 * @since 3.2
 * 
 */
public final class CommonDragAssistantDescriptor implements IViewerExtPtConstants {

	private IConfigurationElement element;

	
	/* package */ CommonDragAssistantDescriptor(IConfigurationElement aConfigElement) {
		element = aConfigElement;
	}

	/**
	 * Create an instance of the {@link CommonDragAdapterAssistant} defined by
	 * this descriptor.
	 * 
	 * @return an instance of the {@link CommonDragAdapterAssistant} or
	 *         {@link SkeletonCommonDragAssistant} if a problem occurs with the
	 *         instantiation.
	 */
	public CommonDragAdapterAssistant createDragAssistant() {

		try {
			return (CommonDragAdapterAssistant) element
					.createExecutableExtension(ATT_CLASS);
		} catch (CoreException e) {
			NavigatorPlugin.logError(0, e.getMessage(), e);
		} catch (RuntimeException re) {
			NavigatorPlugin.logError(0, re.getMessage(), re);
		}
		return SkeletonCommonDragAssistant.INSTANCE;

	}

}
