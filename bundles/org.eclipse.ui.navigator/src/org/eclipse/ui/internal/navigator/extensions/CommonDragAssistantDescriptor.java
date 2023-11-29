/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.extensions;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ui.internal.navigator.NavigatorSafeRunnable;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;

/**
 *
 * Drag assistants allow clients to provide new TransferTypes to a particular
 * viewer.
 *
 * @since 3.2
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

		final CommonDragAdapterAssistant[] da = new CommonDragAdapterAssistant[1];

		SafeRunner.run(new NavigatorSafeRunnable(element) {
			@Override
			public void run() throws Exception {
				da[0] = (CommonDragAdapterAssistant) element.createExecutableExtension(ATT_CLASS);
			}
		});
		if (da[0] != null)
			return da[0];
		return SkeletonCommonDragAssistant.INSTANCE;

	}

}
