/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
package org.eclipse.compare.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.graphics.Image;

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;

import org.eclipse.jface.action.ToolBarManager;

/**
 * A Pane is a convenience class which installs a CustomLabel and a Toolbar (on demand).
 * Double clicking onto the Pane's title bar maximizes the Pane 
 * to the size of an enclosing Splitter (if there is one).
 * If more Splitters are nested maximizing walks up and maximizes to the outermost Splitter.
 */
public class Pane extends ViewForm {
	
	private ToolBarManager fToolBarManager;

	public Pane(Composite parent, int style) {
		super(parent, SWT.BORDER);
		
		marginWidth= 0;
		marginHeight= 0;
		
		CLabel label= new CLabel(this, SWT.NONE);
		setTopLeft(label);
		
		MouseAdapter ml= new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				Control parent= getParent();
				if (parent instanceof Splitter)
					((Splitter)parent).setMaximizedControl(Pane.this);
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
	 * Returns a <code>ToolBarManager</code> if the given parent is a <code>ViewerPane</code>.
	 */
	public static ToolBarManager getToolBarManager(Composite parent) {
		if (parent instanceof Pane) {
			Pane pane= (Pane) parent;
			return pane.getToolBarManager();
		}
		return null;
	}

	/**
	 * Clear tool items in <code>ViewerPane</code>'s control bar.
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
