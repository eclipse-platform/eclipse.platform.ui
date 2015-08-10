/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.performance.parts;

import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @since 3.1
 */
public class PerformanceEditorPart extends EditorPart {

    private static final String PARAM_OUTLINE = "outline";

    private boolean dirty;
    private Label control;
    private boolean useOutline = false;

    /**
     *
     */
    public PerformanceEditorPart() {
        super();
    }

    @Override
	public void doSave(IProgressMonitor monitor) {
    }

    @Override
	public void doSaveAs() {
    }

    @Override
	public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        super.setSite(site);
        super.setInput(input);
    }

    @Override
	public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
	public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
	public void createPartControl(Composite parent) {
        control = new Label(parent, SWT.NONE);

    }

    @Override
	public void setFocus() {
        control.setFocus();
    }

    @Override
	public void setInitializationData(IConfigurationElement cfig,
            String propertyName, Object data) {
        super.setInitializationData(cfig, propertyName, data);

        if (data instanceof String) {
           for (StringTokenizer toker = new StringTokenizer((String) data, ","); toker.hasMoreTokens(); ) {
               String token = toker.nextToken();
               if (token.equals(PARAM_OUTLINE))
                   useOutline = true;
           }

        }
    }
    @Override
	public Object getAdapter(Class adapter) {
        Object object = null;
        if (useOutline && adapter.equals(IContentOutlinePage.class)) {
            object = new ContentOutlinePage() {
            };
        }
        if (object != null)
            return object;
        return super.getAdapter(adapter);
    }
}
