/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;


import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.GestureListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension5;


/**
 * Standard implementation of
 * {@link org.eclipse.jface.text.source.IVerticalRuler}.
 * <p>
 * This ruler does not have a a visual representation of its own. The
 * presentation comes from the configurable list of vertical ruler columns. Such
 * columns must implement the
 * {@link org.eclipse.jface.text.source.IVerticalRulerColumn}. interface.</p>
 * <p>
 * Clients may instantiate and configure this class.</p>
 *
 * @see org.eclipse.jface.text.source.IVerticalRulerColumn
 * @see org.eclipse.jface.text.ITextViewer
 * @since 2.0
 */
public class CompositeRuler implements IVerticalRuler, IVerticalRulerExtension, IVerticalRulerInfoExtension {


	/**
	 * Layout of the composite vertical ruler. Arranges the list of columns.
	 */
	class RulerLayout extends Layout {

		/**
		 * Creates the new ruler layout.
		 */
		protected RulerLayout() {
		}

		@Override
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			Control[] children= composite.getChildren();
			Point size= new Point(0, 0);
			for (Control element : children) {
				Point s= element.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
				size.x += s.x;
				size.y= Math.max(size.y, s.y);
			}
			size.x += (Math.max(0, children.length -1) * fGap);
			return size;
		}

		@Override
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle clArea= composite.getClientArea();
			int rulerHeight= clArea.height;

