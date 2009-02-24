/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl.parts;


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.intro.impl.Messages;
import org.eclipse.ui.internal.intro.impl.swt.PageStyleManager;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.config.IStandbyContentPart;



public class EmptyStandbyContentPart implements IStandbyContentPart {

    private Composite contentComposite;
    private Text contentText;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.config.IStandbyContentPart#createPartControl(org.eclipse.swt.widgets.Composite,
     *      org.eclipse.ui.forms.widgets.FormToolkit)
     */
    public void createPartControl(Composite parent, FormToolkit toolkit) {
        contentComposite = toolkit.createComposite(parent);
        contentComposite.setLayout(new GridLayout());
        // Util.highlight(contentComposite, SWT.COLOR_YELLOW);

        String text = Messages.EmptyStandbyContentPart_text;
        Label label = toolkit.createLabel(contentComposite, text, SWT.WRAP);
        label.setFont(PageStyleManager.getBannerFont());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        label.setLayoutData(gd);

        contentText = toolkit.createText(contentComposite, " ", SWT.MULTI //$NON-NLS-1$
                | SWT.WRAP);
        GridData textGd = new GridData(GridData.FILL_BOTH);
        contentText.setLayoutData(textGd);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.config.IStandbyContentPart#getControl()
     */
    public Control getControl() {
        return contentComposite;
    }


    public void setMessage(String message) {
        if (message != null) {
            contentText.setText(message);
            contentComposite.layout();
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.config.IStandbyContentPart#init(org.eclipse.ui.intro.IIntroPart)
     */
    public void init(IIntroPart introPart, IMemento memento) {
        // no-op
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.config.IStandbyContentPart#setInput(java.lang.Object)
     */
    public void setInput(Object input) {
        if (input != null)
            setMessage((String) input);
        else
            setMessage(""); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.config.IStandbyContentPart#setFocus()
     */
    public void setFocus() {
        // no-op
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.config.IStandbyContentPart#dispose()
     */
    public void dispose() {
        // no-op
    }

    public void saveState(IMemento memento) {
        // no-op
    }

}
