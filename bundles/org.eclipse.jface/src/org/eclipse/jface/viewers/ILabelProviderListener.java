/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 503316
 *******************************************************************************/
package org.eclipse.jface.viewers;

/**
 * A listener which is notified when a label provider's state changes.
 *
 * @see IBaseLabelProvider#addListener
 * @see IBaseLabelProvider#removeListener
 */
@FunctionalInterface
public interface ILabelProviderListener {
	/**
	 * Notifies this listener that the state of the label provider
	 * has changed in a way that affects the labels it computes.
	 * <p>
	 * A typical response would be to refresh all labels by
	 * re-requesting them from the label provider.
	 * </p>
	 *
	 * @param event the label provider change event
	 */
	public void labelProviderChanged(LabelProviderChangedEvent event);
}
