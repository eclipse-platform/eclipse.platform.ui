/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.intro.config;

import org.eclipse.core.runtime.*;
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
 * A re-usable intro part that the Eclipse platform uses for its Out of the Box
 * Experience. It is a customizable intro part where both its presentation, and
 * its content can be customized based on a configuration. Both are contributed
 * using the org.eclipse.ui.intro.intro.config extension point. There are two
 * two presentation: an SWT brwoser based presentatin, and a UI forms
 * presentation. Based on the configuration, one is chosen on startup. If a
 * Brwoser based prsentation is selected, and the intro is being loaded on a
 * platform that does not support the SWT Brwoser control, the default behavior
 * is to degrade to UI forms prsentation. Content displayed in this intro part
 * can be static or dynamic. Static is html files, dynamic is markup in content
 * files. Again, both of whch can be specified using the above extension point.
 * <p>
 * Note: This class was made public on for re-use as-is as a valid class for the
 * <code>org.eclipse.ui.intro</code> extension point.
 * </p>
 * 
 * @since 3.0
 */
public final class CustomizableIntroPart extends IntroPart {

    private IntroModelRoot model;
    private IntroPartPresentation presentation;
    private StandbyPart standbyPart;
    private Composite container;

    // Adapter factory to abstract out the StandbyPart implementation from APIs.
    IAdapterFactory factory = new IAdapterFactory() {

        public Class[] getAdapterList() {
            return new Class[] { StandbyPart.class };
        }

        public Object getAdapter(Object adaptableObject, Class adapterType) {
            if (!(adaptableObject instanceof CustomizableIntroPart))
                return null;

            if (adapterType.equals(StandbyPart.class)) {
                return getStandbyPart();
            } else
                return null;
        }
    };

    public CustomizableIntroPart() {

        // register adapter to hide standbypart.
        Platform.getAdapterManager().registerAdapters(factory,
                CustomizableIntroPart.class);

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

        if (model == null || !model.hasValidConfig())
            DialogUtil.displayErrorMessage(site.getShell(),
                    "CustomizableIntroPart.configNotFound",
                    new Object[] { ModelLoaderUtil.getLogString(
                            getConfigurationElement(), null) }, null);
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
        // clean platform adapter.
        Platform.getAdapterManager().unregisterAdapters(factory,
                CustomizableIntroPart.class);
    }

    /**
     * @return Returns the standbyPart.
     */
    private StandbyPart getStandbyPart() {
        return standbyPart;

    }

    /**
     * Returns the primary control associated with this Intro part.
     * 
     * @return the SWT control which displays this Intro part's content, or
     *         <code>null</code> if this standby part's controls have not yet
     *         been created.
     */
    public Control getControl() {
        return container;
    }
}



