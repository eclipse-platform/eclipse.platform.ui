/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.logic;

import org.eclipse.gef.examples.logicdesigner.LogicEditor;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class TabbedPropertiesLogicEditor
    extends LogicEditor
    implements ITabbedPropertySheetPageContributor {

    private TabbedPropertySheetPage tabbedPropertySheetPage;

    protected void initializeGraphicalViewer() {
        super.initializeGraphicalViewer();
        this.tabbedPropertySheetPage = new TabbedPropertySheetPage(this);
    }

    public String getContributorId() {
        return getSite().getId();
    }

    public Object getAdapter(Class type) {
        if (type == IPropertySheetPage.class)
            return tabbedPropertySheetPage;
        return super.getAdapter(type);
    }
}
