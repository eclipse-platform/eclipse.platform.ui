/*******************************************************************************
 * Copyright (c) 2025 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.osgi.framework.Bundle;

/**
 * Contribution factory that uses a delegate and additionally provides behavior
 * from the {@link Workbench} services.
 *
 */
@SuppressWarnings("restriction")
class WorkbenchContributionFactory implements IContributionFactory {

	private static final String BUNDLE_CLASS_PREFIX = "bundleclass://"; //$NON-NLS-1$

	private final IContributionFactory delegate;

	private final IEclipseContext context;

	private IWorkbenchActivitySupport activitySupport;

	WorkbenchContributionFactory(Workbench workbench) {
		context = workbench.getApplication().getContext();
		delegate = context.get(IContributionFactory.class);
	}

	@Override
	public Object create(String uriString, IEclipseContext context) {
		return delegate.create(uriString, context);
	}

	@Override
	public Object create(String uriString, IEclipseContext context, IEclipseContext staticContext) {
		return delegate.create(uriString, context, staticContext);
	}

	@Override
	public Bundle getBundle(String uriString) {
		return delegate.getBundle(uriString);
	}

	@Override
	public boolean isEnabled(String uriString) {
		if (uriString != null && uriString.startsWith(BUNDLE_CLASS_PREFIX)) {
			String identifierId = uriString.substring(BUNDLE_CLASS_PREFIX.length());
			if (activitySupport == null) {
				activitySupport = context.get(IWorkbenchActivitySupport.class);
			}
			IIdentifier identifier = activitySupport.getActivityManager().getIdentifier(identifierId);
			if (!identifier.isEnabled()) {
				return false;
			}
		}
		return delegate.isEnabled(uriString);
	}

}
