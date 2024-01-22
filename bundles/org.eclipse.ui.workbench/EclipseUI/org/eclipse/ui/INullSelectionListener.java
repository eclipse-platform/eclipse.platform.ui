/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui;

/**
 * Interface for listening to <code>null</code> selection changes.
 * <p>
 * This interface should be implemented by selection listeners that want to be
 * notified when the selection becomes <code>null</code>. It has no methods. It
 * simply indicates the desire to receive <code>null</code> selection events
 * through the existing <code>selectionChanged</code> method. Either the part or
 * the selection may be <code>null</code>.
 * </p>
 *
 * @see ISelectionListener#selectionChanged
 * @see IActionDelegate#selectionChanged
 * @see org.eclipse.ui.ISelectionListener
 *
 * @since 2.0
 */
public interface INullSelectionListener extends ISelectionListener {
}
