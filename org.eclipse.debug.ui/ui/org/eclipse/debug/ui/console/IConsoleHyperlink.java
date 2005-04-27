/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.console;

import org.eclipse.ui.console.IHyperlink;


/**
 * A hyperlink in the console. Link behavior is implementation dependent.
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * Since 3.1, this interface now extends {@link org.eclipse.ui.console.IHyperlink}.
 * </p>
 * @since 2.1
 * @deprecated replaced by org.eclipse.ui.console.IHyperlink
 */
public interface IConsoleHyperlink extends IHyperlink {
}
