/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare;

import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.*;

import org.eclipse.compare.internal.*;
import org.eclipse.compare.structuremergeviewer.ICompareInput;


/**
 * A custom <code>CompareViewerPane</code> which supports viewer switching.
 * <p>
 * Clients must implement the viewer switching strategy by implementing
 * <code>getViewer</code>method.
 * <p>
 * If a property with the name <code>CompareUI.COMPARE_VIEWER_TITLE</code> is set
 * on the top level SWT control of a viewer, it is used as a title in the <code>CompareViewerPane</code>'s
 * title bar.
 */
public abstract class CompareViewerSwitchingPane extends CompareViewerPane
				implements ISelectionChangedListener, ISelectionProvider, IDoubleClickListener {
	
	private Viewer fViewer;
	private Object fInput;
	private ListenerList fSelectionListeners= new ListenerList();
	private ListenerList fOpenListeners= new ListenerList();
	private boolean fControlVisibility= false;
	private String fTitle;
	private String fTitleArgument;


	/**
	 * Creates a <code>CompareViewerSwitchingPane</code> as a child of the given parent and with the
	 * specified SWT style bits.
	 */
	public CompareViewerSwitchingPane(Composite parent, int style) {
		this(parent, style, false);
	}
	
	/**
	 * Creates a <code>CompareViewerSwitchingPane</code> as a child of the given parent and with the
	 * specified SWT style bits.
	 */
	public CompareViewerSwitchingPane(Composite parent, int style, boolean visibility) {
		super(parent, style);

		fControlVisibility= visibility;
		
		setViewer(new NullViewer(this));
		
		CompareNavigator.hookNavigation(this);

		addDisposeListener(
			new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (fViewer instanceof ISelectionProvider)
						((ISelectionProvider) fViewer).removeSelectionChangedListener(CompareViewerSwitchingPane.this);
					if (fViewer instanceof StructuredViewer)
						 ((StructuredViewer) fViewer).removeDoubleClickListener(CompareViewerSwitchingPane.this);
					fViewer= null;
					fInput= null;
					fSelectionListeners= null;
				}
			}
		);
	}
	
	/**
	 * Returns the current viewer.
	 */
	public Viewer getViewer() {
		return fViewer;
	}
	
	/**
	 * Sets the current viewer.
	 */
	private void setViewer(Viewer newViewer) {
		
		if (newViewer == fViewer)
			return;
				
		boolean oldEmpty= isEmpty();

		if (fViewer != null) {
			
			if (fViewer instanceof ISelectionProvider)
				 ((ISelectionProvider) fViewer).removeSelectionChangedListener(this);
				 
			if (fViewer instanceof StructuredViewer)
				((StructuredViewer)fViewer).removeDoubleClickListener(this);

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

			if (fViewer instanceof ISelectionProvider)
				 ((ISelectionProvider) fViewer).addSelectionChangedListener(this);
			if (fViewer instanceof StructuredViewer)
				((StructuredViewer)fViewer).addDoubleClickListener(this);

			if (oldEmpty != newEmpty) {	// relayout my container
				Composite parent= getParent();
				if (parent instanceof Splitter)
					((Splitter)parent).setVisible(this, fControlVisibility ? !newEmpty : true);
			}
				
			layout(true);
		}
	}

	/**
	 * Returns <code>true</code> if no viewer is installed or if the current viewer
	 * is a <code>NullViewer</code>.
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
		fOpenListeners.add(l);
	}

	public void removeDoubleClickListener(IDoubleClickListener l) {
		fOpenListeners.remove(l);
	}

	public void doubleClick(DoubleClickEvent event) {
		Object[] listeners= fOpenListeners.getListeners();
		for (int i= 0; i < listeners.length; i++)
			((IDoubleClickListener) listeners[i]).doubleClick(event);
	}

	public ISelection getSelection() {
		if (fViewer instanceof ISelectionProvider)
			return ((ISelectionProvider) fViewer).getSelection();
		return null;
	}

	public void setSelection(ISelection s) {
		if (fViewer instanceof ISelectionProvider)
			 ((ISelectionProvider) fViewer).setSelection(s);
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
	 * If the old viewer had focus, new setInput tries to set
	 * focus on new viewer too.
	 */ 
	public void setInput(Object input) {

		if (fInput == input)
			return;
			
		boolean hadFocus= hasFocus2();
		
//		try {
//			if (fViewer != null)
//				fViewer.setInput(null);	// force save before switching viewer
//		} catch (ViewerSwitchingCancelled ex) {
//			return;
//		}

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
	
	public void setTitleArgument(String argument) {
		fTitleArgument= argument;
		updateTitle();
	}

	private void updateTitle() {
		if (fTitle != null) {
			if (fTitleArgument != null) {
				String format= CompareMessages.getString("CompareViewerSwitchingPane.Titleformat");	//$NON-NLS-1$
				String t= MessageFormat.format(format, new String[] { fTitle, fTitleArgument } );
				setText(t);
			} else
				setText(fTitle);			
		} else {
			setText("");	//$NON-NLS-1$
		}
	}

	public Object getInput() {
		return fInput;
	}

	abstract protected Viewer getViewer(Viewer oldViewer, Object input);
}
