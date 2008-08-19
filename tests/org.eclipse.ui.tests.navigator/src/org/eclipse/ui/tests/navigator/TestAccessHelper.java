/*******************************************************************************
 * Copyright (c) 2008 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.navigator;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.internal.navigator.actions.CommonActionDescriptorManager;
import org.eclipse.ui.internal.navigator.actions.CommonActionProviderDescriptor;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.NavigatorActionService;

/**
 * Helper used to access things from the CN implementation.
 */
public class TestAccessHelper {

	
	public static CommonActionProvider getActionProvider(
			INavigatorContentService contentService,
			NavigatorActionService actionService, Class cls) throws Exception {

		CommonActionProvider provider = null;
		CommonActionProviderDescriptor[] providerDescriptors = CommonActionDescriptorManager
				.getInstance().findRelevantActionDescriptors(contentService,
						new ActionContext(new StructuredSelection()));
		if (providerDescriptors.length > 0) {
			for (int i = 0; i < providerDescriptors.length; i++) {
				provider = actionService
						.getActionProviderInstance(providerDescriptors[i]);
				if (provider.getClass() == cls)
					return provider;
			}
		}
		return null;
	}

}
