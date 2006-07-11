/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 * An <code>ITocProvider</code> is a mechanism to provide arbitrary content to
 * the table of contents (TOC). <code>ITocProvider</code>s must be registered
 * via the <code>org.eclipse.help.toc</code> extension point, and are queried
 * once only per session.
 * 
 * This interface is intended to be implemented by clients.
 * 
 * @since 3.3
 */
public interface ITocProvider {

	/**
	 * Returns all <code>ITocContribution</code>s for this provider. Providers
	 * are free to provide any number of contributions (zero or more). A return
	 * value of <code>null</code> will be treated the same as an array of size
	 * zero.
	 * 
	 * @param locale the locale for which to get contributions
	 * @return all the contributions for this provider
	 */
	public ITocContribution[] getTocContributions(String locale);
}
