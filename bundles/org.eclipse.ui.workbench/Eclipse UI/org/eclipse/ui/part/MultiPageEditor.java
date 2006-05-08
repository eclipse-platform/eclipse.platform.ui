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
package org.eclipse.ui.part;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;

/**
 * Abstract superclass of all multi-page workbench editors.
 * <p>
 * This class should be subclassed by clients wishing to define new 
 * multi-page editor.
 * </p>
 * <p>
 * Subclasses must implement the following methods:
 * <ul>
 *   <li><code>createPartControl</code> - to create the view's controls </li>
 *   <li><code>setFocus</code> - to accept focus</li>
 *   <li><code>isDirty</code> - to decide whether a significant change has
 *       occurred</li>
 *   <li><code>doSave</code> - to save contents of editor</li>
 *   <li><code>doSaveAs</code> - to save contents of editor</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend or reimplement the following methods as required:
 * <ul>
 *   <li><code>setInitializationData</code> - extend to provide additional 
 *       initialization when editor extension is instantiated</li>
 *   <li><code>init(IEditorSite,IEditorInput)</code> - extend to provide 
 *       additional initialization when editor is assigned its site</li>
 *   <li><code>isSaveOnCloseNeeded</code> - override to control saving</li>
 *   <li><code>isSaveAsAllowed</code> - override to control saving</li>
 *   <li><code>gotoMarker</code> - reimplement to make selections based on
 *       markers</li>
 *   <li><code>dispose</code> - extend to provide additional cleanup</li>
 *   <li><code>getAdapter</code> - reimplement to make their editor
 *       adaptable</li>
 * </ul>
 * </p>
 * 
 * @deprecated Use the class <code>MultiPageEditorPart</code> instead
 */
public abstract class MultiPageEditor extends EditorPart {
    private List syncVector;

    private TabFolder tabFolder;

    /**
     * Creates a new multi-page editor.
     * 
     * @deprecated Use the class <code>MultiPageEditorPart</code> instead
     */
    public MultiPageEditor() {
        super();
    }

    /**
     * Adds a synchronized pagebook to this editor.  Once added, the
     * visible page of the pagebook and the visible page of the editor 
     * will be synchronized.
     *
     * @param pageBook the pagebook to add
     */
    protected void addSyncroPageBook(PageBook pageBook) {
        // Add the page.
        if (syncVector == null) {
			syncVector = new ArrayList(1);
		}
        syncVector.add(pageBook);

        // Set the visible page.
        syncPageBook(pageBook);
    }

    /**
     * The <code>MultiPageEditor</code> implementation of this <code>IWorkbenchPart</code>
     * method creates a <code>TabFolder</code> control.
     */
    public void createPartControl(Composite parent) {
        tabFolder = new TabFolder(parent, SWT.NONE);
        tabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                sync();
            }
        });
    }

    /**
     * Returns this editor's workbook.
     *
     * @return the editor workbook
     */
    protected TabFolder getFolder() {
        return tabFolder;
    }

    /**
     * Indicates that a page change has occurred.  Updates the sync vector.
     */
    protected void onPageChange() {
        if (syncVector != null) {
            Iterator itr = syncVector.iterator();
            while (itr.hasNext()) {
                PageBook pageBook = (PageBook) itr.next();
                syncPageBook(pageBook);
            }
        }
    }

    /**
     * Removes a synchronized pagebook from this editor. 
     *
     * @param pageBook the pagebook to remove 
     * @see #addSyncroPageBook(PageBook)
     */
    protected void removeSyncroPageBook(PageBook pageBook) {
        if (syncVector != null) {
			syncVector.remove(pageBook);
		}
        pageBook.dispose();
    }

    /**
     * Synchronizes each registered pagebook with the editor page.
     */
    protected void sync() {
        if (syncVector != null) {
            Iterator itr = syncVector.iterator();
            while (itr.hasNext()) {
                PageBook pageBook = (PageBook) itr.next();
                syncPageBook(pageBook);
            }
        }
    }

    /**
     * Sets the visible page of the given pagebook to be the same as
     * the visible page of this editor.
     *
     * @param pageBook a pagebook to synchronize
     */
    protected void syncPageBook(PageBook pageBook) {
        int pos = tabFolder.getSelectionIndex();
        Control children[] = pageBook.getChildren();
        int size = children.length;
        if (pos < size) {
			pageBook.showPage(children[pos]);
		}
    }
}
