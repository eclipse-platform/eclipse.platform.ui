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

import org.eclipse.e4.ui.model.application.ui.basic.MPart;

/**
 * @since 1.0
 */
public interface IPartListener {

	public void partActivated(MPart part);

	public void partBroughtToTop(MPart part);

	public void partDeactivated(MPart part);

	public void partHidden(MPart part);

	public void partVisible(MPart part);

}
