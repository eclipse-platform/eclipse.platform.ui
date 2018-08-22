/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.modeling;

import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

/**
 * A handler that can be inserted into the context of the application or a particular window to
 * determine whether the window should be closed or not.
 *
 * @noreference This interface is not intended to be referenced by clients.
 * @since 1.0
 */
public interface IWindowCloseHandler {

	public boolean close(MWindow window);

}
