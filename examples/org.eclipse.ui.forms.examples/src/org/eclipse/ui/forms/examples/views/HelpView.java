/*
 * Created on Dec 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.forms.examples.views;

import java.util.*;
import java.util.Hashtable;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class HelpView extends ViewPart {
	private FormToolkit toolkit;
	private Composite container;
	private IMemento memento;
	private Hashtable pageRecs;
	
	private class PageRec {
		IHelpViewPage page;
		SubActionBars bars;
		
		public PageRec(IHelpViewPage page) {
			this.page = page;
			this.bars = new SubActionBars(getViewSite().getActionBars());
		}
		public void dispose() {
			page.dispose();
			bars.dispose();
		}
	}
	/**
	 * 
	 */
	public HelpView() {
		pageRecs = new Hashtable();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		container = toolkit.createComposite(parent);
		container.setLayout(new StackLayout());
		IHelpViewPage page;
		//if (memento==null)
			//page = createPage(ContextHelpPage.ID);
			page = createPage(AllTopicsPage.ID);
		//else
			//page = createPage(memento.getString("page"));
		showPage(page);
	}
	
	private IHelpViewPage createPage(String id) {
		IHelpViewPage page=null;
		if (id!=null) {
			if (id.equals(ContextHelpPage.ID))
				page = new ContextHelpPage();
			else if (id.equals(AllTopicsPage.ID))
				page = new AllTopicsPage();
		}
		if (page==null)
			return null;
		page.init(this, memento);
		page.createControl(container, toolkit);
		Control c = page.getControl();
		PageRec rec = new PageRec(page);
		c.setData(rec);
		pageRecs.put(id, rec);
		return page;
	}

	public void dispose() {
		for (Enumeration enm = pageRecs.elements(); enm.hasMoreElements();) {
			PageRec rec = (PageRec)enm.nextElement();
			rec.dispose();
		}
		pageRecs.clear();

		if (toolkit!=null) {
			toolkit.dispose();
			toolkit = null;
		}
		super.dispose();
	}
	
	public void showPage(String pageId) {
		Control [] children = container.getChildren();
		for (int i=0; i<children.length; i++) {
			Control child = children[i];
			PageRec rec = (PageRec)child.getData();
			if (rec.page.getId().equals(pageId)) {
				showPage(rec.page);
				return;
			}
		}
	}

	public void showPage(IHelpViewPage page) {
		Control control = page.getControl();
		StackLayout layout = (StackLayout)container.getLayout();
		Control prevControl = layout.topControl;
		if (prevControl!=null) {
			PageRec prevRec = (PageRec)prevControl.getData();
			prevRec.bars.deactivate();
		}
		layout.topControl = control;
		container.layout();
		PageRec rec = (PageRec)control.getData();
		rec.bars.activate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		IHelpViewPage page = getCurrentPage();
		if (page!=null)
			page.setFocus();
	}
    public void init(IViewSite site, IMemento memento) throws PartInitException {
    	this.memento = memento;
       	init(site);
    }
 
    public IHelpViewPage getCurrentPage() {
    	Control c = ((StackLayout)container.getLayout()).topControl;
    	if (c==null) return null;
    	PageRec rec = (PageRec)c.getData();
    	return rec.page;
    }

    /* (non-Javadoc)
     * Method declared on IViewPart.
     */
    public void saveState(IMemento memento) {
    	IHelpViewPage page = getCurrentPage();
    	if (page==null) return;
    	memento.putString("page", page.getId());
    	page.saveState(memento);
    }
}