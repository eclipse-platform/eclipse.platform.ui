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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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
	class CompositeRulerCanvas extends Canvas {
		
		private class ListenerInfo {
			Listener listener;
			int eventType;
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
							Control c= (Control)e.widget;
							Point p= new Point(e.x, e.y);
							Point p2= c.toDisplay(p);
							menu.setLocation(p2.x, p2.y);
							menu.setVisible(true);
						}
					}
				}
			};
		}
		
		/* 
		 * @see Widget#addListener(int, Listener)
		 */
		public void addListener(int eventType, Listener listener) {
			
			super.addListener(eventType, listener);
			
			Control[] children= getChildren();
			for (int i= 0; i < children.length; i++) {
				if (children[i] != null && !children[i].isDisposed())
					children[i].addListener(eventType, listener);
			}
			
			ListenerInfo info= new ListenerInfo();
			info.listener= listener;
			info.eventType= eventType;
			fCachedListeners.add(info);
		}
		
		/*
		 * @see Widget#removeListener(int, Listener)
		 */
		public void removeListener(int eventType, Listener listener) {
			
			int length= fCachedListeners.size();
			for (int i= 0; i < length; i++) {
				ListenerInfo info= (ListenerInfo) fCachedListeners.get(i);
				if (listener == info.listener && eventType == info.eventType) {
					fCachedListeners.remove(i);
					break;
				}
			}
			
			Control[] children= getChildren();
			for (int i= 0; i < children.length; i++) {
				if (children[i] != null && !children[i].isDisposed())
					children[i].removeListener(eventType, listener);
			}
			
			super.removeListener(eventType, listener);
		}
		
		/**
		 * Tells this canvas that a child has been added.
		 */
		public void childAdded(Control child) {
			if (child != null && !child.isDisposed()) {
				
				int length= fCachedListeners.size();
				for (int i= 0; i < length; i++) {
					ListenerInfo info= (ListenerInfo) fCachedListeners.get(i);
					child.addListener(info.eventType, info.listener);
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
					child.removeListener(info.eventType, info.listener);
				}
				
				child.removeMouseListener(fMouseListener);
			}
		}
	};
	
	
	private ITextViewer fTextViewer;
	private CompositeRulerCanvas fComposite;
	
	private List fDecorators= new ArrayList(2);
	private IAnnotationModel fModel;
	
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
