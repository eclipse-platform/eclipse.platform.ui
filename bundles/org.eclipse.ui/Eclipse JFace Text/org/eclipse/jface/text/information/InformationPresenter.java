package org.eclipse.jface.text.information;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.IWidgetTokenOwner;

import org.eclipse.jface.util.Assert;
 
/**
 * Standard implementation of <code>IInformationPresenter</code>.
 * This implementation extends <code>AbstractInformationControlManager</code>.
 * The information control is made visible on request by calling 
 * <code>showInformation</code>.<p>
 * Usually, clients instantiate this class and configure it before using it.
 */
public class InformationPresenter extends AbstractInformationControlManager implements IInformationPresenter, IWidgetTokenKeeper {
	
	/**
	 * Internal information control closer. Listens to several events issued by its subject control
	 * and closes the information control when necessary.
	 */
	class Closer implements IInformationControlCloser, ControlListener, MouseListener, 
							FocusListener, IViewportListener, KeyListener {
		
		/** The subject control */
		private Control fSubjectControl;
		/** The information control */
		private IInformationControl fInformationControl;
		/** Indicates whether this closer is active */
		private boolean fIsActive= false;
		
		/*
		 * @see IInformationControlCloser#setSubjectControl(Control)
		 */
		public void setSubjectControl(Control control) {
			fSubjectControl= control;
		}
		
		/*
		 * @see IInformationControlCloser#setInformationControl(IInformationControl)
		 */
		public void setInformationControl(IInformationControl control) {
			fInformationControl= control;
		}
		
		/*
		 * @see IInformationControlCloser#start(Rectangle)
		 */
		public void start(Rectangle informationArea) {
			
			if (fIsActive)
				return;
			fIsActive= true;
			
			if (fSubjectControl != null && ! fSubjectControl.isDisposed()) {
				fSubjectControl.addControlListener(this);
				fSubjectControl.addMouseListener(this);
				fSubjectControl.addFocusListener(this);
				fSubjectControl.addKeyListener(this);
			}
			
			if (fInformationControl != null)
				fInformationControl.addFocusListener(this);
			
			fTextViewer.addViewportListener(this);			
		}
		
		/*
		 * @see IInformationControlCloser#stop()
		 */
		public void stop() {
			
			if (!fIsActive)
				return;
			fIsActive= false;
			
			fTextViewer.removeViewportListener(this);			
			
			if (fInformationControl != null)
				fInformationControl.removeFocusListener(this);
				
			hideInformationControl();
						
			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.removeControlListener(this);
				fSubjectControl.removeMouseListener(this);
				fSubjectControl.removeFocusListener(this);
				fSubjectControl.removeKeyListener(this);
			}
		}
		
		/*
		 * @see ControlListener#controlResized(ControlEvent)
		 */
		 public void controlResized(ControlEvent e) {
			stop();
		}
		
		/*
		 * @see ControlListener#controlMoved(ControlEvent)
		 */
		 public void controlMoved(ControlEvent e) {
			stop();
		}
		
		/*
		 * @see MouseListener#mouseDown(MouseEvent)
		 */
		 public void mouseDown(MouseEvent e) {
			stop();
		}
		
		/*
		 * @see MouseListener#mouseUp(MouseEvent)
		 */
		public void mouseUp(MouseEvent e) {
		}
		
		/*
		 * @see MouseListener#mouseDoubleClick(MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent e) {
			stop();
		}
		
		/*
		 * @see FocusListener#focusGained(FocusEvent)
		 */
		public void focusGained(FocusEvent e) {
		}
		
		/*
		 * @see FocusListener#focusLost(FocusEvent)
		 */
		 public void focusLost(FocusEvent e) {
			Display d= fSubjectControl.getDisplay();
			d.asyncExec(new Runnable() {
				public void run() {
					if ( !fInformationControl.isFocusControl())
						stop();
				}
			});
		}
		
		/*
		 * @see IViewportListenerListener#viewportChanged(int)
		 */
		public void viewportChanged(int topIndex) {
			stop();
		}
		
		/*
		 * @see KeyListener#keyPressed(KeyEvent)
		 */
		public void keyPressed(KeyEvent e) {
			stop();
		}
		
