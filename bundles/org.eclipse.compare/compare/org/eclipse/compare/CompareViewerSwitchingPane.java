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
package org.eclipse.compare;

import java.text.MessageFormat;

import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.commands.util.ListenerList;
import org.eclipse.jface.viewers.*;

import org.eclipse.compare.internal.*;
import org.eclipse.compare.structuremergeviewer.ICompareInput;


/**
 * A custom <code>CompareViewerPane</code> that supports dynamic viewer switching.
 * 
 * <p>
 * Clients must implement the viewer switching strategy by implementing
 * the <code>getViewer(Viewer, Object)</code> method.
 * <p>
 * If a property with the name <code>CompareUI.COMPARE_VIEWER_TITLE</code> is set
 * on the top level SWT control of a viewer, it is used as a title in the <code>CompareViewerPane</code>'s
 * title bar.
 * 
 * @since 2.0
 */
public abstract class CompareViewerSwitchingPane extends CompareViewerPane
				implements ISelectionChangedListener, ISelectionProvider, IDoubleClickListener {
	
	private Viewer fViewer;
	private Object fInput;
	private ListenerList fSelectionListeners= new ListenerList();
	private ListenerList fDoubleClickListener= new ListenerList();
	private ListenerList fOpenListener= new ListenerList();
	private boolean fControlVisibility= false;
	private String fTitle;
	private String fTitleArgument;
	
	private IOpenListener fOpenHandler= new IOpenListener() {
		public void open(OpenEvent event) {
			Object[] listeners= fOpenListener.getListeners();
			for (int i= 0; i < listeners.length; i++)
				((IOpenListener) listeners[i]).open(event);
		}
	};
	
	/**
	 * Creates a <code>CompareViewerSwitchingPane</code> as a child of the given parent and with the
	 * specified SWT style bits.
	 *
	 * @param parent a widget which will be the parent of the new instance (cannot be null)
	 * @param style the style of widget to construct
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 * </ul>
	 * @exception org.eclipse.swt.SWTException <ul>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
	 * </ul>
	 */		
	public CompareViewerSwitchingPane(Composite parent, int style) {
		this(parent, style, false);
	}
	
	/**
	 * Creates a <code>CompareViewerSwitchingPane</code> as a child of the given parent and with the
	 * specified SWT style bits.
	 *
	 * @param parent a widget which will be the parent of the new instance (cannot be null)
	 * @param style the style of widget to construct
	 * @param visibility the initial visibility of the CompareViewerSwitchingPane
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 * </ul>
	 * @exception org.eclipse.swt.SWTException <ul>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
	 * </ul>
	 */		
	public CompareViewerSwitchingPane(Composite parent, int style, boolean visibility) {
		super(parent, style);

		fControlVisibility= visibility;
		
		setViewer(new NullViewer(this));
		
		addDisposeListener(
			new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (fViewer != null)
						fViewer.removeSelectionChangedListener(CompareViewerSwitchingPane.this);
					if (fViewer instanceof StructuredViewer) {
						StructuredViewer sv= (StructuredViewer) fViewer;
						sv.removeDoubleClickListener(CompareViewerSwitchingPane.this);
						sv.removeOpenListener(fOpenHandler);
					}
					fViewer= null;
					fInput= null;
					fSelectionListeners= null;
				}
			}
		);
	}
	
	/**
	 * Returns the current viewer.
	 * 
	 * @return the current viewer
	 */
	public Viewer getViewer() {
		return fViewer;
	}
	
	private void setViewer(Viewer newViewer) {
		
		if (newViewer == fViewer)
			return;
				
		boolean oldEmpty= isEmpty();

		if (fViewer != null) {
			
			fViewer.removeSelectionChangedListener(this);
				 
			if (fViewer instanceof StructuredViewer) {
				StructuredViewer sv= (StructuredViewer) fViewer;
				sv.removeDoubleClickListener(this);
				sv.removeOpenListener(fOpenHandler);
			}

			Control content= getContent();
			setContent(null);
			
			fViewer.setInput(null);
								
			if (content != null && !content.isDisposed())
				content.dispose();

		} else
			oldEmpty= false;			
		setContent(null);

		fViewer= newViewer;

		if (fViewer != null) {
			// we have to remember and restore the old visibility of the CustomPane
			// since setContent changes the visibility
			boolean old= getVisible();
			setContent(fViewer.getControl());
			setVisible(old);	// restore old visibility

			boolean newEmpty= isEmpty();

			fViewer.addSelectionChangedListener(this);

			if (fViewer instanceof StructuredViewer) {
				StructuredViewer sv= (StructuredViewer) fViewer;
				sv.addDoubleClickListener(this);
				sv.addOpenListener(fOpenHandler);
			}
			
			if (oldEmpty != newEmpty) {	// re-layout my container
				Composite parent= getParent();
				if (parent instanceof Splitter)
					((Splitter)parent).setVisible(this, fControlVisibility ? !newEmpty : true);
			}
				
			layout(true);
		}
	}

	/**
	 * Returns the optional title argument that has been set with <code>setTitelArgument</code>
	 * or <code>null</code> if no optional title argument has been set.
	 * <p>
	 * Note: this method is for internal use only. Clients should not call this method.
	 * 
	 * @return the optional title argument or <code>null</code>
	 */
	public String getTitleArgument() {
		return fTitleArgument;
	}

	/**
	 * Returns <code>true</code> if no viewer is installed or if the current viewer
	 * is a <code>NullViewer</code>.
	 * 
	 * @return <code>true</code> if no viewer is installed or if the current viewer is a <code>NullViewer</code>
	 */
	public boolean isEmpty() {
		return fViewer == null || fViewer instanceof NullViewer;
	}

	public void addSelectionChangedListener(ISelectionChangedListener l) {
		fSelectionListeners.add(l);
	}

	public void removeSelectionChangedListener(ISelectionChangedListener l) {
		fSelectionListeners.remove(l);
	}

	public void addDoubleClickListener(IDoubleClickListener l) {
		fDoubleClickListener.add(l);
	}

	public void removeDoubleClickListener(IDoubleClickListener l) {
		fDoubleClickListener.remove(l);
	}

	public void addOpenListener(IOpenListener l) {
		fOpenListener.add(l);
	}

	public void removeOpenListener(IOpenListener l) {
		fOpenListener.remove(l);
	}

	public void doubleClick(DoubleClickEvent event) {
		Object[] listeners= fDoubleClickListener.getListeners();
		for (int i= 0; i < listeners.length; i++)
			((IDoubleClickListener) listeners[i]).doubleClick(event);
	}

	public ISelection getSelection() {
		if (fViewer != null)
			return fViewer.getSelection();
		return null;
	}

	public void setSelection(ISelection s) {
		if (fViewer != null)
			 fViewer.setSelection(s);
	}

	public void selectionChanged(SelectionChangedEvent ev) {
		Object[] listeners= fSelectionListeners.getListeners();
		for (int i= 0; i < listeners.length; i++)
			((ISelectionChangedListener) listeners[i]).selectionChanged(ev);
	}
	
	private boolean hasFocus2() {
		// do we have focus?
		Display display= getDisplay();
		if (display != null)
			for (Control focus= display.getFocusControl(); focus != null; focus= focus.getParent())
				if (focus == this)
					return true;
		return false;
	}
		
	/**
	 * Sets the input object of this pane. 
	 * For this input object a suitable viewer is determined by calling the abstract
	 * method <code>getViewer(Viewer, Object)</code>.
	 * If the returned viewer differs from the current one, the old viewer
	 * is disposed and the new one installed. Then the input object is fed
	 * into the newly installed viewer by calling its <code>setInput(Object)</code> method.
	 * If new and old viewer don't differ no new viewer is installed but just
	 * <code>setInput(Object)</code> is called.
	 * If the input is <code>null</code> the pane is cleared,
	 * that is the current viewer is disposed.
	 * 
	 * @param input the new input object or <code>null</code>
	 */ 
	public void setInput(Object input) {

		if (fInput == input)
			return;
			
		boolean hadFocus= hasFocus2();
		
		fInput= input;

		// viewer switching
		Viewer newViewer= null;
		if (input != null)
			newViewer= getViewer(fViewer, input);

		if (newViewer == null) {
			if (fViewer instanceof NullViewer)
				return;
			newViewer= new NullViewer(this);
		}
		
		setViewer(newViewer);

		// set input
		fViewer.setInput(input);

		Image image= null;
		if (!(fViewer instanceof NullViewer) && input instanceof ICompareInput)
			image= ((ICompareInput)input).getImage();
		setImage(image);
		
		String title= null;	
		if (fViewer != null) {
			Control c= fViewer.getControl();
			if (c != null) {
				Object data= c.getData(CompareUI.COMPARE_VIEWER_TITLE);
				if (data instanceof String)
					title= (String) data;
				if (hadFocus)
					c.setFocus();
			}	
		}
			
		fTitle= title;
		updateTitle();
	}
	
	/**
	 * Sets an additional and optional argument for the pane's title.
	 * Note: this method is for internal use only. Clients should not call this method.
	 *  
	 * @param argument an optional argument for the pane's title
	 */
	public void setTitleArgument(String argument) {
		fTitleArgument= argument;
		updateTitle();
	}

	private void updateTitle() {
		if (fTitle != null) {
			if (fTitleArgument != null) {
				String format= CompareMessages.CompareViewerSwitchingPane_Titleformat;	
				String t= MessageFormat.format(format, new String[] { fTitle, fTitleArgument } );
				setText(t);
			} else
				setText(fTitle);			
		} else {
			setText("");	//$NON-NLS-1$
		}
	}

	/**
	 * Returns the current input of this pane or null if the pane has no input.
	 * 
	 * @return an <code>Object</code> that is the input to this pane or null if the pane has no input.
	 */
	public Object getInput() {
		return fInput;
	}

	/**
	 * Returns a viewer which is able to display the given input.
	 * If no viewer can be found, <code>null</code> is returned.
	 * The additional argument oldViewer represents the viewer currently installed
	 * in the pane (or <code>null</code> if no viewer is installed).
	 * It can be returned from this method if the current viewer can deal with the
	 * input (and no new viewer must be created).
	 *
	 * @param oldViewer the currently installed viewer or <code>null</code>
	 * @param input the input object for which a viewer must be determined or <code>null</code>
	 * @return a viewer for the given input, or <code>null</code> if no viewer can be determined
	 */
	abstract protected Viewer getViewer(Viewer oldViewer, Object input);
}
