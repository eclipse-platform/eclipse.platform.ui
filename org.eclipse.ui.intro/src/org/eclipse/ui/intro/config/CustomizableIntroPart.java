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
 * two presentation: an SWT browser based presentatin, and a UI forms
 * presentation. Based on the configuration, one is chosen on startup. If a
 * Browser based prsentation is selected, and the intro is being loaded on a
 * platform that does not support the SWT Browser control, the default behavior
 * is to degrade to UI forms prsentation. Content displayed in this intro part
 * can be static or dynamic. Static is html files, dynamic is markup in content
 * files. Again, both of whch can be specified using the above extension point.
 * <p>
 * Memento Support: This intro part tries to restore its presvious state when
 * posible. The state of the intro page is remembered, along with which standby
 * content content part was opened. IStandbyContent parts are passed the Intro's
 * memento shortly after construction, and are expected to restore there own
 * state based on the momento. The customizable intro part handles there initial
 * creation on load, and leaves restoring state to content part. Same with
 * saving state. The memento is paased shortlt before shutdown to enable storing
 * of part specific data.
 * 
 * Note: This class was made public for re-use, as-is, as a valid class for the
 * <code>org.eclipse.ui.intro</code> extension point. It is not intended to be
 * subclassed or used otheriwse.
 * </p>
 * 
 * @since 3.0
 */
public final class CustomizableIntroPart extends IntroPart implements
        IIntroConstants, IRegistryChangeListener {

    private IntroPartPresentation presentation;
    private StandbyPart standbyPart;
    private Composite container;
    private IMemento memento;
    private IntroModelRoot model;

    // Adapter factory to abstract out the StandbyPart implementation from APIs.
    IAdapterFactory factory = new IAdapterFactory() {

        public Class[] getAdapterList() {
            return new Class[] { StandbyPart.class, IntroPartPresentation.class };
        }

        public Object getAdapter(Object adaptableObject, Class adapterType) {
            if (!(adaptableObject instanceof CustomizableIntroPart))
                return null;

            if (adapterType.equals(StandbyPart.class)) {
                return getStandbyPart();
            } else if (adapterType.equals(IntroPartPresentation.class)) {
                return getPresentation();
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
     * @see org.eclipse.ui.intro.IIntroPart#init(org.eclipse.ui.intro.IIntroSite,
     *      org.eclipse.ui.IMemento)
     */
    public void init(IIntroSite site, IMemento memento)
            throws PartInitException {
        super.init(site, memento);

        // load the correct model based in the current Intro Part id. Set the
        // IntroPartId in the manager class.
        String introId = getConfigurationElement().getAttribute("id"); //$NON-NLS-1$
        ExtensionPointManager extensionPointManager = IntroPlugin.getDefault()
                .getExtensionPointManager();
        extensionPointManager.setIntroId(introId);
        model = extensionPointManager.getCurrentModel();

        if (model != null && model.hasValidConfig()) {
            // we have a valid config contribution, get presentation. Make sure
            // you pass corret memento.
            presentation = model.getPresentation();
            if (presentation != null)
                presentation.init(this, getMemento(memento,
                        MEMENTO_PRESENTATION_TAG));
            // standby part is not created here for performance.
            this.memento = memento;
            // add the registry listerner for dynamic awarness.
            Platform.getExtensionRegistry().addRegistryChangeListener(this,
                    IIntroConstants.PLUGIN_ID);
        }

        if (model == null || !model.hasValidConfig())
            DialogUtil.displayErrorMessage(site.getShell(),
                    "CustomizableIntroPart.configNotFound", //$NON-NLS-1$
                    new Object[] { ModelLoaderUtil.getLogString(
                            getConfigurationElement(), null) }, null);

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
            // do not create the standby part here for performance.
        }
        // Util.highlightFocusControl();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IIntroPart#standbyStateChanged(boolean)
     */
    public void standbyStateChanged(boolean standby) {
        // do this only if there is a valid config.
        if (model != null && model.hasValidConfig()) {
            if (standby && standbyPart == null)
                // if standby part is not created yet, create it only if in
                // standby.
                createStandbyPart();
            handleSetFocus(standby);
            setTopControl(standby ? getStandbyControl()
                    : getPresentationControl());
            // triger state change in presentation to enable/disable toobar
            // actions.
            presentation.standbyStateChanged(standby);
        }
        // Util.highlightFocusControl();
    }

    /*
     * Create standby part. Called only when really needed.
     */
    private void createStandbyPart() {
        standbyPart = new StandbyPart(model);
        standbyPart.init(this, getMemento(memento, MEMENTO_STANDBY_PART_TAG));
        standbyPart.createPartControl((Composite) getControl());
    }


    private void handleSetFocus(boolean standby) {
        if (standby)
            // standby part is null when Intro has not gone into standby state
            // yet, or if
            if (standbyPart != null)
                standbyPart.setFocus();
            else
                presentation.setFocus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        handleSetFocus(IntroPlugin.isIntroStandby());
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
        // the Containet top control may have only one child if the stanndby
        // part is not created yet. This happens if the intro never goes into
        // standby. Doing this for performance.
        if (standbyPart != null)
            return container.getChildren()[1];
        return null;
    }

    private IntroPartPresentation getPresentation() {
        return presentation;
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
        if (model != null && model.hasValidConfig())
            Platform.getExtensionRegistry().removeRegistryChangeListener(this);
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


    public void saveState(IMemento memento) {
        // give presentation and standby part there own children to create a
        // name space for each.
        IMemento presentationMemento = memento
                .createChild(MEMENTO_PRESENTATION_TAG);
        IMemento standbyPartMemento = memento
                .createChild(MEMENTO_STANDBY_PART_TAG);
        if (presentation != null)
            presentation.saveState(presentationMemento);
        if (standbyPart != null)
            standbyPart.saveState(standbyPartMemento);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.IIntroPart#saveState(org.eclipse.ui.IMemento)
     */
    private IMemento getMemento(IMemento memento, String key) {
        if (memento == null)
            return null;
        return memento.getChild(key);
    }

    /**
     * Support dynamic awarness.
     * 
     * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
     */
    public void registryChanged(final IRegistryChangeEvent event) {
        // Clear cached models first, then update UI by delegating to
        // implementation. wrap in synchExec because notification is
        // asynchronous. The design here is that the notification is centralized
        // here, then this event propagates and each receiving class reacts
        // accordingly.
        Display.getDefault().syncExec(new Runnable() {

            public void run() {
                String currentPageId = model.getCurrentPageId();
                // clear model
                ExtensionPointManager.getInst().clear();
                // refresh to new model.
                model = ExtensionPointManager.getInst().getCurrentModel();
                // reuse existing presentation, since we just nulled it.
                model.setPresentation(getPresentation());
                // keep same page on refresh. No need for notification here.
                model.setCurrentPageId(currentPageId, false);
                if (presentation != null)
                    presentation.registryChanged(event);

            }
        });

    }

}



