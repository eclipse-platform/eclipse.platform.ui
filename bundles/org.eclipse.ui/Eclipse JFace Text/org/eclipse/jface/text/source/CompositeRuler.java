package org.eclipse.jface.text.source;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.SWTEventListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.Position;


/**
 * A vertical ruler which is connected to a text viewer.
 * Standard implementation of <code>IVerticalRuler</code>.
 * Clients may use this class as is.
 *
 * @see ITextViewer
 */
public final class CompositeRuler implements IVerticalRuler, IVerticalRulerExtension {
	
	
	/**
	 * Layout of  composite vertical ruler.
	 */
	class RulerLayout extends Layout {
		
		protected RulerLayout() {
		}
		
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			Control[] children= composite.getChildren();
			Point size= new Point(0, 0);
			for (int i= 0; i < children.length; i++) {
				Point s= children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
				size.x += s.x;
				size.y= Math.max(size.y, s.y);
			}
			return size;
		}
		
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle clArea= composite.getClientArea();
			int rulerHeight= clArea.height;
			
			int x= 0;			
			Iterator e= fDecorators.iterator();
			while (e.hasNext()) {
				IVerticalRulerColumn column= (IVerticalRulerColumn) e.next();
				int columnWidth= column.getWidth();
				column.getControl().setBounds(x, 0, columnWidth, rulerHeight);
				x += columnWidth;
			}
		}
	};
	
	/**
	 * A canvas that adds listeners to all its children.
	 */
	static class CompositeRulerCanvas extends Canvas {
		
		static class ListenerInfo {
			Class fClass;
			SWTEventListener fListener;
		};
				
		private List fCachedListeners= new ArrayList();
		private MouseListener fMouseListener;
		
		/**
		 * Creates a new composite ruler canvas.
		 */
		public CompositeRulerCanvas(Composite parent, int style) {
			super(parent, style);
			fMouseListener= new MouseAdapter() {
				public void mouseUp(MouseEvent e) {
					if (3 == e.button) {
						Menu menu= getMenu();
						if (menu != null) {
							Control c= (Control) e.widget;
							Point p= new Point(e.x, e.y);
							Point p2= c.toDisplay(p);
							menu.setLocation(p2.x, p2.y);
							menu.setVisible(true);
						}
					}
				}
			};
			super.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (fCachedListeners != null) {
						fCachedListeners.clear();
						fCachedListeners= null;
					}
				}
			});
		}
		
		private void addListener(Class clazz, Control control, SWTEventListener listener) {
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
		}
		
		private void removeListener(Class clazz, Control control, SWTEventListener listener) {
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
				
		private void addListener(Class clazz, SWTEventListener listener) {
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
		
		private void removeListener(Class clazz, SWTEventListener listener) {
			int length= fCachedListeners.size();
			for (int i= 0; i < length; i++) {
				ListenerInfo info= (ListenerInfo) fCachedListeners.get(i);
				if (listener == info.fListener && clazz.equals(info.fClass)) {
					fCachedListeners.remove(i);
					break;
				}
			}
			
			Control[] children= getChildren();
			for (int i= 0; i < children.length; i++) {
				if (children[i] != null && !children[i].isDisposed())
					removeListener(clazz, children[i], listener);
			}
		}
		
		/**
		 * Tells this canvas that a child has been added.
		 */
		public void childAdded(Control child) {
			if (child != null && !child.isDisposed()) {
				int length= fCachedListeners.size();
				for (int i= 0; i < length; i++) {
					ListenerInfo info= (ListenerInfo) fCachedListeners.get(i);
					addListener(info.fClass, child, info.fListener);
				}
				child.addMouseListener(fMouseListener);
			}
		}
		
		/**
		 * Tells this canvas that a child has been removed.
		 */
		public void childRemoved(Control child) {
			if (child != null && !child.isDisposed()) {
				int length= fCachedListeners.size();
				for (int i= 0; i < length; i++) {
					ListenerInfo info= (ListenerInfo) fCachedListeners.get(i);
					removeListener(info.fClass, child, info.fListener);
				}
				child.removeMouseListener(fMouseListener);
			}
		}
		
		/*
		 * @see Control#removeControlListener(ControlListener)
		 */
		public void removeControlListener(ControlListener listener) {
			removeListener(ControlListener.class, listener);
			super.removeControlListener(listener);
		}
		
		/*
		 * @see Control#removeFocusListener(FocusListener)
		 */
		public void removeFocusListener(FocusListener listener) {
			removeListener(FocusListener.class, listener);
			super.removeFocusListener(listener);
		}
		
		/*
		 * @see Control#removeHelpListener(HelpListener)
		 */
		public void removeHelpListener(HelpListener listener) {
			removeListener(HelpListener.class, listener);
			super.removeHelpListener(listener);
		}
		
		/*
		 * @see Control#removeKeyListener(KeyListener)
		 */
		public void removeKeyListener(KeyListener listener) {
			removeListener(KeyListener.class, listener);
			super.removeKeyListener(listener);
		}
		
		/*
		 * @see Control#removeMouseListener(MouseListener)
		 */
		public void removeMouseListener(MouseListener listener) {
			removeListener(MouseListener.class, listener);
			super.removeMouseListener(listener);
		}
		
		/*
		 * @see Control#removeMouseMoveListener(MouseMoveListener)
		 */
		public void removeMouseMoveListener(MouseMoveListener listener) {
			removeListener(MouseMoveListener.class, listener);
			super.removeMouseMoveListener(listener);
		}
		
		/*
		 * @see Control#removeMouseTrackListener(MouseTrackListener)
		 */
		public void removeMouseTrackListener(MouseTrackListener listener) {
			removeListener(MouseTrackListener.class, listener);
			super.removeMouseTrackListener(listener);
		}
		
		/*
		 * @see Control#removePaintListener(PaintListener)
		 */
		public void removePaintListener(PaintListener listener) {
			removeListener(PaintListener.class, listener);
			super.removePaintListener(listener);
		}
		
		/*
		 * @see Control#removeTraverseListener(TraverseListener)
		 */
		public void removeTraverseListener(TraverseListener listener) {
			removeListener(TraverseListener.class, listener);
			super.removeTraverseListener(listener);
		}
		
		/*
		 * @see Widget#removeDisposeListener(DisposeListener)
		 */
		public void removeDisposeListener(DisposeListener listener) {
			removeListener(DisposeListener.class, listener);
			super.removeDisposeListener(listener);
		}
		
		/*
		 * @seeControl#addControlListener(ControlListener)
		 */
		public void addControlListener(ControlListener listener) {
			super.addControlListener(listener);
			addListener(ControlListener.class, listener);
		}
		
		/*
		 * @see Control#addFocusListener(FocusListener)
		 */
		public void addFocusListener(FocusListener listener) {
			super.addFocusListener(listener);
			addListener(FocusListener.class, listener);
		}
		
		/* 
		 * @see Control#addHelpListener(HelpListener)
		 */
		public void addHelpListener(HelpListener listener) {
			super.addHelpListener(listener);
			addListener(HelpListener.class, listener);
		}

		/*
		 * @see Control#addKeyListener(KeyListener)
		 */
		public void addKeyListener(KeyListener listener) {
			super.addKeyListener(listener);
			addListener(KeyListener.class, listener);
		}
		
		/*
		 * @see Control#addMouseListener(MouseListener)
		 */
		public void addMouseListener(MouseListener listener) {
			super.addMouseListener(listener);
			addListener(MouseListener.class, listener);
		}

		/*
		 * @see Control#addMouseMoveListener(MouseMoveListener)
		 */
		public void addMouseMoveListener(MouseMoveListener listener) {
			super.addMouseMoveListener(listener);
			addListener(MouseMoveListener.class, listener);
		}

		/* 
		 * @see Control#addMouseTrackListener(MouseTrackListener)
		 */
		public void addMouseTrackListener(MouseTrackListener listener) {
			super.addMouseTrackListener(listener);
			addListener(MouseTrackListener.class, listener);
		}
		
		/* 
		 * @seeControl#addPaintListener(PaintListener)
		 */
		public void addPaintListener(PaintListener listener) {
			super.addPaintListener(listener);
			addListener(PaintListener.class, listener);
		}
		
		/* 
		 * @see Control#addTraverseListener(TraverseListener)
		 */
		public void addTraverseListener(TraverseListener listener) {
			super.addTraverseListener(listener);
			addListener(TraverseListener.class, listener);
		}
		
		/*
		 * @see Widget#addDisposeListener(DisposeListener)
		 */
		public void addDisposeListener(DisposeListener listener) {
			super.addDisposeListener(listener);
			addListener(DisposeListener.class, listener);
		}
	};
	
	
	private ITextViewer fTextViewer;
	private CompositeRulerCanvas fComposite;
	private IAnnotationModel fModel;
	
	private List fDecorators= new ArrayList(2);	
	private Point fLocation= new Point(-1, -1);
	private int fLastMouseButtonActivityLine= -1;
	
	
	/**
	 * Constructs a vertical ruler.
	 */
	public CompositeRuler() {
	}
	
	public void addDecorator(int index, IVerticalRulerColumn rulerColumn) {
		fDecorators.add(index, rulerColumn);
		if (fComposite != null && !fComposite.isDisposed()) {
			rulerColumn.createControl(this, fComposite);
			fComposite.childAdded(rulerColumn.getControl());
			layoutTextViewer();
		}
	}
	
	public void removeDecorator(int index) {
		IVerticalRulerColumn column= (IVerticalRulerColumn) fDecorators.get(index);
		fDecorators.remove(index);
		Control cc= column.getControl();
		if (cc != null && !cc.isDisposed()) {
			fComposite.childRemoved(cc);
			cc.dispose();
		}
		layoutTextViewer();
	}
	
	private void layoutTextViewer() {
		
		Control parent= fTextViewer.getTextWidget();
		
		if (fTextViewer instanceof ITextViewerExtension) {
			ITextViewerExtension extension= (ITextViewerExtension) fTextViewer;
			parent= extension.getControl();
		}
		
		if (parent instanceof Composite && !parent.isDisposed())
			((Composite) parent).layout(true);
	}
	
	/*
	 * @see IVerticalRuler#getControl()
	 */
	public Control getControl() {
		return fComposite;
	}
	
	/*
	 * @see IVerticalRuler#createControl(Composite, ITextViewer)
	 */
	public Control createControl(Composite parent, ITextViewer textViewer) {
		
		fTextViewer= textViewer;
		
		fComposite= new CompositeRulerCanvas(parent, SWT.NONE);
		fComposite.setLayout(new RulerLayout());
		
		Iterator e= fDecorators.iterator();
		while (e.hasNext()) {
			IVerticalRulerColumn column= (IVerticalRulerColumn) e.next();
			column.createControl(this, fComposite);
			fComposite.childAdded(column.getControl());
		}
		
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				fTextViewer= null;
				fComposite= null;
				fModel= null;
				fDecorators.clear();
			}
		});
		
		return fComposite;
	}
		
	/*
	 * @see IVerticalRuler#setModel(IAnnotationModel)
	 */
	public void setModel(IAnnotationModel model) {
		
		fModel= model;
		
		Iterator e= fDecorators.iterator();
		while (e.hasNext()) {
			IVerticalRulerColumn column= (IVerticalRulerColumn) e.next();
			column.setModel(model);
		}	
	}
	
	/*
	 * @see IVerticalRuler#getModel()
	 */
	public IAnnotationModel getModel() {
		return fModel;
	}
	
	/*
	 * @see IVerticalRuler#update()
	 */
	public void update() {
		if (fComposite != null && !fComposite.isDisposed()) {
			Display d= fComposite.getDisplay();
			if (d != null) {
				d.asyncExec(new Runnable() {
					public void run() {
						Iterator e= fDecorators.iterator();
						while (e.hasNext()) {
							IVerticalRulerColumn column= (IVerticalRulerColumn) e.next();
							column.redraw();
						}	
					}
				});
			}	
		}
	}
	
	/*
	 * @see IVerticalRulerExtension#setFont(Font)
	 */
	public void setFont(Font font) {
		Iterator e= fDecorators.iterator();
		while (e.hasNext()) {
			IVerticalRulerColumn column= (IVerticalRulerColumn) e.next();
			column.setFont(font);
		}	
	}
	
	/*
	 * @see IVerticalRulerInfo#getWidth()
	 */
	public int getWidth() {
		int width= 0;
		Iterator e= fDecorators.iterator();
		while (e.hasNext()) {
			IVerticalRulerColumn column= (IVerticalRulerColumn) e.next();
			width += column.getWidth();
		}
		return width;
	}
	
	/*
	 * @see IVerticalRulerInfo#getLineOfLastMouseButtonActivity()
	 */
	public int getLineOfLastMouseButtonActivity() {
		if (fLastMouseButtonActivityLine == -1)
			fLastMouseButtonActivityLine= toDocumentLineNumber(fLocation.y);
		return fLastMouseButtonActivityLine;
	}
	
	/*
	 * @see IVerticalRulerInfo#toDocumentLineNumber(int)
	 */
	public int toDocumentLineNumber(int y_coordinate) {
		
		if (fTextViewer == null || y_coordinate == -1)
			return -1;
			
		StyledText text= fTextViewer.getTextWidget();
		int line= ((y_coordinate + text.getTopPixel()) / text.getLineHeight());				
		try {
			IRegion r= fTextViewer.getVisibleRegion();
			IDocument d= fTextViewer.getDocument(); 
			line += d.getLineOfOffset(r.getOffset());
		} catch (BadLocationException x) {
		}
		
		return line;
	}
	
	/**
	 * Returns this ruler's text viewer.
	 * 
	 * @return this ruler's text viewer
	 */
	public ITextViewer getTextViewer() {
		return fTextViewer;
	}
	
	/*
	 * @see IVerticalRulerExtension#setLocationOfLastMouseButtonActivity(int, int)
	 */
	public void setLocationOfLastMouseButtonActivity(int x, int y) {
		fLocation.x= x;
		fLocation.y= y;
		fLastMouseButtonActivityLine= -1;
	}

}
