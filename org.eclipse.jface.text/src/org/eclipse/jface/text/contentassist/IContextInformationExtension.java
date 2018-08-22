/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;


/**
 * Extends {@link org.eclipse.jface.text.contentassist.IContextInformation} with
 * the ability to freely position the context information.
 *
 * @since 2.0
 */
public interface IContextInformationExtension {

	/**
	 * Returns the start offset of the range for which this context
	 * information is valid or <code>-1</code> if unknown.
	 *
	 * @return the start offset of the range for which this context
	 *         information is valid
	 */
	int getContextInformationPosition();
}
