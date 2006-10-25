/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A trim element representing the EditorArea
 * 
 * @since 3.3
 *
 */
public class EditorAreaTrimPart extends TrimPart {

	public EditorAreaTrimPart(WorkbenchWindow window, EditorSashContainer editorArea) {
		super(window, editorArea);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.TrimPart#addItems()
	 */
	protected void addItems() {
        // Since we dont have fast view behaviour for ediors we
		// simply restore the editor area on selection
        ToolItem editorAreaItem = new  ToolItem(toolBar, SWT.PUSH, toolBar.getItemCount());        
        Image tbImage = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_ETOOL_EDITOR_TRIMPART);
        editorAreaItem.setImage(tbImage);       
        String menuTip = WorkbenchMessages.EditorArea_Tooltip;
        editorAreaItem.setToolTipText(menuTip);
        editorAreaItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				restorePart();
			}
			public void widgetSelected(SelectionEvent e) {
				restorePart();
			}
        });
	}

}
