/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.update.ui.forms.internal.engine;

import org.eclipse.swt.graphics.GC;
import java.util.Hashtable;

public interface IBulletParagraph extends IParagraph {
	int CIRCLE = 0;
	int TEXT = 1;
	int IMAGE = 2;
	public int getBulletStyle();
	public String getBulletText();

	public void paintBullet(GC gc, Locator loc, int lineHeight, Hashtable objectTable);
}