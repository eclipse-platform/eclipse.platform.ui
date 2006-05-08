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
package org.eclipse.ui.internal.presentations.defaultpresentation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.ui.internal.presentations.BasicPartList;
import org.eclipse.ui.internal.presentations.util.ISystemMenu;
import org.eclipse.ui.internal.presentations.util.PresentablePartFolder;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * @since 3.1
 */
public class DefaultPartList implements ISystemMenu {

    private IStackPresentationSite site;
    private PresentablePartFolder folder;
    
    public DefaultPartList(IStackPresentationSite site, PresentablePartFolder folder) {
        this.site = site;
        this.folder = folder;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.ISystemMenu#show(org.eclipse.swt.widgets.Control, org.eclipse.swt.graphics.Point, org.eclipse.ui.presentations.IPresentablePart)
     */
    public void show(Control control, Point displayCoordinates,
            IPresentablePart currentSelection) {

        int shellStyle = SWT.RESIZE | SWT.ON_TOP | SWT.NO_TRIM;
        int tableStyle = SWT.V_SCROLL | SWT.H_SCROLL;
        final BasicPartList editorList = new BasicPartList(control.getShell(), 
                shellStyle, tableStyle, site, folder);
        editorList.setInput(folder);
        Point size = editorList.computeSizeHint();
        int x = displayCoordinates.x;
        int y = displayCoordinates.y;

        Monitor mon = folder.getTabFolder().getControl().getMonitor();
        Rectangle bounds = mon.getClientArea();
        if (x + size.x > bounds.x + bounds.width) {
			x = bounds.x + bounds.width - size.x;
		}
        if (y + size.y > bounds.y + bounds.height) {
			y = bounds.y + bounds.height - size.y;
		}
        editorList.setLocation(new Point(x, y));
        editorList.setVisible(true);
        editorList.setFocus();
        editorList.getTableViewer().getTable().getShell().addListener(
                SWT.Deactivate, new Listener() {

                    public void handleEvent(Event event) {
                        editorList.setVisible(false);
                    }
                }
        	);
    }
    
    public void dispose() {
        
    }

}
