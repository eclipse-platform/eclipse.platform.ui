/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.intro.impl.parts;

import java.util.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.intro.impl.*;
import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.model.loader.*;
import org.eclipse.ui.internal.intro.impl.util.*;
import org.eclipse.ui.intro.*;
import org.eclipse.ui.intro.config.*;

/**
 * Standby part is responsible for managing and creating IStandbycontent parts.
 * It knows how to create and cache content parts. It also handles saving and
 * restoring its own state. It does that by caching the id of the last content
 * part viewed and recreating that part on startup. It also manages the life
 * cycle of content parts by creating and initializing them at the right
 * moments. It also passes the momento at appropriate times to these content
 * parts to enable storing and retrieving of state by content parts. Content
 * parts are responsible for recreating there own state, including input, from
 * the passed momemnto. When the Return to Introduction link is clicked, the
 * Intro goes out of standby content mode, and the standby content parts are not
 * shown anymore until the user explicitly asks for a part again. This is
 * accomplished through a data flag on the CustomizableIntroPart control.
 *  
 */
public class StandbyPart implements IIntroConstants {

    private FormToolkit toolkit;
    private IntroModelRoot model;
    private ImageHyperlink returnLink;
    private Composite container;
    private Composite content;
    private IIntroPart introPart;
    private EmptyStandbyContentPart emptyPart;
    private IMemento memento;

    // hastable has partIds as keys, and ControlKeys are values.
    private Hashtable cachedContentParts = new Hashtable();

    private ControlKey cachedControlKey;

    class StandbyLayout extends Layout {

