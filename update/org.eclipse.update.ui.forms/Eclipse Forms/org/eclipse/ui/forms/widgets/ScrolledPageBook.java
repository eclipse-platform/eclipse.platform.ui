/*
 * Created on Jan 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms.widgets;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.PageBook;
/**
 * @author dejan
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ScrolledPageBook extends SharedScrolledComposite {
	private PageBook pageBook;
	private Hashtable pages;
	private Composite emptyPage;
	private Composite currentPage;
/**
 * 
 * @param parent
 * @param style
 */
	public ScrolledPageBook(Composite parent) {
		this(parent, SWT.H_SCROLL | SWT.V_SCROLL);
	}
	public ScrolledPageBook(Composite parent, int style) {
		super(parent, style);
		pageBook = new PageBook(this, SWT.NULL);
		setContent(pageBook);
		pages = new Hashtable();
		setExpandHorizontal(true);
		setExpandVertical(true);
		this.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event e) {
				switch (e.detail) {
					case SWT.TRAVERSE_ESCAPE:
					case SWT.TRAVERSE_RETURN:
					case SWT.TRAVERSE_TAB_NEXT:
					case SWT.TRAVERSE_TAB_PREVIOUS:
					e.doit = true;
					break;
				}
			}
		});
	}
	public Point computeSize (int wHint, int hHint, boolean changed) {
		Rectangle trim = computeTrim (0, 0, 10, 10);
		return new Point (trim.width, trim.height);
	}
/**
 * 
 * @param key
 * @return
 */
	public boolean hasPage(Object key) {
		return pages.containsKey(key);
	}
/**
 * 
 * @param key
 * @return
 */
	public Composite createPage(Object key) {
		Composite page = createPage();
		pages.put(key, page);
		return page;
	}

	public Composite getContainer() {
		return pageBook;
	}
	
	public void registerPage(Object key, Composite page) {
		pages.put(key, page);
	}
/**
 * 
 * @param key
 */
	public void removePage(Object key) {
		Composite page = (Composite) pages.get(key);
		if (page != null) {
			pages.remove(key);
			page.dispose();
			showEmptyPage();
		}
	}
/**
 * 
 * @param key
 */
	public void showPage(Object key) {
		Composite page = (Composite) pages.get(key);
		if (page != null) {
			pageBook.showPage(page);
			if (currentPage!=null && currentPage!=page) {
				// switching pages - force layout
				page.layout(false);
			}
			currentPage = page;
		} else {
			showEmptyPage();
		}
		reflow(true);
	}
/**
 * 
 *
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
	public boolean setFocus() {
		if (currentPage!=null)
			return currentPage.setFocus();
		return super.setFocus();
	}
	public Control getCurrentPage() {
		return currentPage;
	}
	private Composite createPage() {
		Composite page = new LayoutComposite(pageBook, SWT.NULL);
		page.setBackground(getBackground());
		page.setForeground(getForeground());
		return page;
	}
}