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
package org.eclipse.ui.examples.presentation.wrappedtabs;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.SwtUtil;

/**
 * A ProxyControl is an invisible control whose size and position are linked
 * with some target control. That is, when the dummy control is asked for its
 * preferred size it returns the preferred size of the target. Changing the
 * bounds of the dummy control also changes the bounds of the target. This allows 
 * any Composite to lay out a control that isn't one of its children.
 * 
 * <p>
 * For example, imagine you have a ViewForm and a ToolBar that share the same parent
 * and you want the ToolBar to be located in the upper-right corner of the ViewForm.
 * If the ToolBar were a child of the ViewForm, this could be done easily by calling
 * viewForm.setTopRight(toolBar). However, this is impossible since ViewForm.setTopRight
 * will only accept a child control. Instead, we create a ProxyControl as a child
 * of the viewForm, and set the toolbar as its target. The ViewForm will treat
 * the ProxyControl just like any other child, but it will actually be arranging the
 * ToolBar. 
 * </p>
 * <p>For example:
 * </p>
 * <code>
 *      // Create a ViewForm and a ToolBar that are siblings
 * 		ViewForm viewForm = new ViewForm(parent, SWT.NONE);
 * 		ToolBar toolBar = new ToolBar(parent, SWT.NONE);
 * 
 *      // Allow the ViewForm to control the position of the ToolBar by creating
 *      // a ProxyControl in the ViewForm that targets the ToolBar.
 * 		ProxyControl toolBarProxy = new ProxyControl(viewForm);
 * 		toolBarProxy.setTarget(toolBar);
 * 		viewForm.setTopRight(toolBarProxy.getControl());
 * </code>
 * 
 * <p>
 * This is intended to simplify management of view toolbars in the presentation API.
 * Presentation objects have no control over where the view toolbars are created in
 * the widget hierarchy, but they may wish to control the position of the view toolbars
 * using traditional SWT layouts and composites. 
 * </p>
 */
public class ProxyControl {
    
    /**
     * Invisible dummy control 
     */
	private Composite control;
	
	/**
	 * Target control (possibly null)
	 */
	private Control target;
	
	/**
	 * Most specific common ancestor between the target and the proxy controls
	 */
	private Control commonAncestor;
	
	/**
	 * Visibility state of the proxy control the last time it had a non-null target.
	 * Note: when the target is set to null, we force the proxy to become invisible
	 * and use this variable to remember the initial state when we get a new non-null
	 * target.
	 */
	private boolean visible = true;
	
	/**
	 * Dispose listener. Breaks the link between the target and the proxy if either
	 * control is disposed.
	 */
	private DisposeListener disposeListener = new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			if (e.widget == target || e.widget == control) {
				setTargetControl(null);
			}
		}
	};
	
	private Listener visibilityListener = new Listener() {

        public void handleEvent(Event event) {
            if (target != null) {
                visible = control.getVisible();
                target.setVisible(visible);
            }
        }
	    
	};
	
	/**
	 * Movement listener. Updates the bounds of the target to match the 
	 * bounds of the dummy control.
	 */
	private ControlListener controlListener = new ControlListener() {

		public void controlMoved(ControlEvent e) {
			ProxyControl.this.layout();
		}

		public void controlResized(ControlEvent e) {
		    //if (e.widget == control) {
		     //   ProxyControl.this.layout();
		    //}
		}
		
	};
	
	/**
	 * Creates a new ProxyControl as a child of the given parent. This is an invisible dummy
	 * control. If given a target, the ProxyControl will update the bounds of the target to
	 * match the bounds of the dummy control.
	 * 
	 * @param parent parent composite
	 */
	public ProxyControl(Composite parent) {
	    // Create the invisible dummy composite
		control = new Composite(parent, SWT.NO_BACKGROUND);
		control.setVisible(false);
		
		// Attach a layout to the dummy composite. This is used to make the preferred
		// size of the dummy match the preferred size of the target.
		control.setLayout(new Layout() {
			protected void layout (Composite composite, boolean flushCache) {
			    ProxyControl.this.layout();
			    // does nothing. The bounds of the target are updated by the controlListener
			}
			
			protected Point computeSize (Composite composite, int wHint, int hHint, boolean flushCache) {
				if (target == null) {
				    // Note: If we returned (0,0), SWT would ignore the result and use a default value.
					return new Point(1,1);
				}
				
				return target.computeSize(wHint, hHint);
			}
		});
		
		// Attach listeners to the dummy
		control.addDisposeListener(disposeListener);
		control.addListener(SWT.Show, visibilityListener);
		control.addListener(SWT.Hide, visibilityListener);
	}
	
	/**
	 * Sets the control whose position will be managed by this proxy
	 * 
	 * @param target the control, or null if none
	 */
	public void setTargetControl(Control target) {
		if (this.target != target) {

		    if (this.target != null) {
		        for (Control next = control; next != commonAncestor && next != null; next = next.getParent()) {
		            next.removeControlListener(controlListener);
		        }
		        commonAncestor = null;
		        
			    // If we already had a target, detach the dispose listener 
			    // (prevents memory leaks due to listeners)
				if (!this.target.isDisposed()) {
					this.target.removeDisposeListener(disposeListener);
				}				
		    }
			
			if (this.target == null && target != null) {
			    // If we had previously forced the dummy control invisible, restore its visibility
			    control.setVisible(visible);
			}
			
			this.target = target;
			
			if (target != null) {
			    commonAncestor = SwtUtil.findCommonAncestor(this.target, control);
		        for (Control next = control; next != null && next != commonAncestor; next = next.getParent()) {
		            next.addControlListener(controlListener);
		        }
			    
			    // Make the new target's visiblity match the visibility of the dummy control
			    target.setVisible(control.getVisible());
				// Add a dispose listener. Ensures that the target is cleared
				// if it is ever disposed.
				target.addDisposeListener(disposeListener);
			} else {
			    control.setVisible(false);
			}
		}
	}
	
	/**
	 * Returns the target control (the control whose size is being managed)
	 * 
	 * @return the target control (or null)
	 */
	public Control getTargetControl() {
		if (target == null) {
			return null;
		}
		
		return target;
	}
	
	/**
	 * Returns the proxy control
	 * 
	 * @return the proxy control (not null)
	 */
	public Control getControl() {
		return control;
	}
	
	public Control getTarget() {
	    return target;
	}
	
	/**
	 * Moves the target control on top of the dummy control.
	 */
	public void layout() {
		if (getTargetControl() == null) {
			return;
		}
		
		// Compute the unclipped bounds of the target in display coordinates
		Rectangle displayBounds = Geometry.toDisplay(control.getParent(), control.getBounds());
		
		// Clip the bounds of the target so that it doesn't go outside the dummy control's parent
		Rectangle clippingRegion = DragUtil.getDisplayBounds(control.getParent());
		displayBounds = displayBounds.intersection(clippingRegion);
		
		// Compute the bounds of the target, in the local coordinate system of its parent
		Rectangle targetBounds = Geometry.toControl(getTargetControl().getParent(), displayBounds);
		
		// Move the target
		getTargetControl().setBounds(targetBounds);
	}
}
