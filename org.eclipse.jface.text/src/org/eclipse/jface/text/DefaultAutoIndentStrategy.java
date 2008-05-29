/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;


/**
 * Default implementation of {@link org.eclipse.jface.text.IAutoIndentStrategy}.
 * This strategy always copies the indentation of the previous line.
 * <p>
 * This class is not intended to be subclassed.</p>
 *
 * @deprecated since 3.1 use {@link org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy} instead
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DefaultAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy implements IAutoIndentStrategy {

	/**
	 * Creates a new default auto indent strategy.
	 */
	public DefaultAutoIndentStrategy() {
	}
}
