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
package org.eclipse.ui.internal.intro.impl.parts;

import java.io.*;
import java.net.*;

import org.eclipse.help.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.help.*;
import org.eclipse.ui.internal.intro.impl.*;
import org.eclipse.ui.internal.intro.impl.util.*;
import org.eclipse.ui.intro.*;

/**
 *  
 */
public class ContextHelpPart implements IStandbyContentPart {

    private ScrolledForm form;
    private IPartListener2 partListener;
    private Label title;
    private Text phraseText;
    private FormText text;
    private String defaultText;
    private static final String HELP_KEY = "org.eclipse.ui.help"; //$NON-NLS-1$

    // private StandbyPart standbyPart;

    class PartListener implements IPartListener2 {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
         */
        public void partActivated(IWorkbenchPartReference ref) {
            handlePartActivation(ref, true);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
         */
        public void partBroughtToTop(IWorkbenchPartReference ref) {
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
         */
        public void partClosed(IWorkbenchPartReference ref) {
            handlePartActivation(ref, false);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
         */
        public void partDeactivated(IWorkbenchPartReference ref) {
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
         */
        public void partHidden(IWorkbenchPartReference ref) {
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
         */
        public void partInputChanged(IWorkbenchPartReference ref) {
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
         */
        public void partOpened(IWorkbenchPartReference ref) {
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
         */
        public void partVisible(IWorkbenchPartReference ref) {
        }
    }



    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.parts.IStandbyContentPart#init(org.eclipse.ui.intro.IIntroPart)
     */
    public void init(IIntroPart introPart) {
        partListener = new PartListener();
        defaultText = IntroPlugin.getString("ContextHelpPart.defaultText"); //$NON-NLS-1$
        ImageUtil.registerImage(ImageUtil.HELP_TOPIC, "help_topic.gif"); //$NON-NLS-1$
    }

    public void createPartControl(Composite parent, FormToolkit toolkit) {
        // parent form
        form = toolkit.createScrolledForm(parent);
        TableWrapLayout layout = new TableWrapLayout();
        form.getBody().setLayout(layout);
        Util.highlight(form.getBody(), SWT.COLOR_YELLOW);

        // help container. Has three colums (search, text, go)
        Composite helpContainer = toolkit.createComposite(form.getBody());
        GridLayout glayout = new GridLayout();
        glayout.numColumns = 3;
        glayout.marginWidth = glayout.marginHeight = 1;
        helpContainer.setLayout(glayout);
        helpContainer.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        toolkit.paintBordersFor(helpContainer);

        Label label = toolkit.createLabel(helpContainer, IntroPlugin
                .getString("ContextHelpPart.search"));
        label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
        phraseText = toolkit.createText(helpContainer, "");
        phraseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        final Button button = toolkit.createButton(helpContainer, IntroPlugin
                .getString("ContextHelpPart.button.go"), SWT.PUSH);
        button.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                doSearch(phraseText.getText());
            }
        });
        button.setEnabled(false);
        phraseText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String text = phraseText.getText();
                button.setEnabled(text.length() > 0);
            }
        });
        phraseText.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (e.character == '\r') {
                    if (button.isEnabled())
                        doSearch(phraseText.getText());
                }
            }
        });
        title = toolkit.createLabel(form.getBody(), null, SWT.WRAP);
        title.setText(IntroPlugin
                .getString("ContextHelpPart.contextHelpArea.Title"));
        title.setFont(JFaceResources.getHeaderFont());
        title.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
        text = toolkit.createFormText(form.getBody(), true);
        text.setImage(ImageUtil.HELP_TOPIC, ImageUtil
                .getImage(ImageUtil.HELP_TOPIC));
        text.addHyperlinkListener(new HyperlinkAdapter() {

            public void linkActivated(HyperlinkEvent e) {
                openLink(e.getHref());
            }
        });
        text.setLayoutData(new TableWrapData(TableWrapData.FILL,
                TableWrapData.FILL));
        text.setText(defaultText, false, false);
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        IPartService service = window.getPartService();
        service.addPartListener(partListener);
        toolkit.paintBordersFor(form.getBody());
    }

    public Control getControl() {
        return form;
    }

    private void doSearch(String phrase) {
        try {
            String ephrase = URLEncoder.encode(phrase, "UTF-8"); //$NON-NLS-1$
            String query = "tab=search&searchWord=" + ephrase;
            WorkbenchHelp.displayHelpResource(query);
        } catch (UnsupportedEncodingException e) {
            //TODO handle this for real
            System.out.println(e);
        }
    }

    private void handlePartActivation(IWorkbenchPartReference ref,
            boolean activated) {
        if (text.isDisposed())
            return;

        IWorkbenchPart part = ref.getPart(false);

        String partId = part.getSite().getId();
        // Ignore ourselves
        if (partId.equals("org.eclipse.ui.internal.introview"))
            return;
        if (activated) {
            title.setText(IntroPlugin
                    .getString("ContextHelpPart.whatIsArea.Title")
                    + " \"" + part.getSite().getRegisteredName() + "\"?");
            String helpText = createContextHelp(part);
            text.setText(helpText != null ? helpText : "", helpText != null,
                    false);
        } else {
            title.setText(IntroPlugin
                    .getString("ContextHelpPart.contextHelpArea.Title"));
            text.setText(defaultText, false, false);
        }
        form.getBody().layout();
        form.reflow(true);
    }

    private String createContextHelp(IWorkbenchPart part) {
        String text = null;
        if (part != null) {
            Display display = part.getSite().getShell().getDisplay();
            Control c = display.getFocusControl();
            if (c != null && c.isVisible() && !c.isDisposed()) {
                IContext helpContext = findHelpContext(c);
                if (helpContext != null) {
                    text = formatHelpContext(helpContext);
                }
            }
        }
        return text;
    }

    private IContext findHelpContext(Control c) {
        String contextId = null;
        Control node = c;
        do {
            contextId = (String) node.getData(HELP_KEY);
            if (contextId != null)
                break;
            node = node.getParent();
        } while (node != null);
        if (contextId != null) { return HelpSystem.getContext(contextId); }
        return null;
    }

    private String formatHelpContext(IContext context) {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("<form>");
        sbuf.append("<p>");
        sbuf.append(decodeContextBoldTags(context.getText()));
        sbuf.append("</p>");
        IHelpResource[] links = context.getRelatedTopics();
        if (links.length > 0) {
            for (int i = 0; i < links.length; i++) {
                IHelpResource link = links[i];
                sbuf.append("<li style=\"text\" indent=\"2\">");
                sbuf.append("<img href=\"");
                sbuf.append(ImageUtil.HELP_TOPIC);
                sbuf.append("\"/> ");
                sbuf.append("<a href=\"");
                sbuf.append(link.getHref());
                sbuf.append("\">");
                sbuf.append(link.getLabel());
                sbuf.append("</a>");
                sbuf.append("</li>");
            }
        }
        sbuf.append("</form>");
        return sbuf.toString();
    }

    private String decodeContextBoldTags(String contextString) {
        String decodedString = contextString.replaceAll("<@#\\$b>", "<b>");
        decodedString = decodedString.replaceAll("</@#\\$b>", "</b>");
        return decodedString;
    }

    private void openLink(Object href) {
        String url = (String) href;
        if (url != null)
            WorkbenchHelp.displayHelpResource(url);
    }

    public void dispose() {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window == null)
            return;
        IPartService service = window.getPartService();
        if (service == null)
            return;
        service.removePartListener(partListener);
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.parts.IStandbyContentPart#setFocus()
     */
    public void setFocus() {
        // REVISIT Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.parts.IStandbyContentPart#setInput(java.lang.Object)
     */
    public void setInput(Object input) {
        // does nothing.
    }
}