/*
 * Created on Dec 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.forms.examples.wizards;

import java.util.*;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.examples.views.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.forms.widgets.WrappedPageBook;
import org.eclipse.ui.part.ViewPart;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ContentSectionPart  {
	private Section section;
	private ReusableHelpPart parentPart;
	private FormText linkText;
	private WrappedPageBook pageBook;
	private IMemento memento;
	private Hashtable pageRecs;
	private IActionBars actionBars;
	private IHelpContentPage currentPage;
	
	private class PageRec {
		String sectionText;
		String linkText;
		IHelpContentPage page;
		SubActionBars bars;

		public PageRec(String sectionText, String linkText) {
			this.sectionText = sectionText;
			this.linkText = linkText;
		}

		public void hook(IHelpContentPage page) {
			this.page = page;
			this.bars = new SubActionBars(actionBars);
		}
		public void dispose() {
			if (page!=null) 
				page.dispose();
			if (bars!=null)
				bars.dispose();
			page = null;
			bars = null;
		}
	}
	/**
	 * 
	 */
	public ContentSectionPart() {
		pageRecs = new Hashtable();
		pageRecs.put(ContextHelpPage.ID, new PageRec("About", "Context help"));
		pageRecs.put(AllTopicsPage.ID, new PageRec("All Topics", "All Topics"));
		pageRecs.put(SearchPage.ID, new PageRec("Other Topics", "Search"));
	}
	
	public void init(ReusableHelpPart parentPart, IActionBars bars) {
		this.parentPart = parentPart;
		this.actionBars = bars;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		section = toolkit.createSection(parent,
				Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		Composite client = toolkit.createComposite(section);
		section.setClient(client);
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 0;
		layout.bottomMargin = 0;
		layout.leftMargin = 0;
		layout.rightMargin = 0;
		client.setLayout(layout);
		
		linkText = toolkit.createFormText(client, true);
		linkText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		linkText.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				showPage((String)e.getHref());
			}
		});
		pageBook = new WrappedPageBook(client, SWT.WRAP);
		pageBook.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		showPage(ContextHelpPage.ID);
	}
	public Section getSection() {
		return section;
	}
	
	private IHelpContentPage createPage(String id) {
		IHelpContentPage page=null;
		if (id!=null) {
			if (id.equals(ContextHelpPage.ID))
				page = new ContextHelpPage();
			else if (id.equals(AllTopicsPage.ID))
				page = new AllTopicsPage();
			else if (id.equals(SearchPage.ID))
				page = new SearchPage();
		}
		if (page==null)
			return null;
		page.init(this, memento);
		page.createControl(pageBook, parentPart.getToolkit());
		Control c = page.getControl();
		PageRec rec = (PageRec)pageRecs.get(id);
		rec.hook(page);
		c.setData(rec);
		page.addToActionBars(rec.bars);		
		return page;
	}

	public void dispose() {
		for (Enumeration enm = pageRecs.elements(); enm.hasMoreElements();) {
			PageRec rec = (PageRec)enm.nextElement();
			rec.dispose();
		}
		pageRecs.clear();
	}
	
	public void showPage(String pageId) {
		PageRec rec = (PageRec)pageRecs.get(pageId);
		if (rec.page==null) {
			createPage(pageId);
		}
		showPage(rec.page);
	}

	public void showPage(IHelpContentPage page) {
		Control control = page.getControl();

		if (currentPage!=null) {
			PageRec prevRec = (PageRec)currentPage.getControl().getData();
			prevRec.bars.deactivate();
		}
		currentPage = page;
		pageBook.showPage(control);
		PageRec rec = (PageRec)control.getData();
		rec.bars.activate();
		section.setText(rec.sectionText);
		updateLinks(page.getId());
		reflow();
	}
	
	private void updateLinks(String currentId) {
		StringBuffer buf = new StringBuffer();
		buf.append("<form>");
		appendLink(buf, ContextHelpPage.ID);
		appendLink(buf, AllTopicsPage.ID);
		appendLink(buf, SearchPage.ID);
		buf.append("</form>");
		linkText.setText(buf.toString(), true, false);
	}

	private void appendLink(StringBuffer buff, String id) {
		IHelpContentPage page = getCurrentPage();
		if (page!=null && page.getId().equals(id))
			return;
		PageRec rec = (PageRec)pageRecs.get(id);
		buff.append("<li>");
		buff.append("<a href=\"");
		buff.append(id);
		buff.append("\">");
		buff.append(rec.linkText);
		buff.append("</a>");
		buff.append("</li>");
	}
	
	public void reflow() {
		parentPart.reflow();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		IHelpContentPage page = getCurrentPage();
		if (page!=null)
			page.setFocus();
	}
 
    public IHelpContentPage getCurrentPage() {
    	return currentPage;
    }
    
    public boolean isMonitoringContextHelp() {
    	IHelpContentPage page = getCurrentPage();
    	return (page!=null && page.getId().equals(ContextHelpPage.ID));
    }

    /* (non-Javadoc)
     * Method declared on IViewPart.
     */
    public void saveState(IMemento memento) {
    	IHelpContentPage page = getCurrentPage();
    	if (page==null) return;
    	memento.putString("page", page.getId());
    	page.saveState(memento);
    }
    public void openLink(Object href) {
    	parentPart.openLink(href);
    }
    public void handleActivation(Control c) {
    	IHelpContentPage page = getCurrentPage();
    	if (page!=null && page instanceof ContextHelpPage) {
    		ContextHelpPage cpage = (ContextHelpPage)page;
    		cpage.handleActivation(c);
    	}
    		
    }
}