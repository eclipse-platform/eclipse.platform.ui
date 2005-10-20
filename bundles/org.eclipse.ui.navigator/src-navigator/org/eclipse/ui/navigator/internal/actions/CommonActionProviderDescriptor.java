/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.navigator.internal.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.internal.ActionExpression;
import org.eclipse.ui.navigator.ICommonActionProvider;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.navigator.internal.extensions.SkeletonActionProvider;

/**
 * <p>
 * The following class is experimental until fully documented.
 * </p>
 */
public class CommonActionProviderDescriptor {

	private final IConfigurationElement configurationElement;
	private ActionExpression enablement;
	private static final String ENABLEMENT= "enablement"; //$NON-NLS-1$
	private boolean hasLoadingFailed;
	private static final String ATT_CLASS= "class"; //$NON-NLS-1$

	/**
	 *  
	 */
	public CommonActionProviderDescriptor(IConfigurationElement aConfigElement) {
		super();
		configurationElement= aConfigElement;
		init();
	}

	/**
	 *  
	 */
	private void init() {

		IConfigurationElement[] children= configurationElement.getChildren(ENABLEMENT);
		if (children.length == 1) {
			enablement= new ActionExpression(children[0]);
		} else if (children.length > 1) {
			System.err.println("More than one element: " + //$NON-NLS-1$
						ENABLEMENT + " in navigator extension: " + //$NON-NLS-1$
						configurationElement.getDeclaringExtension().getUniqueIdentifier());
		}
	}

	public ICommonActionProvider createActionProvider() {
		if (hasLoadingFailed)
			return SkeletonActionProvider.INSTANCE;
		ICommonActionProvider provider= null;
		try {
			provider= (ICommonActionProvider) configurationElement.createExecutableExtension(ATT_CLASS);
		} catch (CoreException exception) {
			NavigatorPlugin.log("Unable to create navigator extension: " + //$NON-NLS-1$
						getClassName(), exception.getStatus());
			hasLoadingFailed= true;
		} catch (Exception e) {
			NavigatorPlugin.log("Unable to create navigator extension: " + //$NON-NLS-1$
						getClassName(), new Status(IStatus.ERROR, NavigatorPlugin.PLUGIN_ID, 0, e.getMessage(), e));
			e.printStackTrace();
			hasLoadingFailed= true;
		}
		return provider;
	}

	/**
	 * @return
	 */
	private String getClassName() {
		return configurationElement.getAttribute(ATT_CLASS);
	}

	public boolean isEnabledFor(IStructuredSelection aStructuredSelection) {
		return (enablement != null && enablement.isEnabledFor(aStructuredSelection));
	}

	public boolean isEnabledFor(Object anElement) {
		return (enablement != null && enablement.isEnabledFor(anElement));
	}

}