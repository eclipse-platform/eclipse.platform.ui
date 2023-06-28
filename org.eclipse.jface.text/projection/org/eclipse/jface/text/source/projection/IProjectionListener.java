/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
package org.eclipse.jface.text.source.projection;

/**
 * Implementers registered with a
 * {@link org.eclipse.jface.text.source.projection.ProjectionViewer} get
 * informed when the projection mode of the viewer gets enabled and when it gets
 * disabled.
 *
 * @since 3.0
 */
public interface IProjectionListener {

	/**
	 * Tells this listener that projection has been enabled.
	 */
	void projectionEnabled();

	/**
	 * Tells this listener that projection has been disabled.
	 */
	void projectionDisabled();
}
