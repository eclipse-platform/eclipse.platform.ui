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
import org.eclipse.ui.internal.intro.impl.util.*;
import org.eclipse.ui.intro.*;
import org.eclipse.ui.intro.config.*;

public class StandbyPart {

    private FormToolkit toolkit;
    private IntroModelRoot model;
    private ImageHyperlink returnLink;
    private Composite container;
    private Composite content;
    //private ContextHelpStandbyPart helpPart;
    private IIntroPart introPart;

    // hastable has partIds as keys, and ControlKeys are values.
    private Hashtable cachedContentParts = new Hashtable();

    private ControlKey cachedControlKey;

    class StandbyLayout extends Layout {

        private int VGAP = 10;
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
            returnLink.setBounds(x, y, carea.width, lsize.y);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.parts.IStandbyContentPart#init(org.eclipse.ui.intro.IIntroPart)
     */
    public void init(IIntroPart introPart) {
        this.introPart = introPart;
    }

    public void createPartControl(Composite parent) {
        toolkit = new FormToolkit(parent.getDisplay());
        // parent container. Has custom layout. Has return link and content
        // stack composite.
        container = toolkit.createComposite(parent);
        container.setLayout(new StandbyLayout());

        // return hyper link.
        ImageUtil.registerImage(ImageUtil.BACK, "home_nav.gif"); //$NON-NLS-1$
        returnLink = toolkit.createImageHyperlink(container, SWT.WRAP);
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

        // By default, we always have the Context Help standby content.
        //addContextHelpPart();
        updateReturnLinkLabel();
    }

    public void setInput(Object input) {
        IStandbyContentPart standbyContent = cachedControlKey.getPart();
        standbyContent.setInput(input);
        updateReturnLinkLabel();
        container.layout();
    }
/*
    private void addContextHelpPart() {
        helpPart = new ContextHelpStandbyPart();
        addStandbyContentPart(IIntroConstants.HELP_CONTEXT_STANDBY_PART,
                helpPart);
        setTopControl(IIntroConstants.HELP_CONTEXT_STANDBY_PART);
    }
*/

    public void setTopControl(String key) {
        cachedControlKey = getCachedContent(key);
        if (cachedControlKey != null) {
            setTopControl(cachedControlKey.getControl());
        }
    }

    private void setTopControl(Control c) {
        StackLayout layout = (StackLayout) content.getLayout();
        layout.topControl = c;
        if (c instanceof Composite) ((Composite) c).layout();
        content.layout();
        container.layout();
    }

    private void updateReturnLinkLabel() {
        AbstractIntroPage page = model.getCurrentPage();
        String linkText = IntroPlugin.getString("StandbyPart.returnToIntro"); //$NON-NLS-1$
        if (page instanceof IntroPage) {
            linkText = IntroPlugin.getString("StandbyPart.returnTo") //$NON-NLS-1$
                    + " " + page.getTitle(); //$NON-NLS-1$
        }
        returnLink.setText(linkText);
        returnLink.setToolTipText(returnLink.getText());
    }

    private void doReturn() {
        IIntroPart part = PlatformUI.getWorkbench().getIntroManager()
                .getIntro();
        PlatformUI.getWorkbench().getIntroManager()
                .setIntroStandby(part, false);
    }

    /**
     * Calls dispose on all cached IStandbyContentParts.
     *  
     */
    public void dispose() {
        Enumeration values = cachedContentParts.elements();
        while (values.hasMoreElements()) {
            ControlKey controlKey = (ControlKey) values.nextElement();
            controlKey.getPart().dispose();
        }
        toolkit.dispose();
    }

    /*
     * Set focus on the IStandbyContentPart that corresponds to the top control
     * in the stack.
     * 
     * @see org.eclipse.ui.internal.intro.impl.parts.IStandbyContentPart#setFocus()
     */
    public void setFocus() {
        if (cachedControlKey != null) cachedControlKey.getPart().setFocus();
    }

    /**
     * Creates a standbyContent part in the stack only if one is not already
     * created. The partId is used as tke key in the cache. The value is an
     * instance of ControlKey that wraps a control/StandbyPart pair. This is
     * needed to retrive the control of a given standby part.
     * 
     * @param standbyContent
     */
    public Control addStandbyContentPart(String partId,
            IStandbyContentPart standbyContent) {

        ControlKey controlKey = getCachedContent(partId);
        if (controlKey == null) {
            standbyContent.init(introPart);
            try {
                standbyContent.createPartControl(content, toolkit);
            } catch (Exception e) {
                Log.error("Failed to create standby part: " + partId, e); //$NON-NLS-1$
                return null;
            }
            Control control = standbyContent.getControl();
            controlKey = new ControlKey(control, standbyContent);
            cachedContentParts.put(partId, controlKey);
        }
        return controlKey.getControl();
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
     * Model class to wrap Control and IStandbyContentPart pairs.
     */
    class ControlKey {

        Control c;
        IStandbyContentPart part;

        ControlKey(Control c, IStandbyContentPart part) {
            this.c = c;
            this.part = part;
        }

        /**
         * @return Returns the c.
         */
        public Control getControl() {
            return c;
        }

        /**
         * @return Returns the part.
         */
        public IStandbyContentPart getPart() {
            return part;
        }
    }
}