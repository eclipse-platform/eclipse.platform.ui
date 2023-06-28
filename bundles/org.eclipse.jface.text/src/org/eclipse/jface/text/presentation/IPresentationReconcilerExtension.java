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
package org.eclipse.jface.text.presentation;

/**
 * Extension interface for {@link IPresentationReconciler}. Adds awareness of
 * documents with multiple partitions.
 *
 * @since 3.0
 */
public interface IPresentationReconcilerExtension {

	/**
	 * Returns the document partitioning this presentation reconciler is using.
	 *
	 * @return the document partitioning this presentation reconciler is using
	 */
	String getDocumentPartitioning();
}
