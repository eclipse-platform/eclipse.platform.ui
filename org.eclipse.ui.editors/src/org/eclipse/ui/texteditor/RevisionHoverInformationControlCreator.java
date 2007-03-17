/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.internal.text.html.HTMLTextPresenter;

import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension4;

import org.eclipse.ui.editors.text.EditorsUI;


/**
 * The revision information control creator
 * that can show the tool tip affordance.
 * 
 * @since 3.3
 */
class RevisionHoverInformationControlCreator extends AbstractReusableInformationControlCreator {

	private static final String fgStyleSheet= "/* Font definitions */\n" + //$NON-NLS-1$
	"body, h1, h2, h3, h4, h5, h6, p, table, td, caption, th, ul, ol, dl, li, dd, dt {font-family: sans-serif; font-size: 9pt }\n" + //$NON-NLS-1$ 
	"pre				{ font-family: monospace; font-size: 9pt }\n" + //$NON-NLS-1$
	"\n" + //$NON-NLS-1$
	"/* Margins */\n" + //$NON-NLS-1$
	"body	     { overflow: auto; margin-top: 0; margin-bottom: 4; margin-left: 3; margin-right: 0 }\n" + //$NON-NLS-1$ 
	"h1           { margin-top: 5; margin-bottom: 1 }	\n" + //$NON-NLS-1$
	"h2           { margin-top: 25; margin-bottom: 3 }\n" + //$NON-NLS-1$
	"h3           { margin-top: 20; margin-bottom: 3 }\n" + //$NON-NLS-1$
	"h4           { margin-top: 20; margin-bottom: 3 }\n" + //$NON-NLS-1$
	"h5           { margin-top: 0; margin-bottom: 0 }\n" + //$NON-NLS-1$
	"p            { margin-top: 10px; margin-bottom: 10px }\n" + //$NON-NLS-1$
	"pre	         { margin-left: 6 }\n" + //$NON-NLS-1$
	"ul	         { margin-top: 0; margin-bottom: 10 }\n" + //$NON-NLS-1$ 
	"li	         { margin-top: 0; margin-bottom: 0 } \n" + //$NON-NLS-1$
	"li p	     { margin-top: 0; margin-bottom: 0 } \n" + //$NON-NLS-1$
	"ol	         { margin-top: 0; margin-bottom: 10 }\n" + //$NON-NLS-1$
	"dl	         { margin-top: 0; margin-bottom: 10 }\n" + //$NON-NLS-1$
	"dt	         { margin-top: 0; margin-bottom: 0; font-weight: bold }\n" + //$NON-NLS-1$ 
	"dd	         { margin-top: 0; margin-bottom: 0 }\n" + //$NON-NLS-1$
	"\n" + //$NON-NLS-1$
	"/* Styles and colors */\n" + //$NON-NLS-1$
	"a:link	     { color: #0000FF }\n" + //$NON-NLS-1$
	"a:hover	     { color: #000080 }\n" + //$NON-NLS-1$
	"a:visited    { text-decoration: underline }\n" + //$NON-NLS-1$
	"h4           { font-style: italic }\n" + //$NON-NLS-1$ 
	"strong	     { font-weight: bold }\n" + //$NON-NLS-1$
	"em	         { font-style: italic }\n" + //$NON-NLS-1$
	"var	         { font-style: italic }\n" + //$NON-NLS-1$
	"th	         { font-weight: bold }\n" + //$NON-NLS-1$
	""; //$NON-NLS-1$

	private boolean fIsFocusable;
	
	
	public RevisionHoverInformationControlCreator(boolean isFocusable) {
		fIsFocusable= isFocusable;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractReusableInformationControlCreator#canReuse(org.eclipse.jface.text.IInformationControl)
	 */
	public boolean canReuse(IInformationControl control) {
		if (!super.canReuse(control))
			return false;
		
		if (control instanceof IInformationControlExtension4)
			((IInformationControlExtension4)control).setStatusText(EditorsUI.getTooltipAffordanceString());
		
		return true;
	}

	/*
	 * @see org.eclipse.jface.internal.text.revisions.AbstractReusableInformationControlCreator#doCreateInformationControl(org.eclipse.swt.widgets.Shell)
	 */
	protected IInformationControl doCreateInformationControl(Shell parent) {
		int style= fIsFocusable ? SWT.V_SCROLL | SWT.H_SCROLL : SWT.NONE;
		
		if (BrowserInformationControl.isAvailable(parent)) {
            final int shellStyle= SWT.TOOL | (fIsFocusable ? SWT.RESIZE : SWT.NO_TRIM);
            return new BrowserInformationControl(parent, shellStyle, style, EditorsUI.getTooltipAffordanceString()) {

            	/*
            	 * @see org.eclipse.jface.internal.text.html.BrowserInformationControl#setInformation(java.lang.String)
            	 */
            	public void setInformation(String content) {
    				content= addCSSToHTMLFragment(content);
            		super.setInformation(content);
            	}
            	
        		/**
        		 * Adds a HTML header and CSS info if <code>html</code> is only an HTML fragment (has no
        		 * &lt;html&gt; section).
        		 * 
        		 * @param html the html / text produced by a revision
        		 * @return modified html
        		 */
        		private String addCSSToHTMLFragment(String html) {
        			int max= Math.min(100, html.length());
        			if (html.substring(0, max).indexOf("<html>") != -1) //$NON-NLS-1$
        				// there is already a header
        				return html;
        			
        			StringBuffer info= new StringBuffer(512 + html.length());
        			HTMLPrinter.insertPageProlog(info, 0, fgStyleSheet);
        			info.append(html);
        			HTMLPrinter.addPageEpilog(info);
        			return info.toString();
        		}
            };
        }
		return new DefaultInformationControl(parent, style, new HTMLTextPresenter(), EditorsUI.getTooltipAffordanceString());
	}
}