/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.rcp.browser;

/**
 * Marks a workbench part implementation as being a secondary part.
 * A secondary part is one that exists only to support the browser,
 * and should not be considered when determining whether to close
 * a window whose last browser has been closed.
 */
public interface ISecondaryPart {
    // marker interface only; no behaviour
}
