/*******************************************************************************
 * Copyright (c) 2007, 2011 Dakshinamurthy Karra, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dakshinamurthy Karra (Jalian Systems) - Templates View - https://bugs.eclipse.org/bugs/show_bug.cgi?id=69581
 *     Piotr Maj <pm@jcake.com> - no access to template store and current selection - https://bugs.eclipse.org/bugs/show_bug.cgi?id=296439
 *******************************************************************************/
package org.eclipse.ui.texteditor.templates;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;


/**
 * The Templates view.hosts {@link ITemplatesPage}s that shows the templates for the currently
 * active editor part.
 * <p>
 * When this view notices an editor being activated, it uses the Eclipse adapter mechanism to get
 * its {@link ITemplatesPage}. Hence, editors that want to provide a templates page need to provide
 * such an adapter:
 *
 * <pre>
 * Object getAdapter() {
 * 	...
 *	if (ITemplatesPage.class.equals(required)) {
 *		if (fTemplatesPage == null)
 *			fTemplatesPage= new JavaTemplatesPage(this);
 *			return fTemplatesPage;
 *		}
 *	}
 *	...
 * }
 * </pre></p>
 * <p>
 * <strong>Note:</strong> This plug-in does not contribute this view. Clients that want to
 * use this view must check whether it is available and if not, contribute this view via
 * extension point using the specified view Id {@link #ID}:
 * <pre>
 * &lt;extension
 *       point="org.eclipse.ui.views"&gt;
 *    &lt;view
 *          name="%templatesViewName"
 *          icon="$nl$/icons/full/eview16/templates.gif"
 *          category="org.eclipse.ui"
 *          class="org.eclipse.ui.texteditor.templates.TemplatesView"
 *          id="org.eclipse.ui.texteditor.TemplatesView"&gt;
 *    &lt;/view&gt;
 * &lt/extension>
 * </pre>
 * The <code>templates.gif</code> icon can be copied from this plug-in.
 * </p>
 * If the editor supports a templates page, the editor instantiates and configures the page, and
 * returns it. This page is then added to this Templates view and immediately made the current page
 * (the Templates view needs not to be visible). If the
 * editor does not support a templates page, the Templates view shows a special default page which
 * makes it clear to the user that no templates are available. When the Templates view notices
 * a different editor being activated, it flips to the editor's corresponding templates page. When
 * the templates view notices an editor being closed, it may destroy the editor's corresponding
 * templates page.
 * </p>
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.
 * </p>
 *
 * @since 3.4
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class TemplatesView extends PageBookView {

	/**
	 * The  id for this view.
	 * <p>
	 * <strong>Note:</strong> Only this id is allowed when contributing
	 * this view via extension point.</p>
	 */
	public static final String ID= "org.eclipse.ui.texteditor.TemplatesView"; //$NON-NLS-1$


	/**
	 * Creates a templates view.
	 */
	public TemplatesView() {
	}

	/*
	 * @see org.eclipse.ui.part.PageBookView#createDefaultPage(org.eclipse.ui.part.PageBook)
	 */
	protected IPage createDefaultPage(PageBook book) {
		MessagePage page= new MessagePage();
		initPage(page);
		page.createControl(book);
		page.setMessage(TemplatesMessages.TemplatesView_no_templates);
		return page;
	}

	/*
	 * @see org.eclipse.ui.part.PageBookView#createPartControl(org.eclipse.swt.widgets.Composite)
	 * @since 3.4
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		Assert.isTrue(ID.equals(getViewSite().getId())); // prevent from contributing this view under a different ID
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getPageBook(), IAbstractTextEditorHelpContextIds.TEMPLATES_VIEW);
	}

	/*
	 * @see org.eclipse.ui.part.PageBookView#doCreatePage(org.eclipse.ui.IWorkbenchPart)
	 */
	protected PageRec doCreatePage(IWorkbenchPart part) {
		// Try to get template page.
		ITemplatesPage page= (ITemplatesPage)part.getAdapter(ITemplatesPage.class);
		if (page == null)
			page= (ITemplatesPage)Platform.getAdapterManager().getAdapter(part, ITemplatesPage.class);
		if (page == null)
			return null; // There is no template page

		initPage(page);
		page.createControl(getPageBook());
		return new PageRec(part, page);
	}

	/*
	 * @see org.eclipse.ui.part.PageBookView#doDestroyPage(org.eclipse.ui.IWorkbenchPart, org.eclipse.ui.part.PageBookView.PageRec)
	 */
	protected void doDestroyPage(IWorkbenchPart part, PageRec rec) {
		ITemplatesPage page= (ITemplatesPage)rec.page;
		page.dispose();
		rec.dispose();
	}

	/*
	 * @see org.eclipse.ui.part.PageBookView#getBootstrapPart()
	 * @since 3.4
	 */
	protected IWorkbenchPart getBootstrapPart() {
		IWorkbenchPage page= getSite().getPage();
		if (page != null)
			return page.getActiveEditor();
		return null;
	}

	/*
	 * @see org.eclipse.ui.part.PageBookView#isImportant(org.eclipse.ui.IWorkbenchPart)
	 */
	protected boolean isImportant(IWorkbenchPart part) {
		//We only care about editors
		return (part instanceof IEditorPart);
	}

	/*
	 * @see org.eclipse.ui.part.PageBookView#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 * @since 3.4
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
		partActivated(part);
	}

	/**
	 * Returns the template store of the current page.
	 * 
	 * @return the template store, or <code>null</code> if the current page does not provide that
	 *         information
	 * @since 3.6
	 */
	public TemplateStore getTemplateStore() {
		IPage currentPage= getCurrentPage();
		if (currentPage instanceof ITemplatesPageExtension)
			return ((ITemplatesPageExtension)currentPage).getTemplateStore();
		return null;
	}

	/**
	 * Returns the currently selected templates.
	 * 
	 * @return array of selected templates, or <code>null</code> if the current page does not
	 *         provide that information
	 * @since 3.6
	 */
	public TemplatePersistenceData[] getSelectedTemplates() {
		IPage currentPage= getCurrentPage();
		if (currentPage instanceof ITemplatesPageExtension)
			return ((ITemplatesPageExtension)currentPage).getSelectedTemplates();
		return null;
	}

}