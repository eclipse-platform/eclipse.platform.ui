/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.intro.impl.presentations;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.intro.impl.*;
import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.model.loader.*;
import org.eclipse.ui.internal.intro.impl.parts.*;
import org.eclipse.ui.internal.intro.impl.util.*;
import org.eclipse.ui.intro.*;
import org.eclipse.ui.part.*;

/**
 *  
 */
public class CustomizableIntroPart extends IntroPart {

    private IntroModelRoot model;

    private IntroPartPresentation presentation;

    private StandbyPart standbyPart;

    private Composite container;

    /**
     *  
     */
    public CustomizableIntroPart() {
        // model can not be loaded here because the configElement of this part
        // is still not loaded here.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.temp.IIntroPart#init(org.eclipse.ui.internal.temp.IIntroSite)
     */
    public void init(IIntroSite site) throws PartInitException {
        setSite(site);

        // load the correct model based in the current Intro Part id. Set the
        // IntroPartId in the manager class.
        String introId = getConfigurationElement().getAttribute("id"); //$NON-NLS-1$
        ExtensionPointManager extensionPointManager = IntroPlugin.getDefault()
                .getExtensionPointManager();
        extensionPointManager.setIntroId(introId);
        model = extensionPointManager.getCurrentModel();

        if (model != null && model.hasValidConfig()) {
            // we have a valid config contribution, get presentation.
            presentation = model.getPresentation();
            if (presentation != null)
                presentation.init(this);
            standbyPart = new StandbyPart(model);
            standbyPart.init(this);
        }

        // REVISIT: make sure this is handled better.
        if (model == null || !model.hasValidConfig())
            DialogUtil.displayErrorMessage(site.getShell(),
                    "Could not find a valid configuration for Intro Part: " //$NON-NLS-1$
                            + ModelLoaderUtil.getLogString(
                                    getConfigurationElement(), "id") //$NON-NLS-1$
                            + "\nCheck Log View for details.", null); //$NON-NLS-1$

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.IIntroPart#init(org.eclipse.ui.intro.IIntroSite,
     *      org.eclipse.ui.IMemento)
     */
    public void init(IIntroSite site, IMemento memento)
            throws PartInitException {
        init(site);
    }

    /**
     * Creates the UI based on how the InroPart has been configured.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        container = new Composite(parent, SWT.NULL);
        StackLayout layout = new StackLayout();
        layout.marginHeight = 0;
        layout.marginHeight = 0;
        container.setLayout(layout);

        if (model != null && model.hasValidConfig()) {
            presentation.createPartControl(container);
            standbyPart.createPartControl(container);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IIntroPart#standbyStateChanged(boolean)
     */
    public void standbyStateChanged(boolean standby) {
        // do this only if there is a valid config.
        if (model != null && model.hasValidConfig()) {
            if (standby)
                standbyPart.setFocus();
            else
                presentation.setFocus();
            setTopControl(standby ? getStandbyControl()
                    : getPresentationControl());
        }
    }

    private void setTopControl(Control c) {
        // container has stack layout. safe to cast.
        StackLayout layout = (StackLayout) container.getLayout();
        layout.topControl = c;
        container.layout();
    }

    private Control getPresentationControl() {
        return container.getChildren()[0];
    }

    private Control getStandbyControl() {
        return container.getChildren()[1];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        if (presentation != null)
            presentation.setFocus();
        if (standbyPart != null)
            standbyPart.setFocus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {
        super.dispose();
        // call dispose on both parts.
        if (presentation != null)
            presentation.dispose();
        if (standbyPart != null)
            standbyPart.dispose();
        // clear all loaded models since we are disposing of the Intro Part.
        IntroPlugin.getDefault().getExtensionPointManager().clear();
    }

    public void setStandbyInput(Object input) {
        standbyPart.setInput(input);
    }

    /**
     * @return Returns the standbyPart.
     */
    public StandbyPart getStandbyPart() {
        return standbyPart;

    }

}