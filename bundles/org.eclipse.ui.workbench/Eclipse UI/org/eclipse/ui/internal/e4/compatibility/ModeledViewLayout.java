/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.ui.IViewLayout;

public class ModeledViewLayout implements IViewLayout {

	// private MContributedPart viewME;

	public ModeledViewLayout(MPart view) {
		// viewME = view;
	}

	public boolean getShowTitle() {
		return true;// viewME.getShowTitle();
	}

	public boolean isCloseable() {
		return true;// viewME.isCloseable();
	}

	public boolean isMoveable() {
		return true;// viewME.isMoveable();
	}

	public boolean isStandalone() {
		return false;// viewME.isStandAlone();
	}

	public void setCloseable(boolean closeable) {
		// viewME.setCloseable(closeable);
	}

	public void setMoveable(boolean moveable) {
		// viewME.setMoveable(moveable);
	}

}
