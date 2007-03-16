/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;
import java.util.Hashtable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.forms.widgets.WrappedPageBook;
/**
 * ScrolledPageBook is a class that is capable of stacking several composites
 * (pages), while showing one at a time. The content is scrolled if there is
 * not enough space to fit it in the client area.
 * 
 * @since 3.0
 */
public class ScrolledPageBook extends SharedScrolledComposite {
	private WrappedPageBook pageBook;
	private Hashtable pages;
	private Composite emptyPage;
	private Control currentPage;
	/**
	 * Creates a new instance in the provided parent
	 * 
	 * @param parent
	 */
	public ScrolledPageBook(Composite parent) {
		this(parent, SWT.H_SCROLL | SWT.V_SCROLL);
	}
	/**
	 * Creates a new instance in the provided parent and with the provided
	 * style.
	 * 
	 * @param parent
	 *            the control parent
	 * @param style
	 *            the style to use
	 */
	public ScrolledPageBook(Composite parent, int style) {
		super(parent, style);
		pageBook = new WrappedPageBook(this, SWT.NULL);
		setContent(pageBook);
		pages = new Hashtable();
		setExpandHorizontal(true);
		setExpandVertical(true);
		this.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event e) {
				switch (e.detail) {
					case SWT.TRAVERSE_ESCAPE :
					case SWT.TRAVERSE_RETURN :
					case SWT.TRAVERSE_TAB_NEXT :
					case SWT.TRAVERSE_TAB_PREVIOUS :
						e.doit = true;
						break;
				}
			}
		});
	}
	/**
	 * Removes the default size of the composite, allowing the control to
	 * shrink to the trim.
	 * 
	 * @param wHint
	 *            the width hint
	 * @param hHint
	 *            the height hint
	 * @param changed
	 *            if <code>true</code>, do not use cached values
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Rectangle trim = computeTrim(0, 0, 10, 10);
		return new Point(trim.width, trim.height);
	}
	/**
	 * Tests if the page under the provided key is currently in the book.
	 * 
	 * @param key
	 *            the page key
	 * @return <code>true</code> if page exists, <code>false</code>
	 *         otherwise.
	 */
	public boolean hasPage(Object key) {
		return pages.containsKey(key);
	}
	/**
	 * Creates a new page for the provided key. Use the returned composite to
	 * create children in it.
	 * 
	 * @param key
	 *            the page key
	 * @return the newly created page composite
	 */
	public Composite createPage(Object key) {
		Composite page = createPage();
		pages.put(key, page);
		return page;
	}
	/**
	 * Returns the page book container.
	 * 
	 * @return the page book container
	 */
	public Composite getContainer() {
		return pageBook;
	}
	/**
	 * Registers a page under the privided key to be managed by the page book.
	 * The page must be a direct child of the page book container.
	 * 
	 * @param key
	 *            the page key
	 * @param page
	 *            the page composite to register
	 * @see #createPage(Object)
	 * @see #getContainer
	 */
	public void registerPage(Object key, Control page) {
		pages.put(key, page);
	}
	/**
	 * Removes the page under the provided key from the page book. Does nothing
	 * if page with that key does not exist.
	 * 
	 * @param key
	 *            the page key.
	 */
	public void removePage(Object key) {
		removePage(key, true);
	}
	/**
	 * Removes the page under the provided key from the page book. Does nothing
	 * if page with that key does not exist.
	 * 
	 * @param key
	 *            the page key.
	 * @param showEmptyPage
	 * 			  if <code>true</code>, shows the empty page
	 *            after page removal.
	 */
	public void removePage(Object key, boolean showEmptyPage) {
		Control page = (Control) pages.get(key);
		if (page != null) {
			pages.remove(key);
			page.dispose();
			if (showEmptyPage)
				showEmptyPage();
		}
	}
	/**
	 * Shows the page with the provided key and hides the page previously
	 * showing. Does nothing if the page with that key does not exist.
	 * 
	 * @param key
	 *            the page key
	 */
	public void showPage(Object key) {
		Control page = (Control) pages.get(key);
		if (page != null) {
			pageBook.showPage(page);
			if (currentPage != null && currentPage != page) {
				// switching pages - force layout
				if (page instanceof Composite)
					((Composite) page).layout(false);
			}
			currentPage = page;
		} else {
			showEmptyPage();
		}
		reflow(true);
	}
	/**
	 * Shows a page with no children to be used if the desire is to not show
	 * any registered page.
	 */
	public void showEmptyPage() {
		if (emptyPage == null) {
			emptyPage = createPage();
			emptyPage.setLayout(new GridLayout());
		}
		pageBook.showPage(emptyPage);
		currentPage = emptyPage;
		reflow(true);
	}
	/**
	 * Sets focus on the current page if shown.
	 */
	public boolean setFocus() {
		if (currentPage != null)
			return currentPage.setFocus();
		return super.setFocus();
	}
	/**
	 * Returns the page currently showing.
	 * 
	 * @return the current page
	 */
	public Control getCurrentPage() {
		return currentPage;
	}
	private Composite createPage() {
		Composite page = new LayoutComposite(pageBook, SWT.NULL);
		page.setBackground(getBackground());
		page.setForeground(getForeground());
		page.setMenu(pageBook.getMenu());
		return page;
	}
}
