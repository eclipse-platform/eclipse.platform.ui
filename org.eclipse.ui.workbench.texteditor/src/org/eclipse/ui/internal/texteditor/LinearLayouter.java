/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;

import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Layout;

/**
 *
 *
 * @since 3.0
 */
public class LinearLayouter {

	private static final int ANNOTATION_SIZE= 14;
	private static final int BORDER_WIDTH= 2;

	public Layout getLayout(int itemCount) {
		// simple layout: a row of items
		GridLayout layout= new GridLayout(itemCount, true);
		layout.horizontalSpacing= 1;
		layout.verticalSpacing= 0;
		layout.marginHeight= 1;
		layout.marginWidth= 1;
		return layout;
	}

	public Object getLayoutData() {
		GridData gridData= new GridData(ANNOTATION_SIZE + 2 * BORDER_WIDTH, ANNOTATION_SIZE + 2 * BORDER_WIDTH);
		gridData.horizontalAlignment= GridData.CENTER;
		gridData.verticalAlignment= GridData.CENTER;
		return gridData;
	}

	public int getAnnotationSize() {
		return ANNOTATION_SIZE;
	}

	public int getBorderWidth() {
		return BORDER_WIDTH;
	}

	public Region getShellRegion(int itemCount) {
		// no special region - set to null for default shell size
		return null;
	}

}
