/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * The AssociatedWindow is a window that is associated with
 * another shell.
 */
class AssociatedWindow {
	
	private Shell floatingShell;
	private Composite control;
	
	/**
	 * 
	 */
	public AssociatedWindow(Shell parent, Composite owner) {
		this.createShell(parent);
		this.associateWith(owner, parent);
	}
	
	public void setVisible(boolean visible) {
		floatingShell.setVisible(visible);
	}
	
	/**
	 * @return a <code>Composite</code> which is the child control
	 * to add widgets to for the receiver.  Or <code>null</null> if one has not
	 * been created yet.
	 * 
	 */
	public Composite getControl() {
		return control;
	}
	
	
	public void createShell(Shell parent) {
		floatingShell = new Shell(parent, SWT.NO_TRIM);
		floatingShell.setLayout(new GridLayout());
		control = new Composite(parent, SWT.NONE);
		GridLayout gd = new GridLayout(1,true);
		gd.marginHeight = 1;
		gd.marginWidth = 1;
		control.setLayout(gd);
		control.setData(new GridData(GridData.FILL_BOTH));
		
//		floatingShell.setSize(floatingShell.computeSize(SWT.DEFAULT, SWT.DEFAULT));
//		Point shellSize = floatingShell.getSize();
//		Region r = new Region(d);
//		Rectangle rect = new Rectangle(0,0, shellSize.x, shellSize.y);
//		r.add(rect);
//		Region cornerRegion = new Region(d);
//		
//		//top right corner region
//		cornerRegion.add(new Rectangle(shellSize.x - 5, 0, 5 ,1));
//		cornerRegion.add(new Rectangle(shellSize.x - 3, 1, 3 ,1));
//		cornerRegion.add(new Rectangle(shellSize.x - 2, 2, 2 ,1));
//		cornerRegion.add(new Rectangle(shellSize.x - 1, 3, 1 ,2));
//		
//		//bottom right corner region
//		int y = shellSize.y;
//		cornerRegion.add(new Rectangle(shellSize.x - 5, y - 1, 5 ,1));
//		cornerRegion.add(new Rectangle(shellSize.x - 3, y - 2, 3 ,1));
//		cornerRegion.add(new Rectangle(shellSize.x - 2, y - 3, 2 ,1));
//		cornerRegion.add(new Rectangle(shellSize.x - 1, y - 5, 1 ,2));
//				
//		
//		r.subtract(cornerRegion);
//		floatingShell.setRegion(r);
		floatingShell.setTransparent(70);
		
		control.setBackground(new Color(control.getDisplay(), 255, 0,0));
		floatingShell.setBackground(new Color(control.getDisplay(), 0, 255,0));
		Button b = new Button(control, SWT.PUSH);
		b.setData(new GridData(GridData.FILL_BOTH));
		b.setText("HI");
		floatingShell.pack();
		
	}

	public void layout() {
		floatingShell.layout();
	}
	
	public void moveShell(Control control) {
		Point location = control.getLocation();
		Point size = control.getSize();
		int x = location.x + size.x;
		int y = location.y + 0;
		floatingShell.setLocation(x,y);
	}
	
	/**
	 * Track the following controls location, by locating the receiver
	 * along the right hand side of the control.  If the control moves the reciever
	 * should move along with it.
	 * 
	 * @param control
	 */
	public void associateWith(final Composite sibling, Shell parent) {
		
		ControlListener cl = new ControlListener() {
			public void controlMoved(ControlEvent e) {
				moveShell((Control)e.widget);
			}

			public void controlResized(ControlEvent e) {
				moveShell((Control)e.widget);
			}
		};
		
		sibling.addControlListener(cl);
		parent.addControlListener(cl);
		
		//set initial location
		moveShell(sibling);
		
		//Add the floating shell to the tab list
		Control[] c = parent.getTabList();
		Control[] newTab = new Control[c.length + 1];
		System.arraycopy(c, 0, newTab, 0, c.length);
		newTab[c.length] = floatingShell;
		parent.setTabList(newTab);
		
		
	}
}