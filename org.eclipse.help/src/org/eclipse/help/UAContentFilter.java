/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.HelpPlugin;

/**
 * <p>
 * This class provides the ability to filter out user assistance model elements
 * that support filtering (e.g. <code>IToc</code>, <code>ITopic</code>, ...).
 * Implementations that display such elements should consult this class before
 * attempting to display them.
 * </p>
 * 
 * @since 3.2
 */
public class UAContentFilter {
	
	private static final String VARIABLE_PLATFORM = "platform"; //$NON-NLS-1$
	private static IEvaluationContext defaultContext;

	/**
	 * <p>
	 * Returns whether or not the given object should be filtered out. This
	 * applies to any user assistance component's elements where filters apply
	 * (e.g. help tocs, topics, intro elements, context help topics). If the
	 * element is <code>null</code> or is not filterable, this method returns
	 * <code>false</code>. 
	 * </p>
	 * <p>
	 * This method is for use in non-UI environments, when serving help outside
	 * the workbench. If filtering from the UI, use the <code>isFiltered</code>
	 * method that accepts the evaluation context as well.
	 * </p>
	 * 
	 * @param element the element to check
	 * @return whether or not the element should be filtered out
	 */
	public static boolean isFiltered(Object element) {
		if (defaultContext == null) {
			defaultContext = createDefaultContext();
		}
		return isFiltered(element, defaultContext);
	}

	private static IEvaluationContext createDefaultContext() {
		EvaluationContext context = new EvaluationContext(null, Platform.class);
		context.addVariable(VARIABLE_PLATFORM, Platform.class);
		return context;
	}

	/**
	 * <p>
	 * Returns whether or not the given object should be filtered out. This
	 * applies to any user assistance component's elements where filters apply
	 * (e.g. help tocs, topics, intro elements, context help topics). If the
	 * element is <code>null</code> or is not filterable, this method returns
	 * <code>false</code>. The evaluation context provides the default object
	 * to test on and a set of variables that can be accessed.
	 * </p>
	 * 
	 * @param element the element to check
	 * @param context the evaluation context for evaluating expressions
	 * @return whether or not the element should be filtered out
	 */
	public static boolean isFiltered(Object element, IEvaluationContext context) {
		if (element instanceof IUAElement) {
			try {
				return !((IUAElement)element).isEnabled(context);
			}
			catch (Throwable t) {
				String msg = "Error while checking element filter"; //$NON-NLS-1$
				HelpPlugin.logError(msg, t);
			}
		}
		return false;
	}
}
