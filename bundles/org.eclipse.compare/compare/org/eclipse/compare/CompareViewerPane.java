/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
 */
public class CompareViewerPane extends ViewForm {
	
	private ToolBarManager fToolBarManager;


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
	
	public void setText(String label) {
		CLabel cl= (CLabel) getTopLeft();
		cl.setText(label);		
	}
	
	public void setImage(Image image) {
		CLabel cl= (CLabel) getTopLeft();
		cl.setImage(image);
	}
	
	/**
	 * Returns a <code>ToolBarManager</code> if the given parent is a <code>CompareViewerPane</code>.
	 */
	public static ToolBarManager getToolBarManager(Composite parent) {
		if (parent instanceof CompareViewerPane) {
			CompareViewerPane pane= (CompareViewerPane) parent;
			return pane.getToolBarManager();
		}
		return null;
	}

	/**
	 * Clear tool items in <code>CompareViewerPane</code>'s control bar.
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
