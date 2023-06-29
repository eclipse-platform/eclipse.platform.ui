/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 460405
 *******************************************************************************/

package org.eclipse.ui.views.contentoutline;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.contentoutline.ContentOutlineMessages;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

/**
 * Main class for the Content Outline View.
 * <p>
 * This standard view has id <code>"org.eclipse.ui.views.ContentOutline"</code>.
 * </p>
 * When a <b>content outline view</b> notices an editor being activated, it asks
 * the editor whether it has a <b>content outline page</b> to include in the
 * outline view. This is done using <code>getAdapter</code>:
 *
 * <pre>
 * IEditorPart editor = ...;
 * IContentOutlinePage outlinePage = (IContentOutlinePage) editor.getAdapter(IContentOutlinePage.class);
 * if (outlinePage != null) {
 *    // editor wishes to contribute outlinePage to content outline view
 * }
 * </pre>
 * <p>
 * If the editor supports a content outline page, the editor instantiates and
 * configures the page, and returns it. This page is then added to the content
 * outline view (a pagebook which presents one page at a time) and immediately
 * made the current page (the content outline view need not be visible). If the
 * editor does not support a content outline page, the content outline view
 * shows a special default page which makes it clear to the user that the
 * content outline view is disengaged. A content outline page is free to report
 * selection events; the content outline view forwards these events along to
 * interested parties. When the content outline view notices a different editor
 * being activated, it flips to the editor's corresponding content outline page.
 * When the content outline view notices an editor being closed, it destroys the
 * editor's corresponding content outline page.
 * </p>
 * <p>
 * The workbench will automatically instantiate this class when a Content
 * Outline view is needed for a workbench window. This class was not intended to
 * be instantiated or subclassed by clients.
 * </p>
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ContentOutline extends PageBookView implements ISelectionProvider, ISelectionChangedListener {

	/**
	 * The plugin prefix.
	 */
	public static final String PREFIX = PlatformUI.PLUGIN_ID + "."; //$NON-NLS-1$

	/**
	 * Help context id used for the content outline view
	 * (value <code>"org.eclipse.ui.content_outline_context"</code>).
	 */
	public static final String CONTENT_OUTLINE_VIEW_HELP_CONTEXT_ID = PREFIX
			+ "content_outline_context";//$NON-NLS-1$

	private static final String VIEWS_PLUGIN_ID = "org.eclipse.ui.views"; //$NON-NLS-1$

	/**
	 * Message to show on the default page.
	 */
	private String defaultText =ContentOutlineMessages.ContentOutline_noOutline;

	/**
	 * Creates a content outline view with no content outline pages.
	 */
	public ContentOutline() {
		super();
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		getSelectionProvider().addSelectionChangedListener(listener);
	}

	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage page = new MessagePage();
		initPage(page);
		page.createControl(book);
		page.setMessage(defaultText);
		return page;
	}

	/**
	 * The <code>PageBookView</code> implementation of this <code>IWorkbenchPart</code>
	 * method creates a <code>PageBook</code> control with its default page showing.
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getPageBook(),
				CONTENT_OUTLINE_VIEW_HELP_CONTEXT_ID);
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		// Try to get an outline page.
		IContentOutlinePage page = Adapters.adapt(part, IContentOutlinePage.class);
		if (page != null) {
			if (page instanceof IPageBookViewPage) {
				initPage((IPageBookViewPage) page);
			}
			try {
				page.createControl(getPageBook());
			} catch (Exception e) {
				String message = "Failed to create outline control for " + page.getClass(); //$NON-NLS-1$
				ILog.of(Platform.getBundle(VIEWS_PLUGIN_ID))
						.log(new Status(IStatus.ERROR, VIEWS_PLUGIN_ID, IStatus.OK, message, e));
				page.dispose();
				return null;
			}
			return new PageRec(part, page);
		}
		// There is no content outline
		return null;
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec rec) {
		IContentOutlinePage page = (IContentOutlinePage) rec.page;
		page.dispose();
		rec.dispose();
	}

	@Override
	public <T> T getAdapter(Class<T> key) {
		if (key == IContributedContentsView.class) {
			return key.cast((IContributedContentsView) this::getContributingEditor);
		}
		return super.getAdapter(key);
	}

	@Override
	protected IWorkbenchPart getBootstrapPart() {
		IWorkbenchPage page = getSite().getPage();
		if (page != null) {
			return page.getActiveEditor();
		}

		return null;
	}

	/**
	 * Returns the editor which contributed the current
	 * page to this view.
	 *
	 * @return the editor which contributed the current page
	 * or <code>null</code> if no editor contributed the current page
	 */
	private IWorkbenchPart getContributingEditor() {
		return getCurrentContributingPart();
	}

	@Override
	public ISelection getSelection() {
		// get the selection from the selection provider
		return getSelectionProvider().getSelection();
	}

	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		//We only care about editors
		return (part instanceof IEditorPart);
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		partActivated(part);
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		getSelectionProvider().removeSelectionChangedListener(listener);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		getSelectionProvider().selectionChanged(event);
	}

	@Override
	public void setSelection(ISelection selection) {
		getSelectionProvider().setSelection(selection);
	}

	/**
	 * The <code>ContentOutline</code> implementation of this <code>PageBookView</code> method
	 * extends the behavior of its parent to use the current page as a selection provider.
	 *
	 * @param pageRec the page record containing the page to show
	 */
	@Override
	protected void showPageRec(PageRec pageRec) {
		IPageSite pageSite = getPageSite(pageRec.page);
		ISelectionProvider provider = pageSite.getSelectionProvider();
		if (provider == null && (pageRec.page instanceof IContentOutlinePage)) {
			// This means that the page did not set a provider during its initialization
			// so for backward compatibility we will set the page itself as the provider.
			pageSite.setSelectionProvider((IContentOutlinePage) pageRec.page);
		}
		super.showPageRec(pageRec);
	}
}
