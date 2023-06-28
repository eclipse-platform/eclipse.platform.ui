/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.jface.text.reconciler;


/**
 * Extends {@link org.eclipse.jface.text.reconciler.IReconciler} with
 * the ability to be aware of documents with multiple partitionings.
 *
 * @since 3.0
 */
public interface IReconcilerExtension {

	/**
	 * Returns the partitioning this reconciler is using.
	 *
	 * @return the partitioning this reconciler is using
	 */
	String getDocumentPartitioning();
}
