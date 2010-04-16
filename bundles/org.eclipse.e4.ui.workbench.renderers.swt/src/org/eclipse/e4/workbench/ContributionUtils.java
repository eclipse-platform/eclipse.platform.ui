/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.workbench;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.model.application.MContribution;

/**
 *
 */
public class ContributionUtils {

	/**
	 * Returns the instance of the implementation for a given contribution, if
	 * necessary create the instance and cache it
	 * 
	 * @param factory
	 *            The contribution factory to use
	 * @param contrib
	 *            The UI Model MContribution
	 * @param context
	 *            The context in which to create the instance
	 * 
	 * @return The implementation instance for this MContribution
	 */
	public static Object getInstance(IContributionFactory factory,
			MContribution contrib, IEclipseContext context) {
		if (contrib.getObject() != null)
			return contrib.getObject();

		contrib.setObject(factory.create(contrib.getContributionURI(), context));
		return contrib.getObject();
	}

	/**
	 * Executes the 'command' represented by the given MContribution's URI
	 * 
	 * @param factory
	 *            The contribution factory to use
	 * @param contrib
	 *            The UI Model's MContribution to be executed
	 * @param context
	 *            The eclipse context in which to execute
	 */
	public static void execute(IContributionFactory factory,
			MContribution contrib, IEclipseContext context) {
		context.set(MContribution.class.getName(), contrib);
		Object implementation = getInstance(factory, contrib, context);
		ContextInjectionFactory.invoke(implementation, "execute", context); //$NON-NLS-1$
		context.remove(MContribution.class.getName());
	}

}
