package org.eclipse.jface.text.internal.html;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.IInformationControl;

/**
 * HTML based implementation of <code>IHoverControl</code>. 
 * Displays hover information in a web browser. Thus, the content
 * passed into  <code>setContent</code> should be valid HTML code.
 * 
 * @deprecated will be removed - present just for test purposes
 */
public class HoverBrowserControl  implements IInformationControl {
	
	/** Name of the file used as input to the web browser */
	private final static String FILE= "____$html_text___"; //$NON-NLS-1$
	
	/** The hover shell */
	private Shell fShell;
	/** The hover browser */
	private IBrowser fBrowser;
	/** The control max width constraint */
	private int fMaxWidth= -1;
	/** The control max height constraint */
	private int fMaxHeight= -1;
	
	
	/**
	 * Creates a new instance of the HTML browser control.
	 * 
	 * @param parent the parent shell
	 */
	public HoverBrowserControl(Shell parent) {
		fShell= new Shell(parent, SWT.NO_FOCUS | SWT.NO_TRIM /*| SWT.ON_TOP*/);
		fBrowser= new WebBrowser(fShell);
		
		Display display= parent.getDisplay();
		fBrowser.getControl().setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		fBrowser.getControl().setBackground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
	}
	
	/*
	 * @see IHoverControl#setContent(String)
	 */
	public void setInformation(String content) {
		
		try {
			FileWriter fw= new FileWriter(FILE);
			fw.write(content);
			fw.close();
		} catch (IOException x) {
		}
		
		fBrowser.navigate(new File(FILE).getAbsolutePath());		
	}
	
	/*
	 * @see IHoverControl#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		fBrowser.setVisible(visible);
		fShell.setVisible(visible);
	}
	
	/*
	 * @see IHoverControl#dispose()
	 */
	public void dispose() {
		
		new File(FILE).delete();
		
		if (fShell != null) {
			
			if (!fShell.isDisposed()) {
				fBrowser.dispose();
				fShell.dispose();
			}
			
			fShell= null;
			fBrowser= null;
		}
	}
	
	/*
	 * @see IHoverControl#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		Control c= fBrowser.getControl();
		c.setSize(width, height);
		fShell.setSize(width - 18, height - 2);
	}
	
	/*
	 * @see IHoverControl#setLocation(Point)
	 */
	public void setLocation(Point location) {
		Control c= fBrowser.getControl();
		c.setLocation( -1, -1);
		fShell.setLocation(location);
	}
	
	/*
	 * @see IHoverControl#setSizeConstraints(int, int)
	 */
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		fMaxWidth= maxWidth;
		fMaxHeight= maxHeight;
	}
	
	/*
	 * @see IHoverControl#computeSizeHint()
	 */
	public Point computeSizeHint() {
		return new Point(fMaxWidth, fMaxHeight);
	}
	
	/*
	 * @see IHoverControl#addDisposeListener(DisposeListener)
	 */
	public void addDisposeListener(DisposeListener listener) {
		fShell.addDisposeListener(listener);
	}	
	/*
	 * @see IHoverControl#removeDisposeListener(DisposeListener)
	 */
	public void removeDisposeListener(DisposeListener listener) {
		fShell.removeDisposeListener(listener);
	}
	
	/*
	 * @see IHoverControl#setForegroundColor(Color)
	 */
	public void setForegroundColor(Color foreground) {
		fBrowser.getControl().setForeground(foreground);
	}
	
	/*
	 * @see IHoverControl#setBackgroundColor(Color)
	 */
	public void setBackgroundColor(Color background) {
		fBrowser.getControl().setBackground(background);
	}
	
	/*
	 * @see IHoverControl#isFocusControl()
	 */
	public boolean isFocusControl() {
		return fBrowser.getControl().isFocusControl();
	}
	
	/*
	 * @see IInformationControl#setFocus()
	 */
	public void setFocus() {
		fBrowser.getControl().setFocus();
	}
	
	/*
	 * @see IInformationControl#addFocusListener(FocusListener)
	 */
	public void addFocusListener(FocusListener listener) {
		fBrowser.getControl().addFocusListener(listener);
	}

	/*
	 * @see IInformationControl#removeFocusListener(FocusListener)
	 */
	public void removeFocusListener(FocusListener listener) {
		fBrowser.getControl().removeFocusListener(listener);
	}

}

