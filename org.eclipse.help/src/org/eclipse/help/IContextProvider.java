/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

/**
 * <p>
 * A dual-purpose context-sensitive help provider that can either be used
 * on a specific object (a local provider) or be registered to provide help for
 * any id (a global provider).
 * </p>
 * <p>
 * Global providers are registered in the <code>plugin.xml</code> via the
 * <code>contextProvider</code> element of the <code>org.eclipse.help.contexts</code>
 * extension point. In this case, only the <code>getContext(Object)</code>
 * method will be called, and it will be passed a <code>String</code> containing
 * the full context id (e.g. <code>com.my.plugin.myContextId</code>).
 * </p>
 * <p>
 * In the case of a local provider, classes that implement this interface should
 * be returned from adaptable objects when <code>IContextProvider.class</code>
 * is used as the adapter key. Adaptable objects must implement
 * <code>org.eclipse.core.runtime.IAdaptable</code> interface.
 * </p>
 * <p>
 * Local providers can be used for providing focused dynamic help that changes
 * depending on the various platform states. The state change criteria are
 * defined by bitwise-OR of the individual state change triggers. Each time a
 * registered trigger occurs, the class that implements this interface will be
 * called again to provide the help context for the given target.
 * </p>
 * <p>
 * Local providers should be used for all visual artifacts that provide context
 * help that handle context help trigger by handling the SWT help event instead
 * of tagging the artifact with a static context Id. In addition to providing
 * static help context, this interface can also be used to control the query
 * string that is passed to the help search system on context switches. If not
 * provided, the query string is computed based on the current context.
 * Providing the string explicitly gives the context owners better control over
 * the search outcome.
 * </p>
 * 
 * @since 3.1
 * @see IContext
 * @see org.eclipse.core.runtime.IAdaptable
 */
public interface IContextProvider {
	/**
	 * State change trigger indicating a static context provider.
	 */
	int NONE = 0x0;

	/**
	 * State change trigger indicating that the provider should be asked for
	 * context help on each selection change.
	 */
	int SELECTION = 0x1;

	/**
	 * Returns the mask created by combining supported change triggers using the
	 * bitwise OR operation. This method is only called for local providers.
	 * 
	 * @return a bitwise-OR combination of state change triggers or
	 *         <code>NONE</code> for a static provider.
	 */
	int getContextChangeMask();

	/**
	 * <p>
	 * Returns a help context for the given target. The number of times this
	 * method will be called depends on the context change mask. Static context
	 * providers will be called each time the owner of the target is activated.
	 * If change triggers are used, the method will be called each time the
	 * trigger occurs.
	 * </p>
	 * <p>
	 * For global providers registered via the <code>org.eclipse.help.contexts</code>
	 * extension point, the parameter will always be a <code>String</code>
	 * containing the full context help id (e.g.
	 * <code>com.my.plugin.myContextId</code>).
	 * </p>
	 * 
	 * @param target
	 *            the focus of the context help
	 * @return context help for the provided target or <code>null</code> if
	 *         none is defined.
	 */
	IContext getContext(Object target);

	/**
	 * Returns a search expression that should be used to find more information
	 * about the current target. If provided, it can be used for background
	 * search. This method is only called for local providers.
	 * 
	 * @param target
	 *            the focus of the context help
	 * @return search expression as defined by the help system search, or
	 *         <code>null</code> if background search is not desired.
	 */
	String getSearchExpression(Object target);
}
