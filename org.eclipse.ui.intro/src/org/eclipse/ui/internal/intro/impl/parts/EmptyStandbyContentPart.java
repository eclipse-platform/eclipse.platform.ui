/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.intro.impl.parts;


import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.intro.impl.*;
import org.eclipse.ui.internal.intro.impl.swt.*;
import org.eclipse.ui.intro.*;
import org.eclipse.ui.intro.config.*;



public class EmptyStandbyContentPart implements IStandbyContentPart {

    Composite contentComposite;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.config.IStandbyContentPart#createPartControl(org.eclipse.swt.widgets.Composite,
     *      org.eclipse.ui.forms.widgets.FormToolkit)
     */
    public void createPartControl(Composite parent, FormToolkit toolkit) {
        contentComposite = toolkit.createComposite(parent);
        contentComposite.setLayout(new GridLayout());
        String text = IntroPlugin.getString("EmptyStandbyContentPart.text"); //$NON-NLS-1$
        Label label = toolkit.createLabel(contentComposite, text, SWT.NULL);
        label.setFont(PageStyleManager.getBannerFont());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        label.setLayoutData(gd);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.config.IStandbyContentPart#getControl()
     */
    public Control getControl() {
        return contentComposite;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.config.IStandbyContentPart#init(org.eclipse.ui.intro.IIntroPart)
     */
    public void init(IIntroPart introPart, IMemento memento) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.config.IStandbyContentPart#setInput(java.lang.Object)
     */
    public void setInput(Object input) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.config.IStandbyContentPart#setFocus()
     */
    public void setFocus() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.config.IStandbyContentPart#dispose()
     */
    public void dispose() {


    }

    public void saveState(IMemento memento) {
    }

}