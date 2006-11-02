/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

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
public class CompareViewerPane extends ViewForm implements ISelectionProvider, 
		IDoubleClickListener, ISelectionChangedListener, IOpenListener, IAdaptable {
	
	private ToolBarManager fToolBarManager;
	private Object fInput;
	private ListenerList fSelectionListeners= new ListenerList();
	private ListenerList fDoubleClickListener= new ListenerList();
	private ListenerList fOpenListener= new ListenerList();

	/**
	 * Constructs a new instance of this class given its parent
	 * and a style value describing its behavior and appearance.
	 *
	 * @param container a widget which will be the container of the new instance (cannot be null)
	 * @param style the style of widget to construct
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 * </ul>
	 * @exception org.eclipse.swt.SWTException <ul>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
	 * </ul>
	 */		
	public CompareViewerPane(Composite container, int style) {
		super(container, style);
		
		marginWidth= 0;
		marginHeight= 0;
		
		CLabel label= new CLabel(this, SWT.NONE) {
			public Point computeSize(int wHint, int hHint, boolean changed) {
				return super.computeSize(wHint, Math.max(24, hHint), changed);
			}
		};
		setTopLeft(label);
		
		MouseAdapter ml= new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				Control content= getContent();
				if (content != null && content.getBounds().contains(e.x, e.y))
					return;
				Control parent= getParent();
				if (parent instanceof Splitter)
					((Splitter)parent).setMaximizedControl(CompareViewerPane.this);
			}
		};	
				
		addMouseListener(ml);
		label.addMouseListener(ml);
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (fToolBarManager != null) {
					fToolBarManager.removeAll();
					fToolBarManager.dispose();
					fToolBarManager= null;
				}
				fInput= null;
				fSelectionListeners= null;
			}
		});
	}
	
	/**
	 * Set the pane's title text.
	 * The value <code>null</code> clears it.
	 * 
	 * @param label the text to be displayed in the pane or null
	 */
	public void setText(String label) {
		CLabel cl= (CLabel) getTopLeft();
		if (cl != null)
			cl.setText(label);		
	}
	
	/**
	 * Set the pane's title Image.
	 * The value <code>null</code> clears it.
	 * 
	 * @param image the image to be displayed in the pane or null
	 */
	public void setImage(Image image) {
		CLabel cl= (CLabel) getTopLeft();
		if (cl != null)
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
			final ToolBar tb = new ToolBar(this, SWT.FLAT);
			setTopCenter(tb);
			fToolBarManager = new ToolBarManager(tb);
			tb.getAccessible().addAccessibleListener(new AccessibleAdapter() {
				public void getName(AccessibleEvent e) {
					if (e.childID != ACC.CHILDID_SELF) {
						ToolItem item = tb.getItem(e.childID);
						if (item != null) {
							String toolTip = item.getToolTipText();
							if (toolTip != null) {
								e.result = toolTip;
							}
						}
					}
				}
			});
		}
		return fToolBarManager;
	}
	
	/**
	 * Returns the current input of this pane or null if the pane has no input.
	 * 
	 * @return an <code>Object</code> that is the input to this pane or null if the pane has no input.
	 * 
	 * @since 3.3
	 */
	public Object getInput() {
		return fInput;
	}
	
	/**
	 * Sets the input object of this pane. 
	 * 
	 * @param input the new input object or <code>null</code>
	 * @since 3.3
	 */ 
	public void setInput(Object input) {
		if (fInput != input)
			fInput= input;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener l) {
		fSelectionListeners.add(l);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener l) {
		fSelectionListeners.remove(l);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection s) {
		// Default is to do nothing
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent ev) {
		Object[] listeners= fSelectionListeners.getListeners();
		for (int i= 0; i < listeners.length; i++)
			((ISelectionChangedListener) listeners[i]).selectionChanged(ev);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
		Object[] listeners= fDoubleClickListener.getListeners();
		for (int i= 0; i < listeners.length; i++)
			((IDoubleClickListener) listeners[i]).doubleClick(event);
	}

	/**
	 * Add a double-click listener to the pane. The listener will get
	 * invoked when the contents of the pane are double-clicked. Adding
	 * a listener that is already registered has no effect.
	 * @param listener the listener
	 * @since 3.3
	 */
	public void addDoubleClickListener(IDoubleClickListener listener) {
		fDoubleClickListener.add(listener);
	}

	/**
	 * Remove a double-click listener. Removing a listener that is not 
	 * registered has no effect.
	 * @param listener the listener
	 * @since 3.3
	 */
	public void removeDoubleClickListener(IDoubleClickListener listener) {
		fDoubleClickListener.remove(listener);
	}

	/**
	 * Add an open listener to the pane. The listener will get
	 * invoked when the contents of the pane are double-clicked. Adding
	 * a listener that is already registered has no effect.
	 * @param listener the listener
	 * @since 3.3
	 */
	public void addOpenListener(IOpenListener listener) {
		fOpenListener.add(listener);
	}

	/**
	 * Remove an open listener. Removing a listener that is not 
	 * registered has no effect.
	 * @param listener the listener
	 * @since 3.3
	 */
	public void removeOpenListener(IOpenListener listener) {
		fOpenListener.remove(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IOpenListener#open(org.eclipse.jface.viewers.OpenEvent)
	 */
	public void open(OpenEvent event) {
		Object[] listeners= fOpenListener.getListeners();
		for (int i= 0; i < listeners.length; i++)
			((IOpenListener) listeners[i]).open(event);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}
}
