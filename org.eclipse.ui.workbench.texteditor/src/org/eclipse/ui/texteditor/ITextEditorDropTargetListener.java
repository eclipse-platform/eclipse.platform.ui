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
package org.eclipse.ui.texteditor;

import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;

/**
 * Interface to be implemented by SWT drop target listeners to be used with
 * <code>AbstractTextEditor</code>.
 * <p>
 * This interface is not part of the official API.
 *
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ITextEditorDropTargetListener extends DropTargetListener {

	/**
	 * Returns the list of <code>Transfer</code> agents that are supported by this listener.
	 *
	 * @return the list of transfer agents supported by this listener
	 */
	Transfer[] getTransfers();
}