		/*
		 * @see KeyListener#keyReleased(KeyEvent)
		 */
		public void keyReleased(KeyEvent e) {
		}
	};	
	
	
	/** The text viewer this information presenter works on */
	private ITextViewer fTextViewer;
	/** The map of <code>IInformationProvider</code> objects */
	private Map fProviders;
	
	
	/**
	 * Creates a new information presenter that uses the given information control creator.
	 * The presenter is not installed on any text viewer yet. By default, an information
	 * control closer is set that closes the information control in the event of key strokes, 
	 * resizing, moves,focus changes, mouse clicks, and disposal - all of those applied to
	 * the information control's parent control.
	 * 
	 * @param creator the information control creator to be used
	 */
	public InformationPresenter(IInformationControlCreator creator) {
		super(creator);
		setCloser(new Closer());
		takesFocusWhenVisible(true);
	}
	
	/**
	 * Registers a given information provider for a particular content type.
	 * If there is already a provider registered for this type, the new provider 
	 * is registered instead of the old one.
	 *
	 * @param provider the information provider to register, or <code>null</code> to remove an existing one
	 * @param contentType the content type under which to register
	 */	
	 public void setInformationProvider(IInformationProvider provider, String contentType) {
		
		Assert.isNotNull(contentType);
					
		if (fProviders == null)
			fProviders= new HashMap();
			
		if (provider == null)
			fProviders.remove(contentType);
		else
			fProviders.put(contentType, provider);
	}
	
	/*
	 * @see IInformationPresenter#getInformationProvider(String)
	 */
	public IInformationProvider getInformationProvider(String contentType) {
		if (fProviders == null)
			return null;
						
		return (IInformationProvider) fProviders.get(contentType);
	}
	
	/*
	 * @see AbstractInformationControlManager#computeInformation()
	 */
	protected void computeInformation() {
		int offset= fTextViewer.getSelectedRange().x;
		if (offset == -1)
			return;
			
			
		IInformationProvider provider= null;
		try {
			IDocument document= fTextViewer.getDocument();
			String type= document.getContentType(offset);
			provider= getInformationProvider(type);
		} catch (BadLocationException x) {
		}
		if (provider == null)
			return;
			
		IRegion subject= provider.getSubject(fTextViewer, offset);
		if (subject == null)
			return;
			
		setInformation(provider.getInformation(fTextViewer, subject), computeArea(subject));
	}
	
	/**
	 * Determines graphical area covered by the given text region.
	 *
	 * @param region the region whose graphical extend must be computed
	 * @return the graphical extend of the given region
	 */
	private Rectangle computeArea(IRegion region) {
		
		StyledText styledText= fTextViewer.getTextWidget();
		
		IRegion visible= fTextViewer.getVisibleRegion();
		int start= region.getOffset() - visible.getOffset();
		int end= start + region.getLength();
		
		Point upperLeft= styledText.getLocationAtOffset(start);
		Point lowerRight= new Point(upperLeft.x, upperLeft.y);
		
		for (int i= start +1; i < end; i++) {
			
			Point p= styledText.getLocationAtOffset(i);
			
			if (upperLeft.x > p.x)
				upperLeft.x= p.x;
				
			if (upperLeft.y > p.y)
				upperLeft.y= p.y;
				
			if (lowerRight.x  < p.x)
				lowerRight.x= p.x;
				
			if (lowerRight.y < p.y)
				lowerRight.y= p.y;
		}
		
		GC gc= new GC(styledText);
		lowerRight.x +=  gc.getFontMetrics().getAverageCharWidth();
		lowerRight.y += styledText.getLineHeight();
		gc.dispose();
		
		int width= lowerRight.x - upperLeft.x;
		int height= lowerRight.y - upperLeft.y;
		return new Rectangle(upperLeft.x, upperLeft.y, width, height);
	}
	
	/*
	 * @see IInformationPresenter#install(ITextViewer)
	 */
	public void install(ITextViewer textViewer) {
		fTextViewer= textViewer;
		install(fTextViewer.getTextWidget());
	}
	
	/*
	 * @see IInformationPresenter#uninstall()
	 */
	public void uninstall() {
		dispose();
	}
	
	/*
	 * @see AbstractInformationControlManager#showInformationControl(Rectangle)
	 */
	protected void showInformationControl(Rectangle subjectArea) {
		if (fTextViewer instanceof IWidgetTokenOwner) {
			IWidgetTokenOwner owner= (IWidgetTokenOwner) fTextViewer;
			if (owner.requestWidgetToken(this))
				super.showInformationControl(subjectArea);
		}
	}

	/*
	 * @see AbstractInformationControlManager#hideInformationControl()
	 */
	protected void hideInformationControl() {
		try {
			super.hideInformationControl();
		} finally {
			if (fTextViewer instanceof IWidgetTokenOwner) {
				IWidgetTokenOwner owner= (IWidgetTokenOwner) fTextViewer;
				owner.releaseWidgetToken(this);
			}
		}
	}
	
	/*
	 * @see AbstractInformationControlManager#handleInformationControlDisposed()
	 */
	protected void handleInformationControlDisposed() {
		try {
			super.handleInformationControlDisposed();
		} finally {
			if (fTextViewer instanceof IWidgetTokenOwner) {
				IWidgetTokenOwner owner= (IWidgetTokenOwner) fTextViewer;
				owner.releaseWidgetToken(this);
			}
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.IWidgetTokenKeeper#requestWidgetToken(IWidgetTokenOwner)
	 */
	public boolean requestWidgetToken(IWidgetTokenOwner owner) {
		return false;
	}
}

