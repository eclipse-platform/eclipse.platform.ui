/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.ILoggerProvider;
import org.eclipse.e4.core.services.log.Logger;
import org.osgi.framework.FrameworkUtil;

/**
 *
 */
public class DefaultLoggerProvider implements ILoggerProvider {
	@Inject
	private IEclipseContext context;

	@Override
	public Logger getClassLogger(Class<?> clazz) {
		IEclipseContext childContext = context.createChild();
		childContext.set("logger.bundlename", FrameworkUtil.getBundle(clazz).getSymbolicName()); //$NON-NLS-1$
		return (Logger) ContextInjectionFactory.make(WorkbenchLogger.class, childContext);
	}
}