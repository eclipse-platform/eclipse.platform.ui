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
package org.eclipse.e4.ui.workbench.addons.dndaddon;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

class CursorInfo {
	Point cursorPos;
	MUIElement curElement;
	MPlaceholder curElementRef;
	MUIElement itemElement;
	MPlaceholder itemElementRef;
	int itemIndex;
	Rectangle itemRect;
}
