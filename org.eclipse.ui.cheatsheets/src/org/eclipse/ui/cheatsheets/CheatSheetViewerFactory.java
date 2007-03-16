/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.cheatsheets;

import org.eclipse.ui.internal.cheatsheets.views.CheatSheetViewer;

/**
 * A factory for creating a cheat sheet viewer.
 * <p>
 * This class provides all its functionality via static members.
 * It is not intended to be instantiated.
 * </p>
 * 
 * @since 3.0
 */
public final class CheatSheetViewerFactory {

	/**
	 * Non-instantiable.
	 */
	private CheatSheetViewerFactory() {
		// do nothing
	}
	
	/**
	 * Creates a new cheat sheet viewer. The viewer does not
	 * show any cheat sheet initially.
	 * 
	 * @return a new cheat sheet viewer
	 */
	public static ICheatSheetViewer createCheatSheetView() {
		return new CheatSheetViewer(false);
	}
}
