package org.eclipse.update.ui.forms.internal;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;
import java.util.Hashtable;

import org.eclipse.update.ui.forms.internal.engine.*;
import org.eclipse.core.runtime.CoreException;

public class HTMLFormLabel extends Canvas {
	private boolean hasFocus;
	private String text;
	private TextModel model;
	private Hashtable objectTable = new Hashtable();
	private int marginWidth = 2;
	private int marginHeight = 2;
	
	public boolean getFocus() {
		return hasFocus;
	}
	
	/**
	 * Constructor for SelectableFormLabel
	 */
	public HTMLFormLabel(Composite parent, int style) {
		super(parent, style);
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				paint(e);
			}
		});
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.character == '\r') {
					// Activation
					activateSelectedLink();
				}
			}
		});
		addListener(SWT.Traverse, new Listener () {
			public void handleEvent(Event e) {
				if (e.detail == SWT.TRAVERSE_TAB_NEXT)
				   e.doit = advance(true);
				else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS)
					e.doit = advance(false);
				else if (e.detail != SWT.TRAVERSE_RETURN)
					e.doit = true;
			}
		});
		addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (!hasFocus) {
				   hasFocus=true;
				   handleFocusChange();
				}
			}
			public void focusLost(FocusEvent e) {
				if (hasFocus) {
					hasFocus=false;
					handleFocusChange();
				}
			}
		});
		model = new TextModel();
	}
	
	public HyperlinkSettings getHyperlinkSettings() {
		return model.getHyperlinkSettings();
	}
	
	public void setHyperlinkSettings(HyperlinkSettings settings) {
		model.setHyperlinkSettings(settings);
	}
	
	private boolean advance(boolean next) {
		boolean valid = model.traverseLinks(next);
		if (valid) enterLink(model.getSelectedLink());
		redraw();
		return !valid;
	}
	
	private void handleFocusChange() {
		if (hasFocus) {
			model.traverseLinks(true);
			enterLink(model.getSelectedLink());
		}
		else {
			model.deselectCurrentLink();
		}
	   	redraw();
	}
	
	private void enterLink(IHyperlinkSegment link) {
		if (link==null) return;
		IHyperlinkListener listener = link.getListener(objectTable);
		if (listener!=null)
		   listener.linkEntered(this);
	}
	
	private void activateSelectedLink() {
		IHyperlinkSegment link = model.getSelectedLink();
		if (link!=null) activateLink(link);
	}
	
	private void activateLink(IHyperlinkSegment link) {
		IHyperlinkListener listener = link.getListener(objectTable);
		if (listener!=null)
			listener.linkActivated(this);
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed) {
		int innerWidth = wHint;
		if (innerWidth!=SWT.DEFAULT)
		   innerWidth -= marginWidth*2;
		Point textSize = computeTextSize(innerWidth);
		int textWidth = textSize.x + 2*marginWidth;
		int textHeight = textSize.y + 2*marginHeight;
		return new Point(textWidth, textHeight);
	}
	
	private Point computeTextSize(int wHint) {
		IParagraph [] paragraphs = model.getParagraphs();
		
		GC gc = new GC(this);
		gc.setFont(getFont());
		
		Locator loc = new Locator();
		
		int width = 0;
		int height = 0;
		
		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();
		
		for (int i=0; i<paragraphs.length; i++) {
			IParagraph p = paragraphs[i];
			
			if (i>0) loc.y += lineHeight;
			
			loc.rowHeight = 0;
			
			IParagraphSegment [] segments = p.getSegments();
			for (int j=0; j<segments.length; j++) {
				IParagraphSegment segment = segments[j];
				segment.advanceLocator(gc, wHint, loc, objectTable);
				width = Math.max(width, loc.width);
			}
			loc.y += loc.rowHeight;
		}
		gc.dispose();
		return new Point(width, loc.y);
	}
	
	protected void paint(PaintEvent e) {
		int width = getClientArea().width;
		IParagraph [] paragraphs = model.getParagraphs();
		
		GC gc = e.gc;
		gc.setFont(getFont());
		gc.setForeground(getForeground());
		gc.setBackground(getBackground());
		
		Locator loc = new Locator();
		
		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();
		
		IHyperlinkSegment selected = model.getSelectedLink();
		
		for (int i=0; i<paragraphs.length; i++) {
			IParagraph p = paragraphs[i];
			
			if (i>0) loc.y += lineHeight;
			
			loc.rowHeight = 0;
			loc.x = 0;
			
			IParagraphSegment [] segments = p.getSegments();
			for (int j=0; j<segments.length; j++) {
				IParagraphSegment segment = segments[j];
				boolean doSelect = false;
				if (selected!=null && segment.equals(selected))
				   doSelect = true;
				segment.paint(gc, width, loc, objectTable, doSelect);
			}
			loc.y += loc.rowHeight;
		}
	}
	
	public void registerTextObject(String key, Object value) {
		objectTable.put(key, value);
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text, boolean parseTags, boolean expandURLs) {
		this.text = text;
		try {
			if (parseTags)
		   		model.parseTaggedText(text);
			else
		   		model.parseRegularText(text, expandURLs);
		}
		catch (CoreException e) {
		}
	}
}