			int x= 0;
			Iterator<IVerticalRulerColumn> e= fDecorators.iterator();
			while (e.hasNext()) {
				IVerticalRulerColumn column= e.next();
				int columnWidth= column.getWidth();
				column.getControl().setBounds(x, 0, columnWidth, rulerHeight);
				x += (columnWidth + fGap);
			}
		}
	}

	/**
	 * A canvas that adds listeners to all its children. Used by the implementation of the
	 * vertical ruler to propagate listener additions and removals to the ruler's columns.
	 */
	static class CompositeRulerCanvas extends Canvas {

		/**
		 * Keeps the information for which event type a listener object has been added.
		 */
		static class ListenerInfo {
			Class<? extends EventListener> fClass;
			EventListener fListener;
		}

		/** The list of listeners added to this canvas. */
		private List<ListenerInfo> fCachedListeners= new ArrayList<>();
		/**
		 * Internal listener for opening the context menu.
		 * @since 3.0
		 */
		private Listener fMenuDetectListener;

		/**
		 * Creates a new composite ruler canvas.
		 *
		 * @param parent the parent composite
		 * @param style the SWT styles
		 */
		public CompositeRulerCanvas(Composite parent, int style) {
			super(parent, style);
			fMenuDetectListener= new Listener() {
				@Override
				public void handleEvent(Event event) {
				  	if (event.type == SWT.MenuDetect) {
						Menu menu= getMenu();
						if (menu != null) {
							menu.setLocation(event.x, event.y);
							menu.setVisible(true);
						}
					}
				}
			};
			super.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					if (fCachedListeners != null) {
						fCachedListeners.clear();
						fCachedListeners= null;
					}
				}
			});
		}

		/**
		 * Adds the given listener object as listener of the given type (<code>clazz</code>) to
		 * the given control.
		 *
		 * @param clazz the listener type
		 * @param control the control to add the listener to
		 * @param listener the listener to be added
		 */
		private void addListener(Class<? extends EventListener> clazz, Control control, EventListener listener) {
			if (ControlListener.class.equals(clazz)) {
				control. addControlListener((ControlListener) listener);
				return;
			}
			if (FocusListener.class.equals(clazz)) {
				control. addFocusListener((FocusListener) listener);
				return;
			}
			if (HelpListener.class.equals(clazz)) {
				control. addHelpListener((HelpListener) listener);
				return;
			}
			if (KeyListener.class.equals(clazz)) {
				control. addKeyListener((KeyListener) listener);
				return;
			}
			if (MouseListener.class.equals(clazz)) {
				control. addMouseListener((MouseListener) listener);
				return;
			}
			if (MouseMoveListener.class.equals(clazz)) {
				control. addMouseMoveListener((MouseMoveListener) listener);
				return;
			}
			if (MouseTrackListener.class.equals(clazz)) {
				control. addMouseTrackListener((MouseTrackListener) listener);
				return;
			}
			if (PaintListener.class.equals(clazz)) {
				control. addPaintListener((PaintListener) listener);
				return;
			}
			if (TraverseListener.class.equals(clazz)) {
				control. addTraverseListener((TraverseListener) listener);
				return;
			}
			if (DisposeListener.class.equals(clazz)) {
				control. addDisposeListener((DisposeListener) listener);
				return;
			}
			if (GestureListener.class.equals(clazz)) {
				control. addGestureListener((GestureListener) listener);
				return;
			}
		}

		/**
		 * Removes the given listener object as listener of the given type (<code>clazz</code>) from
		 * the given control.
		 *
		 * @param clazz the listener type
		 * @param control the control to remove the listener from
		 * @param listener the listener to be removed
		 */
		private void removeListener(Class<? extends EventListener> clazz, Control control, EventListener listener) {
			if (ControlListener.class.equals(clazz)) {
				control. removeControlListener((ControlListener) listener);
				return;
			}
			if (FocusListener.class.equals(clazz)) {
				control. removeFocusListener((FocusListener) listener);
				return;
			}
			if (HelpListener.class.equals(clazz)) {
				control. removeHelpListener((HelpListener) listener);
				return;
			}
			if (KeyListener.class.equals(clazz)) {
				control. removeKeyListener((KeyListener) listener);
				return;
			}
			if (MouseListener.class.equals(clazz)) {
				control. removeMouseListener((MouseListener) listener);
				return;
			}
			if (MouseMoveListener.class.equals(clazz)) {
				control. removeMouseMoveListener((MouseMoveListener) listener);
				return;
			}
			if (MouseTrackListener.class.equals(clazz)) {
				control. removeMouseTrackListener((MouseTrackListener) listener);
				return;
			}
			if (PaintListener.class.equals(clazz)) {
				control. removePaintListener((PaintListener) listener);
				return;
			}
			if (TraverseListener.class.equals(clazz)) {
				control. removeTraverseListener((TraverseListener) listener);
				return;
			}
			if (DisposeListener.class.equals(clazz)) {
				control. removeDisposeListener((DisposeListener) listener);
				return;
			}
		}

		/**
		 * Adds the given listener object to the internal book keeping under
		 * the given listener type (<code>clazz</code>).
		 *
		 * @param clazz the listener type
		 * @param listener the listener object
		 */
		private void addListener(Class<? extends EventListener> clazz, EventListener listener) {
			Control[] children= getChildren();
			for (int i= 0; i < children.length; i++) {
				if (children[i] != null && !children[i].isDisposed())
					addListener(clazz, children[i], listener);
			}

			ListenerInfo info= new ListenerInfo();
			info.fClass= clazz;
			info.fListener= listener;
			fCachedListeners.add(info);
		}

		/**
		 * Removes the given listener object from the internal book keeping under
		 * the given listener type (<code>clazz</code>).
		 *
		 * @param clazz the listener type
		 * @param listener the listener object
		 */
		private void removeListener(Class<? extends EventListener> clazz, EventListener listener) {
			// Keep as first statement to ensure checkWidget() is called.
			Control[] children= getChildren();

			if (fCachedListeners == null) // already disposed
				return;

			int length= fCachedListeners.size();
			for (int i= 0; i < length; i++) {
				ListenerInfo info= fCachedListeners.get(i);
				if (listener == info.fListener && clazz.equals(info.fClass)) {
					fCachedListeners.remove(i);
					break;
				}
			}

			for (int i= 0; i < children.length; i++) {
				if (children[i] != null && !children[i].isDisposed())
					removeListener(clazz, children[i], listener);
			}
		}

		/**
		 * Tells this canvas that a child has been added.
		 *
		 * @param child the child
		 */
		public void childAdded(Control child) {
			if (child != null && !child.isDisposed()) {
				int length= fCachedListeners.size();
				for (int i= 0; i < length; i++) {
					ListenerInfo info= fCachedListeners.get(i);
					addListener(info.fClass, child, info.fListener);
				}
				child.addListener(SWT.MenuDetect, fMenuDetectListener);
			}
		}

		/**
		 * Tells this canvas that a child has been removed.
		 *
		 * @param child the child
		 */
		public void childRemoved(Control child) {
			if (child != null && !child.isDisposed()) {
				int length= fCachedListeners.size();
				for (int i= 0; i < length; i++) {
					ListenerInfo info= fCachedListeners.get(i);
					removeListener(info.fClass, child, info.fListener);
				}
				child.removeListener(SWT.MenuDetect, fMenuDetectListener);
			}
		}

		@Override
		public void removeControlListener(ControlListener listener) {
			removeListener(ControlListener.class, listener);
			super.removeControlListener(listener);
		}

		@Override
		public void removeFocusListener(FocusListener listener) {
			removeListener(FocusListener.class, listener);
			super.removeFocusListener(listener);
		}

		@Override
		public void removeHelpListener(HelpListener listener) {
			removeListener(HelpListener.class, listener);
			super.removeHelpListener(listener);
		}

		@Override
		public void removeKeyListener(KeyListener listener) {
			removeListener(KeyListener.class, listener);
			super.removeKeyListener(listener);
		}

		@Override
		public void removeMouseListener(MouseListener listener) {
			removeListener(MouseListener.class, listener);
			super.removeMouseListener(listener);
		}

		@Override
		public void removeMouseMoveListener(MouseMoveListener listener) {
			removeListener(MouseMoveListener.class, listener);
			super.removeMouseMoveListener(listener);
		}

		@Override
		public void removeMouseTrackListener(MouseTrackListener listener) {
			removeListener(MouseTrackListener.class, listener);
			super.removeMouseTrackListener(listener);
		}

		@Override
		public void removePaintListener(PaintListener listener) {
			removeListener(PaintListener.class, listener);
			super.removePaintListener(listener);
		}

		@Override
		public void removeTraverseListener(TraverseListener listener) {
			removeListener(TraverseListener.class, listener);
			super.removeTraverseListener(listener);
		}

		@Override
		public void removeDisposeListener(DisposeListener listener) {
			removeListener(DisposeListener.class, listener);
			super.removeDisposeListener(listener);
		}

		@Override
		public void removeGestureListener(GestureListener listener) {
			removeListener(GestureListener.class, listener);
			super.removeGestureListener(listener);
		}

		/*
		 * @seeControl#addControlListener(ControlListener)
		 */
		@Override
		public void addControlListener(ControlListener listener) {
			super.addControlListener(listener);
			addListener(ControlListener.class, listener);
		}

		@Override
		public void addFocusListener(FocusListener listener) {
			super.addFocusListener(listener);
			addListener(FocusListener.class, listener);
		}

		@Override
		public void addHelpListener(HelpListener listener) {
			super.addHelpListener(listener);
			addListener(HelpListener.class, listener);
		}

		@Override
		public void addKeyListener(KeyListener listener) {
			super.addKeyListener(listener);
			addListener(KeyListener.class, listener);
		}

		@Override
		public void addMouseListener(MouseListener listener) {
			super.addMouseListener(listener);
			addListener(MouseListener.class, listener);
		}

		@Override
		public void addMouseMoveListener(MouseMoveListener listener) {
			super.addMouseMoveListener(listener);
			addListener(MouseMoveListener.class, listener);
		}

		@Override
		public void addMouseTrackListener(MouseTrackListener listener) {
			super.addMouseTrackListener(listener);
			addListener(MouseTrackListener.class, listener);
		}

		/*
		 * @seeControl#addPaintListener(PaintListener)
		 */
		@Override
		public void addPaintListener(PaintListener listener) {
			super.addPaintListener(listener);
			addListener(PaintListener.class, listener);
		}

		@Override
		public void addTraverseListener(TraverseListener listener) {
			super.addTraverseListener(listener);
			addListener(TraverseListener.class, listener);
		}

		@Override
		public void addDisposeListener(DisposeListener listener) {
			super.addDisposeListener(listener);
			addListener(DisposeListener.class, listener);
		}

		@Override
		public void addGestureListener(GestureListener listener) {
			super.addGestureListener(listener);
			addListener(GestureListener.class, listener);
		}
	}

	/** The ruler's viewer */
	private ITextViewer fTextViewer;
	/** The ruler's canvas to which to add the ruler columns */
	private CompositeRulerCanvas fComposite;
	/** The ruler's annotation model */
	private IAnnotationModel fModel;
	/** The list of columns */
	private List<IVerticalRulerColumn> fDecorators= new ArrayList<>(2);
	/** The cached location of the last mouse button activity */
	private Point fLocation= new Point(-1, -1);
	/** The cached line of the list mouse button activity */
	private int fLastMouseButtonActivityLine= -1;
	/** The gap between the individual columns of this composite ruler */
	private int fGap;
	/**
	 * The set of annotation listeners.
	 * @since 3.0
	 */
	private Set<IVerticalRulerListener> fAnnotationListeners= new HashSet<>();


	/**
	 * Constructs a new composite vertical ruler.
	 */
	public CompositeRuler() {
		this(0);
	}

	/**
	 * Constructs a new composite ruler with the given gap between its columns.
	 *
	 * @param gap the gap
	 */
	public CompositeRuler(int gap) {
		fGap= gap;
	}

	/**
	 * Inserts the given column at the specified slot to this composite ruler.
	 * Columns are counted from left to right.
	 *
	 * @param index the index
	 * @param rulerColumn the decorator to be inserted
	 */
	public void addDecorator(int index, IVerticalRulerColumn rulerColumn) {
		rulerColumn.setModel(getModel());

		if (index > fDecorators.size())
			fDecorators.add(rulerColumn);
		else
			fDecorators.add(index, rulerColumn);

		if (fComposite != null && !fComposite.isDisposed()) {
			rulerColumn.createControl(this, fComposite);
			fComposite.childAdded(rulerColumn.getControl());
			layoutTextViewer();
		}
	}

	/**
	 * Removes the decorator in the specified slot from this composite ruler.
	 *
	 * @param index the index
	 */
	public void removeDecorator(int index) {
		IVerticalRulerColumn rulerColumn= fDecorators.get(index);
		removeDecorator(rulerColumn);
	}

	/**
	 * Removes the given decorator from the composite ruler.
	 *
	 * @param rulerColumn the ruler column to be removed
	 * @since 3.0
	 */
	public void removeDecorator(IVerticalRulerColumn rulerColumn) {
		fDecorators.remove(rulerColumn);
		if (rulerColumn != null) {
			Control cc= rulerColumn.getControl();
			if (cc != null && !cc.isDisposed()) {
				fComposite.childRemoved(cc);
				cc.dispose();
			}
		}
		layoutTextViewer();
	}

	/**
	 * Layouts the text viewer. This also causes this ruler to get
	 * be layouted.
	 */
	private void layoutTextViewer() {

		Control parent= fTextViewer.getTextWidget();

		if (fTextViewer instanceof ITextViewerExtension) {
			ITextViewerExtension extension= (ITextViewerExtension) fTextViewer;
			parent= extension.getControl();
		}

		if (parent instanceof Composite && !parent.isDisposed())
			((Composite) parent).layout(true);
	}

	@Override
	public Control getControl() {
		return fComposite;
	}

	@Override
	public Control createControl(Composite parent, ITextViewer textViewer) {

		fTextViewer= textViewer;

		fComposite= new CompositeRulerCanvas(parent, SWT.NONE);
		fComposite.setLayout(new RulerLayout());

		Iterator<IVerticalRulerColumn> iter= fDecorators.iterator();
		while (iter.hasNext()) {
			IVerticalRulerColumn column= iter.next();
			column.createControl(this, fComposite);
			fComposite.childAdded(column.getControl());
		}

		return fComposite;
	}

	@Override
	public void setModel(IAnnotationModel model) {

		fModel= model;

		Iterator<IVerticalRulerColumn> e= fDecorators.iterator();
		while (e.hasNext()) {
			IVerticalRulerColumn column= e.next();
			column.setModel(model);
		}
	}

	@Override
	public IAnnotationModel getModel() {
		return fModel;
	}

	@Override
	public void update() {
		if (fComposite != null && !fComposite.isDisposed()) {
			Display d= fComposite.getDisplay();
			if (d != null) {
				d.asyncExec(new Runnable() {
					@Override
					public void run() {
						immediateUpdate();
					}
				});
			}
		}
	}

	/**
	 * Immediately redraws the entire ruler (without asynchronous posting).
	 *
	 * @since 3.2
	 */
	public void immediateUpdate() {
		Iterator<IVerticalRulerColumn> e= fDecorators.iterator();
		while (e.hasNext()) {
			IVerticalRulerColumn column= e.next();
			column.redraw();
		}
	}

	@Override
	public void setFont(Font font) {
		Iterator<IVerticalRulerColumn> e= fDecorators.iterator();
		while (e.hasNext()) {
			IVerticalRulerColumn column= e.next();
			column.setFont(font);
		}
	}

	@Override
	public int getWidth() {
		int width= 0;
		Iterator<IVerticalRulerColumn> e= fDecorators.iterator();
		while (e.hasNext()) {
			IVerticalRulerColumn column= e.next();
			width += (column.getWidth() + fGap);
		}
		return Math.max(0, width - fGap);
	}

	@Override
	public int getLineOfLastMouseButtonActivity() {
		if (fLastMouseButtonActivityLine == -1)
			fLastMouseButtonActivityLine= toDocumentLineNumber(fLocation.y);
		else if (fTextViewer.getDocument() == null || fLastMouseButtonActivityLine >= fTextViewer.getDocument().getNumberOfLines())
			fLastMouseButtonActivityLine= -1;
		return fLastMouseButtonActivityLine;
	}

	@Override
	public int toDocumentLineNumber(int y_coordinate) {
		if (fTextViewer == null || y_coordinate == -1)
			return -1;

		StyledText text= fTextViewer.getTextWidget();
		int line= text.getLineIndex(y_coordinate);

		if (line == text.getLineCount() - 1) {
			// check whether y_coordinate exceeds last line
			if (y_coordinate > text.getLinePixel(line + 1))
				return -1;
		}

		return widgetLine2ModelLine(fTextViewer, line);
	}

	/**
	 * Returns the line in the given viewer's document that correspond to the given
	 * line of the viewer's widget.
	 *
	 * @param viewer the viewer
	 * @param widgetLine the widget line
	 * @return the corresponding line the viewer's document
	 * @since 2.1
	 */
	protected final static int widgetLine2ModelLine(ITextViewer viewer, int widgetLine) {

		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			return extension.widgetLine2ModelLine(widgetLine);
		}

		try {
			IRegion r= viewer.getVisibleRegion();
			IDocument d= viewer.getDocument();
			return widgetLine += d.getLineOfOffset(r.getOffset());
		} catch (BadLocationException x) {
		}
		return widgetLine;
	}

	/**
	 * Returns this ruler's text viewer.
	 *
	 * @return this ruler's text viewer
	 */
	public ITextViewer getTextViewer() {
		return fTextViewer;
	}

	@Override
	public void setLocationOfLastMouseButtonActivity(int x, int y) {
		fLocation.x= x;
		fLocation.y= y;
		fLastMouseButtonActivityLine= -1;
	}

	/**
	 * Returns an iterator over the <code>IVerticalRulerColumns</code> that make up this
	 * composite column.
	 *
	 * @return an iterator over the contained columns.
	 * @since 3.0
	 */
	public Iterator<IVerticalRulerColumn> getDecoratorIterator() {
		Assert.isNotNull(fDecorators, "fDecorators must be initialized"); //$NON-NLS-1$
		return fDecorators.iterator();
	}

	@Override
	public IAnnotationHover getHover() {
		return null;
	}

	@Override
	public void addVerticalRulerListener(IVerticalRulerListener listener) {
		fAnnotationListeners.add(listener);
	}

	@Override
	public void removeVerticalRulerListener(IVerticalRulerListener listener) {
		fAnnotationListeners.remove(listener);
	}

	/**
	 * Fires the annotation selected event to all registered vertical ruler
	 * listeners.
	 * TODO use robust iterators
	 *
	 * @param event the event to fire
	 * @since 3.0
	 */
	public void fireAnnotationSelected(VerticalRulerEvent event) {
		// forward to listeners
		for (IVerticalRulerListener listener : fAnnotationListeners) {
			listener.annotationSelected(event);
		}
	}

	/**
	 * Fires the annotation default selected event to all registered vertical
	 * ruler listeners.
	 * TODO use robust iterators
	 *
	 * @param event the event to fire
	 * @since 3.0
	 */
	public void fireAnnotationDefaultSelected(VerticalRulerEvent event) {
		// forward to listeners
		for (IVerticalRulerListener listener : fAnnotationListeners) {
			listener.annotationDefaultSelected(event);
		}
	}

	/**
	 * Informs all registered vertical ruler listeners that the content menu on a selected annotation\
	 * is about to be shown.
	 * TODO use robust iterators
	 *
	 * @param event the event to fire
	 * @param menu the menu that is about to be shown
	 * @since 3.0
	 */
	public void fireAnnotationContextMenuAboutToShow(VerticalRulerEvent event, Menu menu) {
		// forward to listeners
		for (IVerticalRulerListener listener : fAnnotationListeners) {
			listener.annotationContextMenuAboutToShow(event, menu);
		}
	}

	/**
	 * Relayouts the receiver.
	 *
	 * @since 3.3
	 */
	public void relayout() {
		layoutTextViewer();
	}
}
