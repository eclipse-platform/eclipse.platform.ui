/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */
package org.eclipse.compare;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.action.ToolBarManager;

import org.eclipse.compare.internal.Splitter;

/**
 * A <code>CompareViewerPane</code> is a convenience class which installs a
 * <code>CLabel</code> and a <code>Toolbar</code> in a <code>ViewForm</code>.
 * <P>
 * Double clicking onto the <code>CompareViewerPane</code>'s title bar maximizes
 * the <code>CompareViewerPane</code> to the size of an enclosing <code>Splitter</code>
 * (if there is one).
 * If more <code>Splitters</code> are nested maximizing walks up and
 * maximizes to the outermost <code>Splitter</code>.
 * 
 * @since 2.0
 */
public class CompareViewerPane extends ViewForm {
	
	private ToolBarManager fToolBarManager;

	/**
	 * Constructs a new instance of this class given its parent
	 * and a style value describing its behavior and appearance.
	 *
	 * @param parent a widget which will be the parent of the new instance (cannot be null)
	 * @param style the style of widget to construct
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
	 * </ul>
	 */		
	public CompareViewerPane(Composite parent, int style) {
		super(parent, style);
		
		marginWidth= 0;
		marginHeight= 0;
		
		CLabel label= new CLabel(this, SWT.NONE);
		setTopLeft(label);
		
		MouseAdapter ml= new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				Control parent= getParent();
				if (parent instanceof Splitter)
					((Splitter)parent).setMaximizedControl(CompareViewerPane.this);
			}
		};	
				
		addMouseListener(ml);
		label.addMouseListener(ml);	
	}
	
	/**
	 * Sets the receiver's title text.
	 * The value <code>null</code> clears it.
	 * 
	 * @param text the text to be displayed in the CompareViewerPane's title or null
	 * 
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setText(String label) {
		CLabel cl= (CLabel) getTopLeft();
		cl.setText(label);		
	}
	
	/**
	 * Return the receiver's title text.
	 * 
	 * @return the text of the CompareViewerPane's title or null
	 */
	public void setImage(Image image) {
		CLabel cl= (CLabel) getTopLeft();
		cl.setImage(image);
	}
	
	/**
	 * Returns a <code>ToolBarManager</code> if the given parent is a
	 * <code>CompareViewerPane</code> or <code>null</code> otherwise.
	 * 
	 * @param parent a <code>Composite</code> or <code>null</code>
	 * @return a <code>ToolBarManager</code> if the given parent is a <code>CompareViewerPane</code> otherwise <code>null</code>
	 */
	public static ToolBarManager getToolBarManager(Composite parent) {
		if (parent instanceof CompareViewerPane) {
			CompareViewerPane pane= (CompareViewerPane) parent;
			return pane.getToolBarManager();
		}
		return null;
	}

	/**
	 * Clears tool items in the <code>CompareViewerPane</code>'s control bar.
	 * 
	 * @param parent a <code>Composite</code> or <code>null</code>
	 */
	public static void clearToolBar(Composite parent) {
		ToolBarManager tbm= getToolBarManager(parent);
		if (tbm != null) {
			tbm.removeAll();
			tbm.update(true);
		}
	}
	
	//---- private stuff
	
	private ToolBarManager getToolBarManager() {
		if (fToolBarManager == null) {
			ToolBar tb= new ToolBar(this, SWT.FLAT);
			setTopCenter(tb);
			fToolBarManager= new ToolBarManager(tb);
		}
		return fToolBarManager;
	}
}
