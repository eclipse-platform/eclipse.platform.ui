/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * @since 1.0
 */
public interface IWindowCloseHandler {

	public boolean close(MWindow window);

}
