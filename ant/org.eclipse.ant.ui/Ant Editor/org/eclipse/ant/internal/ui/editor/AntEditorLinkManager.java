/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import org.eclipse.ant.internal.ui.AntUtil;
import org.eclipse.ant.internal.ui.editor.text.XMLTextHover;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.IDocumentProvider;


class AntEditorLinkManager implements KeyListener, MouseListener, MouseMoveListener, FocusListener,
			PaintListener, IPropertyChangeListener, IDocumentListener, ITextInputListener, ITextPresentationListener {

	private final AntEditor fEditor;
	/** The session is active. */
	private boolean fActive;
	/** The currently active style range. */
	private IRegion fActiveRegion;
	/** The currently active style range as position. */
	private Position fRememberedPosition;
	/** The hand cursor. */
	private Cursor fCursor;
	/** The link color. */
	private Color fColor;
	/** The key modifier mask. */
	private int fKeyModifierMask;
	/** The URL string if a URL was detected or <code>null</code> otherwise. */
	private String fURLString;
	/** The link target. */
	private Object fLinkTarget;

	public AntEditorLinkManager(AntEditor editor) {
		fEditor = editor;
	}
	
	public void deactivate() {
		if (!fActive) {
			return;
		}
		
		repairRepresentation(false);			
		fActive= false;
	}
	
	public void install() {
		ISourceViewer sourceViewer= fEditor.getViewer();
		if (sourceViewer == null) {
			return;
		}
		
		StyledText text= sourceViewer.getTextWidget();			
		if (text == null || text.isDisposed()) {
			return;
		}
		
		updateColor(sourceViewer);
		
		sourceViewer.addTextInputListener(this);
		
		IDocument document= sourceViewer.getDocument();
		if (document != null) {
			document.addDocumentListener(this);
		}			
		
		text.addKeyListener(this);
		text.addMouseListener(this);
		text.addMouseMoveListener(this);
		text.addFocusListener(this);
		text.addPaintListener(this);
		
		((ITextViewerExtension4)sourceViewer).addTextPresentationListener(this);
		
		updateKeyModifierMask();
		
		IPreferenceStore preferenceStore= fEditor.getEditorPreferenceStore();
		preferenceStore.addPropertyChangeListener(this);
	}
	
	private void updateKeyModifierMask() {
		String modifiers=  fEditor.getEditorPreferenceStore().getString(AntEditorPreferenceConstants.EDITOR_BROWSER_LIKE_LINKS_KEY_MODIFIER); //$NON-NLS-1$
		fKeyModifierMask= computeStateMask(modifiers);
	}
	
	private int computeStateMask(String modifiers) {
		if (modifiers == null) {
			return -1;
		}
		
		if (modifiers.length() == 0) {
			return SWT.NONE;
		}
		
		int stateMask= 0;
		StringTokenizer modifierTokenizer= new StringTokenizer(modifiers, ",;.:+-* "); //$NON-NLS-1$
		while (modifierTokenizer.hasMoreTokens()) {
			int modifier= findLocalizedModifier(modifierTokenizer.nextToken());
			if (modifier == 0 || (stateMask & modifier) == modifier) {
				return -1;
			}
			stateMask= stateMask | modifier;
		}
		return stateMask;
	}
	
	private int findLocalizedModifier(String token) {
		if (token == null) {
			return 0;
		}
		
		if (token.equalsIgnoreCase(Action.findModifierString(SWT.CTRL))) {
			return SWT.CTRL;
		}
		if (token.equalsIgnoreCase(Action.findModifierString(SWT.SHIFT))) {
			return SWT.SHIFT;
		}
		if (token.equalsIgnoreCase(Action.findModifierString(SWT.ALT))) {
			return SWT.ALT;
		}
		if (token.equalsIgnoreCase(Action.findModifierString(SWT.COMMAND))) {
			return SWT.COMMAND;
		}
		
		return 0;
	}
	
	public void uninstall() {
		
		if (fColor != null) {
			fColor.dispose();
			fColor= null;
		}
		
		if (fCursor != null) {
			fCursor.dispose();
			fCursor= null;
		}
		
		ISourceViewer sourceViewer= fEditor.getViewer();
		if (sourceViewer != null) {
			sourceViewer.removeTextInputListener(this);
		}
		
		IDocumentProvider documentProvider= fEditor.getDocumentProvider();
		if (documentProvider != null) {
			IDocument document= documentProvider.getDocument(fEditor.getEditorInput());
			if (document != null)
				document.removeDocumentListener(this);
		}
		
		IPreferenceStore preferenceStore= fEditor.getEditorPreferenceStore();
		if (preferenceStore != null) {
			preferenceStore.removePropertyChangeListener(this);
		}
		
		if (sourceViewer == null) {
			return;
		}
		
		StyledText text= sourceViewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			return;
		}
		
		text.removeKeyListener(this);
		text.removeMouseListener(this);
		text.removeMouseMoveListener(this);
		text.removeFocusListener(this);
		text.removePaintListener(this);
		
		((ITextViewerExtension4)sourceViewer).removeTextPresentationListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(AntEditorPreferenceConstants.EDITOR_LINK_COLOR)) {
			ISourceViewer viewer= fEditor.getViewer();
			if (viewer != null)	 {
				updateColor(viewer);
			}
		} else if (event.getProperty().equals(AntEditorPreferenceConstants.EDITOR_BROWSER_LIKE_LINKS_KEY_MODIFIER)) {
			updateKeyModifierMask();
		}
	}
	
	private void updateColor(ISourceViewer viewer) {
		if (fColor != null) {
			fColor.dispose();
		}
		
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			return;
		}
		
		fColor= createColor(fEditor.getEditorPreferenceStore(), AntEditorPreferenceConstants.EDITOR_LINK_COLOR, text.getDisplay());
	}
	
	/**
	 * Creates a color from the information stored in the given preference store.
	 * 
	 * @param store the preference store
	 * @param key the key
	 * @param display the display
	 * @return the color or <code>null</code> if there is no such information available 
	 */
	private Color createColor(IPreferenceStore store, String key, Display display) {
		RGB rgb= null;		
		if (store.contains(key)) {
			
			if (!store.isDefault(key)) {
				rgb= PreferenceConverter.getColor(store, key);
			} else {
				rgb= PreferenceConverter.getDefaultColor(store, key);
			}
			
			if (rgb != null) {
				return new Color(display, rgb);
			}
		}
		
		return null;
	}		
	
	private void repairRepresentation() {			
		repairRepresentation(false);
	}
	
	private void repairRepresentation(boolean redrawAll) {			
		
		if (fActiveRegion == null) {
			return;
		}
		
		int offset= fActiveRegion.getOffset();
		int length= fActiveRegion.getLength();
		fActiveRegion= null;
		
		ISourceViewer viewer= fEditor.getViewer();
		if (viewer != null) {
			
			resetCursor(viewer);
			
			// Invalidate ==> remove applied text presentation
			if (!redrawAll && viewer instanceof ITextViewerExtension2) {
				((ITextViewerExtension2) viewer).invalidateTextPresentation(offset, length);
			} else {
				viewer.invalidateTextPresentation();
			}
			
			// Remove underline
			if (viewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
				offset= extension.modelOffset2WidgetOffset(offset);
			} else {
				offset -= viewer.getVisibleRegion().getOffset();
			}
			try {
				StyledText text= viewer.getTextWidget();
				text.redrawRange(offset, length, false);
			} catch (IllegalArgumentException x) {
			}
		}
	}
	
	private IRegion getCurrentTextRegion(ISourceViewer viewer) {
		
		int offset= getCurrentTextOffset(viewer);				
		if (offset == -1) {
			return null;
		}
		
		IDocument document= viewer.getDocument();
		
		IRegion target= findAndRememberLinkTarget(offset);
		if (target != null) {
			return target;
		}
		
		// URL link
		IRegion url= findAndRememberURL(document, offset);
		if (url != null) {
			return url;
		}
		
		return null;
	}
	
	private IRegion findAndRememberLinkTarget(int offset) {
		IRegion region= XMLTextHover.getRegion(fEditor.getViewer(), offset);
		if (region != null) {
			fLinkTarget= this.fEditor.findTarget(region);
			if (fLinkTarget != null) {
				return region;
			}
		}
		return null;
	}
	
	private IRegion findAndRememberURL(IDocument document, int offset) {
		fURLString= null;
		if (document == null) {
			return null;
		}
		
		IRegion lineInfo;
		String line;
		try {
			lineInfo= document.getLineInformationOfOffset(offset);
			line= document.get(lineInfo.getOffset(), lineInfo.getLength());
		} catch (BadLocationException ex) {
			return null;
		}
		
		int offsetInLine= offset - lineInfo.getOffset();
		
		int urlSeparatorOffset= line.indexOf("://"); //$NON-NLS-1$
		if (urlSeparatorOffset < 0) {
			return null;
		}
		
		// URL protocol (left to "://")
		int urlOffsetInLine= urlSeparatorOffset;
		char ch;
		do {
			urlOffsetInLine--;
			ch= ' ';
			if (urlOffsetInLine > -1) {
				ch= line.charAt(urlOffsetInLine);
			}
		} while (!Character.isWhitespace(ch));
		urlOffsetInLine++;
		
		// Right to "://"
		StringTokenizer tokenizer= new StringTokenizer(line.substring(urlSeparatorOffset + 3));
		if (!tokenizer.hasMoreTokens()) {
			return null;
		}
		
		int urlLength= tokenizer.nextToken().length() + 3 + urlSeparatorOffset - urlOffsetInLine;
		if (offsetInLine < urlOffsetInLine || offsetInLine > urlOffsetInLine + urlLength) {
			return null;
		}
		
		// Set and validate URL string
		try {
			fURLString= line.substring(urlOffsetInLine, urlOffsetInLine + urlLength);
			new URL(fURLString);
		} catch (MalformedURLException ex) {
			fURLString= null;
			return null;
		}
		
		return new Region(lineInfo.getOffset() + urlOffsetInLine, urlLength);
	}
	
	private int getCurrentTextOffset(ISourceViewer viewer) {
		
		try {					
			StyledText text= viewer.getTextWidget();			
			if (text == null || text.isDisposed()) {
				return -1;
			}
			
			Display display= text.getDisplay();				
			Point absolutePosition= display.getCursorLocation();
			Point relativePosition= text.toControl(absolutePosition);
			
			int widgetOffset= text.getOffsetAtLocation(relativePosition);
			if (viewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
				return extension.widgetOffset2ModelOffset(widgetOffset);
			}
			return widgetOffset + viewer.getVisibleRegion().getOffset();
			
		} catch (IllegalArgumentException e) {
			return -1;
		}			
	}
	
	public void applyTextPresentation(TextPresentation textPresentation) {
		if (fActiveRegion == null)
			return;
		IRegion region= textPresentation.getExtent();
		if (fActiveRegion.getOffset() + fActiveRegion.getLength() >= region.getOffset() && region.getOffset() + region.getLength() > fActiveRegion.getOffset()) {
			textPresentation.mergeStyleRange(new StyleRange(fActiveRegion.getOffset(), fActiveRegion.getLength(), fColor, null));
		}
	}
	
	private void highlightRegion(ISourceViewer viewer, IRegion region) {
		
		if (region.equals(fActiveRegion)) {
			return;
		}
		
		repairRepresentation();
		
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			return;
		}
		
		
		// Underline
		int offset= 0;
		int length= 0;
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			IRegion widgetRange= extension.modelRange2WidgetRange(region);
			if (widgetRange == null) {
				return;
			}
			
			offset= widgetRange.getOffset();
			length= widgetRange.getLength();
			
		} else {
			offset= region.getOffset() - viewer.getVisibleRegion().getOffset();
			length= region.getLength();
		}
		text.redrawRange(offset, length, false);
		
		// Invalidate region ==> apply text presentation
		fActiveRegion= region;
		if (viewer instanceof ITextViewerExtension2) {
			((ITextViewerExtension2) viewer).invalidateTextPresentation(region.getOffset(), region.getLength());
		} else {
			viewer.invalidateTextPresentation();
		}
	}
	
	private void activateCursor(ISourceViewer viewer) {
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			return;
		}
		Display display= text.getDisplay();
		if (fCursor == null) {
			fCursor= new Cursor(display, SWT.CURSOR_HAND);
		}
		text.setCursor(fCursor);
	}
	
	private void resetCursor(ISourceViewer viewer) {
		StyledText text= viewer.getTextWidget();
		if (text != null && !text.isDisposed()) {
			text.setCursor(null);
		}
		
		if (fCursor != null) {
			fCursor.dispose();
			fCursor= null;
		}
	}
	
	/*
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent event) {
		
		if (fActive) {
			deactivate();
			return;	
		}
		
		if (event.keyCode != fKeyModifierMask) {
			deactivate();
			return;
		}
		
		fActive= true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyReleased(KeyEvent event) {
		if (!fActive) {
			return;
		}
		deactivate();				
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent e) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDown(MouseEvent event) {
		
		if (!fActive) {
			return;
		}
		
		if (event.stateMask != fKeyModifierMask) {
			deactivate();
			return;	
		}
		
		if (event.button != 1) {
			deactivate();
			return;	
		}			
	}
	
	/*
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {
		
		if (!fActive) {
			fURLString= null;
			fLinkTarget= null;
			return;
		}
		
		if (e.button != 1) {
			deactivate();
			fURLString= null;
			fLinkTarget= null;
			return;
		}
		
		boolean wasActive= fCursor != null;
		
		deactivate();
		
		if (wasActive) {
			
			// URL link
			if (fURLString != null) {
				AntUtil.openBrowser(fURLString, fEditor.getEditorSite().getShell(), AntEditorMessages.getString("AntEditorLinkManager.0")); //$NON-NLS-1$
				fURLString= null;
				
			}
			if (fLinkTarget != null) {
				fEditor.openTarget(fLinkTarget);
			}
			
			fLinkTarget= null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseMove(MouseEvent event) {
		
		if (event.widget instanceof Control && !((Control) event.widget).isFocusControl()) {
			deactivate();
			return;
		}
		
		if (!fActive) {
			if (event.stateMask != fKeyModifierMask)
				return;
			// modifier was already pressed
			fActive= true;
		}
		
		ISourceViewer viewer= fEditor.getViewer();
		if (viewer == null) {
			deactivate();
			return;
		}
		
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			deactivate();
			return;
		}
		
		if ((event.stateMask & SWT.BUTTON1) != 0 && text.getSelectionCount() != 0) {
			deactivate();
			return;
		}
		
		fURLString= null;
		IRegion region= getCurrentTextRegion(viewer);
		if (region == null || region.getLength() == 0) {
			repairRepresentation();
			return;
		}
		
		highlightRegion(viewer, region);
		activateCursor(viewer);												
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
	 */
	public void focusGained(FocusEvent e) {}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
	 */
	public void focusLost(FocusEvent event) {
		deactivate();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
		if (fActive && fActiveRegion != null) {
			fRememberedPosition= new Position(fActiveRegion.getOffset(), fActiveRegion.getLength());
			try {
				event.getDocument().addPosition(fRememberedPosition);
			} catch (BadLocationException x) {
				fRememberedPosition= null;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {
		if (fRememberedPosition != null) {
			if (!fRememberedPosition.isDeleted()) {
				
				event.getDocument().removePosition(fRememberedPosition);
				fActiveRegion= new Region(fRememberedPosition.getOffset(), fRememberedPosition.getLength());
				fRememberedPosition= null;
				
				ISourceViewer viewer= fEditor.getViewer();
				if (viewer != null) {
					StyledText widget= viewer.getTextWidget();
					if (widget != null && !widget.isDisposed()) {
						widget.getDisplay().asyncExec(new Runnable() {
							public void run() {
								deactivate();
							}
						});
					}
				}
			} else {
				fActiveRegion= null;
				fRememberedPosition= null;
				deactivate();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
	 */
	public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		if (oldInput == null) {
			return;
		}
		deactivate();
		oldInput.removeDocumentListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
	 */
	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		if (newInput == null) {
			return;
		}
		newInput.addDocumentListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl(PaintEvent event) {	
		if (fActiveRegion == null) {
			return;
		}
		
		ISourceViewer viewer= fEditor.getViewer();
		if (viewer == null) {
			return;
		}
		
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			return;
		}
		
		
		int offset= 0;
		int length= 0;
		
		if (viewer instanceof ITextViewerExtension5) {
			
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			IRegion widgetRange= extension.modelRange2WidgetRange(fActiveRegion);
			if (widgetRange == null) {
				return;
			}
			
			offset= widgetRange.getOffset();
			length= widgetRange.getLength();
			
		} else {
			
			IRegion region= viewer.getVisibleRegion();			
			if (!includes(region, fActiveRegion)) {
				return;
			}		    
			
			offset= fActiveRegion.getOffset() - region.getOffset();
			length= fActiveRegion.getLength();
		}
		
		// support for bidi
		Point minLocation= getMinimumLocation(text, offset, length);
		Point maxLocation= getMaximumLocation(text, offset, length);
		
		int x1= minLocation.x;
		int x2= minLocation.x + maxLocation.x - minLocation.x - 1;
		int y= minLocation.y + text.getLineHeight() - 1;
		
		GC gc= event.gc;
		if (fColor != null && !fColor.isDisposed()) {
			gc.setForeground(fColor);
		}
		gc.drawLine(x1, y, x2, y);
	}
	
	private boolean includes(IRegion region, IRegion position) {
		return position.getOffset() >= region.getOffset() &&
		position.getOffset() + position.getLength() <= region.getOffset() + region.getLength();
	}
	
	private Point getMinimumLocation(StyledText text, int offset, int length) {
		Point minLocation= new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
		
		for (int i= 0; i <= length; i++) {
			Point location= text.getLocationAtOffset(offset + i);
			
			if (location.x < minLocation.x) {
				minLocation.x= location.x;
			}			
			if (location.y < minLocation.y) {
				minLocation.y= location.y;
			}			
		}	
		
		return minLocation;
	}
	
	private Point getMaximumLocation(StyledText text, int offset, int length) {
		Point maxLocation= new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
		
		for (int i= 0; i <= length; i++) {
			Point location= text.getLocationAtOffset(offset + i);
			
			if (location.x > maxLocation.x) {
				maxLocation.x= location.x;
			}			
			if (location.y > maxLocation.y) {
				maxLocation.y= location.y;
			}			
		}	
		
		return maxLocation;
	}
}