        private int VGAP = 20;
        private int VMARGIN = 5;
        private int HMARGIN = 5;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
         *      int, int, boolean)
         */
        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean flushCache) {
            Point lsize = returnLink.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                    flushCache);
            Point csize = content.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                    flushCache);
            int width = Math.max(lsize.x + 2 * HMARGIN, csize.x);
            int height = HMARGIN + lsize.y + VGAP + csize.y;
            return new Point(width, height);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
         *      boolean)
         */
        protected void layout(Composite composite, boolean flushCache) {
            Rectangle carea = composite.getClientArea();
            int lwidth = carea.width - HMARGIN * 2;
            Point lsize = returnLink.computeSize(lwidth, SWT.DEFAULT,
                    flushCache);
            int x = HMARGIN;
            int y = VMARGIN;
            returnLink.setBounds(x, y, lsize.x, lsize.y);
            x = 0;
            y += lsize.y + VGAP;
            content.setBounds(x, y, carea.width, carea.height - VMARGIN
                    - lsize.y - VGAP);
        }
    }

    /**
     * @param parent
     */
    public StandbyPart(IntroModelRoot model) {
        this.model = model;
    }


    public void init(IIntroPart introPart, IMemento memento) {
        this.introPart = introPart;
        this.memento = memento;
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

    public void createPartControl(Composite parent) {
        toolkit = new FormToolkit(parent.getDisplay());
        // parent container. Has custom layout. Has return link and content
        // stack composite.
        container = toolkit.createComposite(parent);
        container.setLayout(new StandbyLayout());

        // return hyper link.
        ImageUtil.registerImage(ImageUtil.BACK, "full/elcl16/home_nav.gif"); //$NON-NLS-1$
        returnLink = toolkit.createImageHyperlink(container, SWT.WRAP
                | SWT.CENTER);
        returnLink.setImage(ImageUtil.getImage(ImageUtil.BACK));
        returnLink.addHyperlinkListener(new HyperlinkAdapter() {

            public void linkActivated(HyperlinkEvent e) {
                doReturn();
            }
        });

        // content stack container
        content = toolkit.createComposite(container);
        StackLayout slayout = new StackLayout();
        slayout.marginWidth = slayout.marginHeight = 0;
        content.setLayout(slayout);

        boolean success = false;
        if (memento != null)
            success = restoreState(memento);

        if (!success)
            // add empty standby content.
            addEmptyPart();
        updateReturnLinkLabel();
    }

    /**
     * Empty content part used as backup for failures.
     *  
     */
    private void addEmptyPart() {
        emptyPart = new EmptyStandbyContentPart();
        addStandbyContentPart(EMPTY_STANDBY_CONTENT_PART, emptyPart);
        setTopControl(EMPTY_STANDBY_CONTENT_PART);
    }

    /**
     * Tries to create the last content part viewed, based on content part id..
     * 
     * @param memento
     * @return
     */
    private boolean restoreState(IMemento memento) {
        String contentPartId = memento
                .getString(MEMENTO_STANDBY_CONTENT_PART_ID_ATT);
        if (contentPartId == null)
            return false;
        // create the cached content part. Content parts are responsible for
        // storing and reading their input state.
        return showContentPart(contentPartId, null);
    }


    /**
     * Sets the into part to standby, and shows the passed standby part, with
     * the given input.
     * 
     * @param partId
     * @param input
     */
    public boolean showContentPart(String partId, String input) {
        // Get the IntroStandbyContentPart that maps to the given partId.
        IntroStandbyContentPart standbyPartContent = ExtensionPointManager
                .getInst().getSharedConfigExtensionsManager().getStandbyPart(
                        partId);

        if (standbyPartContent != null) {
            String standbyContentClassName = standbyPartContent.getClassName();
            String pluginId = standbyPartContent.getPluginId();

            Object standbyContentObject = ModelLoaderUtil.createClassInstance(
                    pluginId, standbyContentClassName);
            if (standbyContentObject instanceof IStandbyContentPart) {
                IStandbyContentPart contentPart = (IStandbyContentPart) standbyContentObject;
                Control c = addStandbyContentPart(partId, contentPart);
                if (c != null) {
                    try {
                        setTopControl(partId);
                        setInput(input);
                        return true;
                    } catch (Exception e) {
                        Log.error("Failed to set the input: " + input //$NON-NLS-1$
                                + " on standby part: " + partId, e); //$NON-NLS-1$
                        return false;
                    }
                }
            }
        }


        // we do not have a valid partId or we failed to instantiate part or
        // create the part content, show empty part and signal failure.
        setTopControl(EMPTY_STANDBY_CONTENT_PART);
        return false;
    }

    /**
     * Creates a standbyContent part in the stack only if one is not already
     * created. The partId is used as tke key in the cache. The value is an
     * instance of ControlKey that wraps a control/StandbyPart pair along with
     * the corresponding part id. This is needed to retrive the control of a
     * given standby part. The IMemento should be passed to the StandbyPart when
     * it is initialized.
     * 
     * @param standbyContent
     */
    public Control addStandbyContentPart(String partId,
            IStandbyContentPart standbyContent) {

        ControlKey controlKey = getCachedContent(partId);
        if (controlKey == null) {
            standbyContent.init(introPart, getMemento(memento,
                    MEMENTO_STANDBY_CONTENT_PART_TAG));
            try {
                standbyContent.createPartControl(content, toolkit);
            } catch (Exception e) {
                Log.error(
                        "Failed to create part for standby part: " + partId, e); //$NON-NLS-1$
                return null;
            }
            Control control = standbyContent.getControl();
            controlKey = new ControlKey(control, standbyContent, partId);
            cachedContentParts.put(partId, controlKey);
        }
        return controlKey.getControl();
    }



    public void setInput(Object input) {
        IStandbyContentPart standbyContent = cachedControlKey.getContentPart();
        standbyContent.setInput(input);
        updateReturnLinkLabel();
        container.layout();
    }


    public void setTopControl(String key) {
        cachedControlKey = getCachedContent(key);
        if (cachedControlKey != null) {
            setTopControl(cachedControlKey.getControl());
        }
    }

    private void setTopControl(Control c) {
        StackLayout layout = (StackLayout) content.getLayout();
        layout.topControl = c;
        if (c instanceof Composite)
            ((Composite) c).layout();
        content.layout();
        container.layout();
    }

    private void updateReturnLinkLabel() {
        AbstractIntroPage page = model.getCurrentPage();
        String linkText = IntroPlugin.getString("StandbyPart.returnToIntro"); //$NON-NLS-1$
        String toolTip = IntroPlugin.getString("StandbyPart.returnTo") //$NON-NLS-1$
                + " " + page.getTitle(); //$NON-NLS-1$

        returnLink.setText(linkText);
        returnLink.setToolTipText(toolTip);
    }

    private void doReturn() {
        // remove the flag to indicate that standbypart is no longer needed.
        ((CustomizableIntroPart) introPart).getControl().setData(
                IIntroConstants.SHOW_STANDBY_PART, null);
        IntroPlugin.setIntroStandby(false);
    }

    /**
     * Calls dispose on all cached IStandbyContentParts.
     *  
     */
    public void dispose() {

        Enumeration values = cachedContentParts.elements();
        while (values.hasMoreElements()) {
            ControlKey controlKey = (ControlKey) values.nextElement();
            controlKey.getContentPart().dispose();
        }
        toolkit.dispose();
    }

    /**
     * Save the current state of the standby part. It stores the cached content
     * part id for later creating it on restart. It also creates another
     * subclass momento to also give the standby content part its own name
     * space. This was momentos saved by different content parts will not
     * conflict.
     * 
     * @param memento
     *            the memento in which to store state information
     */
    public void saveState(IMemento memento) {
        // save cached content part id.
        if (cachedControlKey != null) {
            String contentPartId = cachedControlKey.getContentPartId();
            if (contentPartId == EMPTY_STANDBY_CONTENT_PART)
                // do not create memento for empty standby.
                return;
            memento.putString(MEMENTO_STANDBY_CONTENT_PART_ID_ATT,
                    contentPartId);
            // give standby part its own child to create a name space for
            // IStandbyPartContent contribution momentos.
            IMemento standbyContentPartMemento = memento
                    .createChild(MEMENTO_STANDBY_CONTENT_PART_TAG);
            // pass new memento to correct standby part.
            IStandbyContentPart standbyContentpart = cachedControlKey
                    .getContentPart();
            if (standbyContentpart != null)
                standbyContentpart.saveState(standbyContentPartMemento);
        }
    }


    /*
     * Set focus on the IStandbyContentPart that corresponds to the top control
     * in the stack.
     * 
     * @see org.eclipse.ui.internal.intro.impl.parts.IStandbyContentPart#setFocus()
     */
    public void setFocus() {
        // grab foxus first, then delegate. This way if content part does
        // nothing on focus, part still works.
        returnLink.setFocus();
        if (cachedControlKey != null)
            cachedControlKey.getContentPart().setFocus();
    }



    /**
     * Checks the standby cache stack if we have already created a similar
     * IStandbyContentPart. If not, returns null.
     * 
     * @param standbyContent
     * @return
     */
    private ControlKey getCachedContent(String key) {
        if (cachedContentParts.containsKey(key))
            return (ControlKey) cachedContentParts.get(key);
        return null;
    }

    /*
     * Model class to wrap Control and IStandbyContentPart pairs, along with the
     * representing ID..
     */
    class ControlKey {

        Control c;
        IStandbyContentPart part;
        String contentPartId;

        ControlKey(Control c, IStandbyContentPart part, String contentPartId) {
            this.c = c;
            this.part = part;
            this.contentPartId = contentPartId;
        }

        /**
         * @return Returns the c.
         */
        public Control getControl() {
            return c;
        }

        /**
         * @return Returns the content part.
         */
        public IStandbyContentPart getContentPart() {
            return part;
        }

        /**
         * @return Returns the part id.
         */
        public String getContentPartId() {
            return contentPartId;
        }
    }


}