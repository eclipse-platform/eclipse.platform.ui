/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

/**
 * Main class for the Property Sheet View.
 * <p>
 * This standard view has id <code>"org.eclipse.ui.views.PropertySheet"</code>.
 * </p>
 * <p>
 * Note that property <it>sheets</it> and property sheet pages are not the
 * same thing as property <it>dialogs</it> and their property pages (the property
 * pages extension point is for contributing property pages to property dialogs).
 * Within the property sheet view, all pages are <code>IPropertySheetPage</code>s.
 * </p>
 * <p>
 * Property sheet pages are discovered by the property sheet view automatically
 * when a part is first activated. The property sheet view asks the active part
 * for its property sheet page; this is done by invoking 
 * <code>getAdapter(IPropertySheetPage.class)</code> on the part. If the part 
 * returns a page, the property sheet view then creates the controls for that
 * property sheet page (using <code>createControl</code>), and adds the page to 
 * the property sheet view. Whenever this part becomes active, its corresponding
 * property sheet page is shown in the property sheet view (which may or may not
 * be visible at the time). A part's property sheet page is discarded when the
 * part closes. The property sheet view has a default page (an instance of 
 * <code>PropertySheetPage</code>) which services all parts without a property
 * sheet page of their own.
 * </p>
 * <p>
 * The workbench will automatically instantiates this class when a Property
 * Sheet view is needed for a workbench window. This class is not intended
 * to be instantiated or subclassed by clients.
 * </p>
 *
 * @see IPropertySheetPage
 * @see PropertySheetPage
 */
public class PropertySheet extends PageBookView implements ISelectionListener {
    /**
     * No longer used but preserved to avoid api change
     */
    public static final String HELP_CONTEXT_PROPERTY_SHEET_VIEW = IPropertiesHelpContextIds.PROPERTY_SHEET_VIEW;

    /**
     * The initial selection when the property sheet opens
     */
    private ISelection bootstrapSelection;

    /**
     * Creates a property sheet view.
     */
    public PropertySheet() {
        super();
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     * Returns the default property sheet page.
     */
    protected IPage createDefaultPage(PageBook book) {
        PropertySheetPage page = new PropertySheetPage();
        initPage(page);
        page.createControl(book);
        return page;
    }

    /**
     * The <code>PropertySheet</code> implementation of this <code>IWorkbenchPart</code>
     * method creates a <code>PageBook</code> control with its default page showing.
     */
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        getSite().getPage().getWorkbenchWindow().getWorkbench().getHelpSystem()
				.setHelp(getPageBook(),
						IPropertiesHelpContextIds.PROPERTY_SHEET_VIEW);
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchPart.
     */
    public void dispose() {
        // run super.
        super.dispose();

        // remove ourselves as a selection listener
        getSite().getPage().removeSelectionListener(this);
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     */
    protected PageRec doCreatePage(IWorkbenchPart part) {
        // Try to get a custom property sheet page.
        IPropertySheetPage page = (IPropertySheetPage) part
                .getAdapter(IPropertySheetPage.class);
        if (page == null) {
        	// Look for a declaratively-contributed adapter.
        	// See bug 86362 [PropertiesView] Can not access AdapterFactory, when plugin is not loaded.
			page = (IPropertySheetPage) Platform.getAdapterManager()
					.loadAdapter(part, IPropertySheetPage.class.getName());
		}
        if (page != null) {
            if (page instanceof IPageBookViewPage)
                initPage((IPageBookViewPage) page);
            page.createControl(getPageBook());
            return new PageRec(part, page);
        }

        // Use the default page		
        return null;
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     */
    protected void doDestroyPage(IWorkbenchPart part, PageRec rec) {
        IPropertySheetPage page = (IPropertySheetPage) rec.page;
        page.dispose();
        rec.dispose();
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     * Returns the active part on the same workbench page as this property 
     * sheet view.
     */
    protected IWorkbenchPart getBootstrapPart() {
        IWorkbenchPage page = getSite().getPage();
        if (page != null) {
            bootstrapSelection = page.getSelection();
            return page.getActivePart();
        } 
        return null;
    }

    /* (non-Javadoc)
     * Method declared on IViewPart.
     */
    public void init(IViewSite site) throws PartInitException {
        site.getPage().addSelectionListener(this);
        super.init(site);
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     * The property sheet may show properties for any view other than this view.
     */
    protected boolean isImportant(IWorkbenchPart part) {
        return part != this;
    }

    /**
     * The <code>PropertySheet</code> implementation of this <code>IPartListener</code>
     * method first sees if the active part is an <code>IContributedContentsView</code>
     * adapter and if so, asks it for its contributing part.
     */
    public void partActivated(IWorkbenchPart part) {
        IContributedContentsView view = (IContributedContentsView) part
                .getAdapter(IContributedContentsView.class);
        IWorkbenchPart source = null;
        if (view != null)
            source = view.getContributingPart();
        if (source != null)
            super.partActivated(source);
        else
            super.partActivated(part);

        // When the view is first opened, pass the selection to the page		
        if (bootstrapSelection != null) {
            IPropertySheetPage page = (IPropertySheetPage) getCurrentPage();
            if (page != null)
                page.selectionChanged(part, bootstrapSelection);
            bootstrapSelection = null;
        }
    }

    /* (non-Javadoc)
     * Method declared on ISelectionListener.
     * Notify the current page that the selection has changed.
     */
    public void selectionChanged(IWorkbenchPart part, ISelection sel) {
        // we ignore our own selection or null selection
        if (part == this || sel == null)
            return;

        // pass the selection to the page		
        IPropertySheetPage page = (IPropertySheetPage) getCurrentPage();
        if (page != null)
            page.selectionChanged(part, sel);
    }
    
    /**
	 * The <code>PropertySheet</code> implementation of this
	 * <code>PageBookView</code> method handles the <code>ISaveablePart</code>
	 * adapter case by calling <code>getSaveablePart()</code>.
	 * 
	 * @since 3.2
	 */
	protected Object getViewAdapter(Class key) {
		if (ISaveablePart.class.equals(key)) {
			return getSaveablePart();
		}
		return super.getViewAdapter(key);
	}

	/**
	 * Returns an <code>ISaveablePart</code> that delegates to the source part
	 * for the current page if it implements <code>ISaveablePart</code>, or
	 * <code>null</code> otherwise.
	 * 
	 * @return an <code>ISaveablePart</code> or <code>null</code>
	 * @since 3.2
	 */
	protected ISaveablePart getSaveablePart() {
		IWorkbenchPart part = getCurrentContributingPart();
		if (part instanceof ISaveablePart) {
			return (ISaveablePart) part;
		}
		return null;
	}
}
