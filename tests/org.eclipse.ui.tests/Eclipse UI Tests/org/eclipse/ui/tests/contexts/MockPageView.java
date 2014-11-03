/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.contexts;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

/**
 * A special view that manages pages. It is based on text editors, and adds a
 * ContextPage for files ending in ".xml", but just a MessagePage otherwise.
 *
 * @since 3.2
 */
public class MockPageView extends PageBookView {

	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage page = new MessagePage();
		initPage(page);
		page.createControl(book);
		page.setMessage("There is no relevant part active.");
		return page;
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			IEditorPart editor = (IEditorPart) part;
			if (editor.getTitle().endsWith(".xml")) {
				ContextPage page = new ContextPage();
				initPage(page);
				page.createControl(getPageBook());
				page.setMessage("The XML editor is active: "
						+ editor.getTitle());
				return new PageRec(part, page);
			}
			MessagePage page = new MessagePage();
			initPage(page);
			page.createControl(getPageBook());
			page.setMessage("The editor is active: " + editor.getTitle());
			return new PageRec(part, page);
		}
		return null;
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		pageRecord.page.dispose();
		pageRecord.dispose();
	}

	@Override
	protected IWorkbenchPart getBootstrapPart() {
		return getSite().getPage().getActiveEditor();
	}

	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		return part instanceof IEditorPart;
	}

}
