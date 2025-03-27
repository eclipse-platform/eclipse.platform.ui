/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jozef Tomek - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.ui;

import org.eclipse.text.quicksearch.ITextViewerCreator;

interface IViewerDescriptor {

	/**
	 * Creates a new viewer creator from this descriptor.
	 *
	 * @return a new viewer creator.
	 */
	ITextViewerCreator getViewerCreator();

	/**
	 * Returns label for viewer created by provided viewer creator that will be used in UI.
	 *
	 * @return label for provided viewer used in UI
	 */
	String getLabel();

}
