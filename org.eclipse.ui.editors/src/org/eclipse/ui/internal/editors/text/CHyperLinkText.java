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
package org.eclipse.ui.internal.editors.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.util.SafeRunnable;

import org.eclipse.jface.text.Assert;


/**
 * TODO remove when platform widget becomes available
 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=79419
 * @since 3.1
 */
public class CHyperLinkText extends Composite {
	private static final char ESCAPE= '\\';
	private static final String OPENING= "{"; //$NON-NLS-1$
	private static final String CLOSING= "}"; //$NON-NLS-1$
	private static final String SEPARATOR= ","; //$NON-NLS-1$
	
	public interface ILinkListener {
		void linkSelected(String url);
	}
	
	private static final class LinkSpec {
		public String url;
		public String text;
		public String tooltip;
	}
	
	private final List fListeners= new ArrayList();
	
	public CHyperLinkText(Composite parent, int style) {
		super(parent, style);

		RowLayout rowLayout= new RowLayout(SWT.HORIZONTAL);
		rowLayout.justify= false;
		rowLayout.fill= true;
		rowLayout.marginBottom= 0;
		rowLayout.marginHeight= 0;
		rowLayout.marginLeft= 0;
		rowLayout.marginRight= 0;
		rowLayout.marginTop= 0;
		rowLayout.marginWidth= 0;
		rowLayout.spacing= 0;
		setLayout(rowLayout);
	}
	
	public void setText(String text) {
		List tokens= new ArrayList();
		int last= 0, next;
		while ((next= indexOfUnescaped(text, OPENING, last)) != -1) {
			int closing= indexOfUnescaped(text, CLOSING, next);
			int comma1= indexOfUnescaped(text, SEPARATOR, next);
			int comma2= indexOfUnescaped(text, SEPARATOR, comma1 + 1);
			
			if (closing != -1) {
				if (next > last) {
					tokens.add(text.substring(last, next));
				}
				
				LinkSpec link= new LinkSpec();
				if (comma1 != -1 && comma1 < closing) {
					link.url= text.substring(next + 1, comma1);
					if (comma2 != -1 && comma2 < closing) {
						link.text= text.substring(comma1 + 1, comma2);
						link.tooltip= text.substring(comma2 + 1, closing);
					} else {
						link.text= text.substring(comma1 + 1, closing);
					}
				} else {
					link.url= text.substring(next + 1, closing);
				}
				
				tokens.add(link);
				
				last= closing + 1;
			}
		}
		
		if (last < text.length())
			tokens.add(text.substring(last));
		
		setText(tokens);
	}
	
	private int indexOfUnescaped(String target, String str, int index) {
		if (index == -1)
			return -1;
		while (true) {
			index= target.indexOf(str, index);
			if (index <= 0 || target.charAt(index - 1) != ESCAPE)
				return index;
		}
	}
	
	private void setText(List tokens) {
		for (Iterator it= tokens.iterator(); it.hasNext();) {
			Object token= it.next();

			if (token instanceof LinkSpec) {
				final LinkSpec spec= (LinkSpec) token;
				Assert.isNotNull(spec.url);
				CHyperLink link= new CHyperLink(this, SWT.NONE);
				link.setText(spec.text == null ? spec.url : spec.text);
				link.setToolTipText(spec.tooltip);
				link.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						fireLinkSelected(spec.url);
					}
				});
				continue;
			}
			
			String text= (String) token;
			Label last= null;
			StringTokenizer tokenizer= new StringTokenizer(text, " ", true); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				String next= tokenizer.nextToken();
				if (next.trim().length() == 0 && last != null) {
					last.setText(last.getText() + next);
					continue;
				}
				Label label= new Label(this, SWT.NONE);
				label.setText(next);
				last= label;
			}
		}
		
	}
	
	public void addLinkListener(ILinkListener listener) {
		if (isDisposed())
			SWT.error(SWT.ERROR_WIDGET_DISPOSED);
		if (listener == null)
			throw new NullPointerException();
		
		if (!fListeners.contains(listener))
			fListeners.add(listener);
	}
	
	public void removeLinkListener(ILinkListener listener) {
		if (isDisposed())
			SWT.error(SWT.ERROR_WIDGET_DISPOSED);
		fListeners.remove(listener);
	}

	private void fireLinkSelected(final String url) {
		for (Iterator it= fListeners.iterator(); it.hasNext();) {
			final ILinkListener listener= (ILinkListener) it.next();
			
			SafeRunnable runnable= new SafeRunnable() {
				public void run() throws Exception {
					listener.linkSelected(url);
				}
			};
			Platform.run(runnable);
		}
	}
	
	/*
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose() {
		fListeners.clear();
		super.dispose();
	}
}
