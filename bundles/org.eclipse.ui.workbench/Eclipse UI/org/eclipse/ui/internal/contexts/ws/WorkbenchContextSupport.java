/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.contexts.ws;

import org.eclipse.ui.contexts.ContextActivationServiceFactory;
import org.eclipse.ui.contexts.ContextManagerFactory;
import org.eclipse.ui.contexts.ICompoundContextActivationService;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IMutableContextManager;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.internal.contexts.ProxyContextManager;

public class WorkbenchContextSupport implements IWorkbenchContextSupport {
	private ICompoundContextActivationService compoundContextActivationService;
	private IMutableContextManager mutableContextManager;
	private ProxyContextManager proxyContextManager;

	public WorkbenchContextSupport() {
		mutableContextManager =
			ContextManagerFactory.getMutableContextManager();
		proxyContextManager = new ProxyContextManager(mutableContextManager);
		compoundContextActivationService =
			ContextActivationServiceFactory
				.getCompoundContextActivationService();
	}

	public ICompoundContextActivationService getCompoundContextActivationService() {
		return compoundContextActivationService;
	}

	public IContextManager getContextManager() {
		return proxyContextManager;
	}
}
