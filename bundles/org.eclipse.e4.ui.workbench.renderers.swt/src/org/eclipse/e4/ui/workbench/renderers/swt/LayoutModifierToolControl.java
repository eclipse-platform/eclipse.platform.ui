/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import javax.annotation.PostConstruct;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * This is a specialized tool control used by the TrimBarLayout to modify the
 * layout mechanisms.
 */
public class LayoutModifierToolControl {
	@PostConstruct
	void createWidget(Composite parent, MToolControl tc) {
		Composite comp = new Composite(parent, SWT.NONE) {
			@Override
			public Point computeSize(int wHint, int hHint, boolean flushCache) {
				return new Point(0, 0);
			}
		};
		comp.setSize(0, 0);
	}
}
