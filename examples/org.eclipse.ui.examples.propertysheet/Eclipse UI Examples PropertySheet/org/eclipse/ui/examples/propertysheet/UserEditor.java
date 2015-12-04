/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.propertysheet;

import org.eclipse.jface.text.Document;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

/**
 * This class implements the User editor.
 */
public class UserEditor extends TextEditor {
    private ContentOutlinePage userContentOutline;

    /**
     * UserEditor default Constructor
     */
    public UserEditor() {
        super();
    }

    @Override
	public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        getSourceViewer().setDocument(
                new Document(MessageUtil.getString("Editor_instructions"))); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    @Override
	public <T> T getAdapter(Class<T> adapter) {
        if (adapter.equals(IContentOutlinePage.class)) {
            return (T) getContentOutline();
        }
        if (adapter.equals(IPropertySheetPage.class)) {
            return (T) getPropertySheet();
        }
        return super.getAdapter(adapter);
    }

    /**
     * Returns the content outline.
     */
    protected ContentOutlinePage getContentOutline() {
        if (userContentOutline == null) {
            //Create a property outline page using the parsed result of passing in the document provider.
            userContentOutline = new PropertySheetContentOutlinePage(
                    new UserFileParser().parse(getDocumentProvider()));
        }
        return userContentOutline;
    }

    /**
     * Returns the property sheet.
     */
    protected IPropertySheetPage getPropertySheet() {
        return new PropertySheetPage();
    }
}
