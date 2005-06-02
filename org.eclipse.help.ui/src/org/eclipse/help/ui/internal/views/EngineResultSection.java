/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.search.ISearchEngineResult;
import org.eclipse.help.ui.internal.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.*;
import org.osgi.framework.Bundle;

public class EngineResultSection {
    private SearchResultsPart part;

    private EngineDescriptor desc;

    private IStatus errorStatus;

    private ArrayList hits;

    private Section section;

    private Composite container;

    private FormText searchResults;

    private ImageHyperlink prevLink;

    private ImageHyperlink nextLink;

    private boolean needsUpdating;

    private FederatedSearchSorter sorter;

    private int HITS_PER_PAGE = 10;

    private static final String HREF_PROGRESS = "__progress__"; //$NON-NLS-1$

    private static final String PROGRESS_VIEW = "org.eclipse.ui.views.ProgressView"; //$NON-NLS-1$

    private int resultOffset = 0;

    public EngineResultSection(SearchResultsPart part, EngineDescriptor desc) {
        this.part = part;
        this.desc = desc;
        hits = new ArrayList();
        sorter = new FederatedSearchSorter();
    }

    public boolean hasControl(Control control) {
        return searchResults.equals(control);
    }

    public boolean matches(EngineDescriptor desc) {
        return this.desc == desc;
    }

    public Control createControl(Composite parent, final FormToolkit toolkit) {
        section = toolkit.createSection(parent, Section.SHORT_TITLE_BAR
        		| Section.COMPACT
                | Section.TWISTIE | Section.EXPANDED
                | Section.LEFT_TEXT_CLIENT_ALIGNMENT);
        // section.marginHeight = 10;
        container = toolkit.createComposite(section);
        TableWrapLayout layout = new TableWrapLayout();
        layout.topMargin = 0;
        layout.bottomMargin = 0;
        layout.leftMargin = 0;
        layout.rightMargin = 0;
        layout.verticalSpacing = 0;
        container.setLayout(layout);
        createFormText(container, toolkit);
        searchResults.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        searchResults
                .setColor(
                        "summary", parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW)); //$NON-NLS-1$
        section.setClient(container);
        updateSectionTitle(0);
        section.addExpansionListener(new IExpansionListener() {
            public void expansionStateChanging(ExpansionEvent e) {
                if (needsUpdating)
                    asyncUpdateResults(true, false);
            }

            public void expansionStateChanged(ExpansionEvent e) {
            }
        });
        return section;
    }

    private void createFormText(Composite parent, FormToolkit toolkit) {
        searchResults = toolkit.createFormText(parent, false);
        searchResults.setColor(FormColors.TITLE, toolkit.getColors().getColor(
                FormColors.TITLE));
        searchResults.marginHeight = 5;
        String topicKey = IHelpUIConstants.IMAGE_FILE_F1TOPIC;
        String nwKey = IHelpUIConstants.IMAGE_NW;
        String searchKey = IHelpUIConstants.IMAGE_HELP_SEARCH;
        searchResults.setImage(topicKey, HelpUIResources.getImage(topicKey));
        searchResults.setImage(nwKey, HelpUIResources.getImage(nwKey));
        searchResults.setImage(searchKey, HelpUIResources.getImage(searchKey));
        searchResults.setColor("summary", parent.getDisplay().getSystemColor( //$NON-NLS-1$
                SWT.COLOR_WIDGET_DARK_SHADOW));
        searchResults.setImage(ISharedImages.IMG_TOOL_FORWARD, PlatformUI
                .getWorkbench().getSharedImages().getImage(
                        ISharedImages.IMG_TOOL_FORWARD));
        searchResults.setImage(ISharedImages.IMG_TOOL_BACK, PlatformUI
                .getWorkbench().getSharedImages().getImage(
                        ISharedImages.IMG_TOOL_BACK));
        searchResults.setImage(ISharedImages.IMG_OBJS_ERROR_TSK, PlatformUI
                .getWorkbench().getSharedImages().getImage(
                        ISharedImages.IMG_OBJS_ERROR_TSK));
        searchResults.setImage(IHelpUIConstants.IMAGE_ADD_BOOKMARK,
                HelpUIResources.getImage(IHelpUIConstants.IMAGE_ADD_BOOKMARK));
        searchResults.setImage(desc.getId(), desc.getIconImage());
        searchResults.addHyperlinkListener(new IHyperlinkListener() {
            public void linkActivated(HyperlinkEvent e) {
                Object href = e.getHref();
                if (HREF_PROGRESS.equals(href)) {
                    showProgressView();
                } else if (((String) href).startsWith("bmk:")) { //$NON-NLS-1$
                    doBookmark(e.getLabel(), (String) href);
                } else
                    part.doOpenLink(e.getHref());
            }

            public void linkEntered(HyperlinkEvent e) {
                part.parent.handleLinkEntered(e);
            }

            public void linkExited(HyperlinkEvent e) {
                part.parent.handleLinkExited(e);
            }
        });
        initializeText();
        part.parent.hookFormText(searchResults);
        needsUpdating = true;
    }

    private void initializeText() {
        Bundle bundle = Platform.getBundle("org.eclipse.ui.views"); //$NON-NLS-1$
        if (bundle != null) {
            StringBuffer buff = new StringBuffer();
            buff.append("<form>"); //$NON-NLS-1$
            buff.append("<p><a href=\""); //$NON-NLS-1$
            buff.append(HREF_PROGRESS);
            buff.append("\""); //$NON-NLS-1$
            if (Platform.getWS()!=Platform.WS_GTK) {
            	buff.append(" alt=\""); //$NON-NLS-1$
            	buff.append(Messages.EngineResultSection_progressTooltip); //$NON-NLS-1$
            	buff.append("\""); //$NON-NLS-1$
            }
            buff.append(">"); //$NON-NLS-1$
            buff.append(Messages.EngineResultSection_searchInProgress); //$NON-NLS-1$
            buff.append("</a></p></form>"); //$NON-NLS-1$
            searchResults.setText(buff.toString(), true, false);
        } else {
            searchResults.setText(Messages.EngineResultSection_progress2,
                    false, false); //$NON-NLS-1$
        }
    }

    private void showProgressView() {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                try {
                    page.showView(PROGRESS_VIEW);
                } catch (PartInitException e) {
                    HelpUIPlugin.logError(
                            Messages.EngineResultSection_progressError, e); //$NON-NLS-1$
                }
            }
        }
    }

    public synchronized void add(ISearchEngineResult match) {
        hits.add(match);
        asyncUpdateResults(false, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.help.internal.search.federated.ISearchEngineResultCollector#add(org.eclipse.help.internal.search.federated.ISearchEngineResult[])
     */
    public synchronized void add(ISearchEngineResult[] matches) {
        for (int i = 0; i < matches.length; i++)
            hits.add(matches[i]);
        asyncUpdateResults(false, false);
    }

    public synchronized void error(IStatus status) {
        errorStatus = status;
        asyncUpdateResults(false, false);
    }

    public synchronized void completed() {
        if (hits.size() == 0 && !searchResults.isDisposed())
            asyncUpdateResults(false, false);
    }

    public synchronized void canceling() {
        if (hits.size() == 0 && !searchResults.isDisposed()) {
            StringBuffer buff = new StringBuffer();
            buff.append("<form>"); //$NON-NLS-1$
            buff.append("<p><span color=\"summary\">");//$NON-NLS-1$
            buff.append(Messages.EngineResultSection_canceling); //$NON-NLS-1$
            buff.append("</span></p>"); //$NON-NLS-1$
            buff.append("</form>"); //$NON-NLS-1$
            searchResults.setText(buff.toString(), true, false);
        }
    }

    private void asyncUpdateResults(boolean now, final boolean scrollToBeginning) {
        Runnable runnable = new Runnable() {
            public void run() {
                BusyIndicator.showWhile(section.getDisplay(), new Runnable() {
                    public void run() {
                        updateResults(true);
                        if (scrollToBeginning) {
                            searchResults.setFocus();
                            FormToolkit.setControlVisible(section, true);
                            part.updateSeparatorVisibility();
                        }
                    }
                });
            }
        };
        if (section.isDisposed())
            return;
        if (now)
            section.getDisplay().syncExec(runnable);
        else
            section.getDisplay().asyncExec(runnable);
    }

    private ISearchEngineResult[] getResults() {
        ArrayList list = hits;
        if (desc.getEngineTypeId().equals(IHelpUIConstants.INTERNAL_HELP_ID)) {
            if (part.parent.isFilteredByRoles()) {
                list = new ArrayList();
                for (int i = 0; i < hits.size(); i++) {
                    ISearchEngineResult hit = (ISearchEngineResult) hits.get(i);
                    if (HelpBasePlugin.getActivitySupport().isEnabled(
                            hit.getHref()))
                        list.add(hit);
                }
            }
        }
        ISearchEngineResult[] results = (ISearchEngineResult[]) list
                .toArray(new ISearchEngineResult[list.size()]);
        if (part.getShowCategories())
            sorter.sort(null, results);
        return results;
    }

    void updateResults(boolean reflow) {
        ISearchEngineResult[] results = getResults();
        updateSectionTitle(results.length);
        StringBuffer buff = new StringBuffer();
        buff.append("<form>"); //$NON-NLS-1$
        IHelpResource oldCat = null;
        //boolean earlyExit = false;

        for (int i = resultOffset; i < results.length; i++) {
            if (i - resultOffset == HITS_PER_PAGE) {
                break;
            }
            ISearchEngineResult hit = results[i];
            IHelpResource cat = hit.getCategory();
            if (part.getShowCategories()
                    && cat != null
                    && (oldCat == null || !oldCat.getLabel().equals(
                            cat.getLabel()))) {
                buff.append("<p>"); //$NON-NLS-1$
                if (cat.getHref() != null) {
                    buff.append("<a bold=\"true\" href=\""); //$NON-NLS-1$
                    String absoluteHref = hit.toAbsoluteHref(cat.getHref(),
                            true);
                    buff.append(part.parent.escapeSpecialChars(absoluteHref));
                    buff.append("\">"); //$NON-NLS-1$
                    buff.append(cat.getLabel());
                    buff.append("</a>"); //$NON-NLS-1$
                } else {
                    buff.append("<b>"); //$NON-NLS-1$
                    buff.append(cat.getLabel());
                    buff.append("</b>"); //$NON-NLS-1$
                }
                buff.append("</p>"); //$NON-NLS-1$
                oldCat = cat;
            }
            int indent = part.getShowCategories() && cat != null ? 26 : 21;
            int bindent = part.getShowCategories() && cat != null ? 5 : 0;
            buff
                    .append("<li indent=\"" + indent + "\" bindent=\"" + bindent + "\" style=\"image\" value=\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            buff.append(desc.getId());
            buff.append("\">"); //$NON-NLS-1$
            buff.append("<a href=\""); //$NON-NLS-1$
            if (hit.getForceExternalWindow())
                buff.append("nw:"); //$NON-NLS-1$
            buff.append(part.parent.escapeSpecialChars(hit.toAbsoluteHref(hit
                    .getHref(), false)));
            buff.append("\""); //$NON-NLS-1$
            if (hit.getCategory() != null && Platform.getWS()!=Platform.WS_GTK) {
                buff.append(" alt=\""); //$NON-NLS-1$
                buff.append(hit.getCategory().getLabel());
                buff.append("\""); //$NON-NLS-1$
            }
            buff.append(">"); //$NON-NLS-1$
            String elabel = part.parent.escapeSpecialChars(hit.getLabel());
            buff.append(elabel);
            buff.append("</a>"); //$NON-NLS-1$
            if (!hit.getForceExternalWindow()) {
                buff.append(" <a href=\""); //$NON-NLS-1$ 
                buff.append("nw:");//$NON-NLS-1$ 
                String ahref = part.parent.escapeSpecialChars(hit
                        .toAbsoluteHref(hit.getHref(), true));
                buff.append(ahref);
                buff.append("\"><img href=\""); //$NON-NLS-1$ 
                buff.append(IHelpUIConstants.IMAGE_NW);
                buff.append("\""); //$NON-NLS-1$
                if (Platform.getWS()!=Platform.WS_GTK) {
                	buff.append(" alt=\""); //$NON-NLS-1$
                	buff.append(Messages.SearchResultsPart_nwtooltip);//$NON-NLS-1$ 
                	buff.append("\""); //$NON-NLS-1$
                }
                buff.append("/>"); //$NON-NLS-1$ 
                buff.append("</a>"); //$NON-NLS-1$
            }
            addBookmarkLink(buff, hit);
            if (part.getShowDescription()) {
                String edesc = hit.getDescription();
                if (edesc != null)
                    edesc = part.parent.escapeSpecialChars(edesc);
                String summary = getSummary(elabel, edesc);
                if (summary != null) {
                    buff.append("<br/>"); //$NON-NLS-1$
                    // buff.append("<span color=\"summary\">"); //$NON-NLS-1$
                    // System.out.println(summary);
                    buff.append(summary);
                    // buff.append("</span>"); //$NON-NLS-1$
                }
            }
            buff.append("</li>"); //$NON-NLS-1$
        }
        if (errorStatus != null)
            updateErrorStatus(buff);
        updateNavigation(results.length);
        buff.append("</form>"); //$NON-NLS-1$
        searchResults.setText(buff.toString(), true, false);
        section.layout();
        if (reflow)
            part.reflow();
    }

    private void addBookmarkLink(StringBuffer buff, ISearchEngineResult hit) {
        buff.append(" <a href=\""); //$NON-NLS-1$ 
        buff.append("bmk:");//$NON-NLS-1$ 
        String ahref = part.parent.escapeSpecialChars(hit.toAbsoluteHref(hit
                .getHref(), true));
        buff.append(ahref);
        buff.append("\"><img href=\""); //$NON-NLS-1$ 
        buff.append(IHelpUIConstants.IMAGE_ADD_BOOKMARK);
        buff.append("\""); //$NON-NLS-1$
        if (Platform.getWS()!=Platform.WS_GTK) {
        	buff.append(" alt=\""); //$NON-NLS-1$
        	buff.append(Messages.SearchResultsPart_bmktooltip);
        	buff.append("\""); //$NON-NLS-1$
        }
        buff.append(" text=\""); //$NON-NLS-1$
        buff.append(part.parent.escapeSpecialChars(hit.getLabel()));
        buff.append("\""); //$NON-NLS-1$ 
        buff.append("/>"); //$NON-NLS-1$ 
        buff.append("</a>"); //$NON-NLS-1$
    }

    private void updateErrorStatus(StringBuffer buff) {
        int indent = 21;
        buff.append("<li indent=\"" + indent + "\" style=\"image\" value=\""); //$NON-NLS-1$ //$NON-NLS-2$
        buff.append(ISharedImages.IMG_OBJS_ERROR_TSK);
        buff.append("\">"); //$NON-NLS-1$
        buff.append("<b>"); //$NON-NLS-1$
        buff.append(part.parent.escapeSpecialChars(errorStatus.getMessage()));
        buff.append("</b>"); //$NON-NLS-1$
        buff.append("<br/>"); //$NON-NLS-1$
        Throwable t = errorStatus.getException();
        if (t != null)
            buff.append(part.parent.escapeSpecialChars(t.getMessage()));
        buff.append("</li>"); //$NON-NLS-1$
    }

    private void updateNavigation(int size) {
        if (size > HITS_PER_PAGE) {
            if (prevLink == null) {
                FormToolkit toolkit = part.getToolkit();
                Composite navContainer = toolkit.createComposite(container);
                TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
                navContainer.setLayoutData(td);
                GridLayout glayout = new GridLayout();
                glayout.numColumns = 2;
                navContainer.setLayout(glayout);
                GridData gd;
                /*
                Label sep = toolkit.createLabel(navContainer, null,
                        SWT.SEPARATOR | SWT.HORIZONTAL);
                GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
                gd.horizontalSpan = 2;
                gd.widthHint = 2;
                sep.setLayoutData(gd);
                */
                prevLink = toolkit.createImageHyperlink(navContainer, SWT.NULL);

                prevLink.setText(NLS.bind(
                        Messages.EngineResultSection_previous,
                        "" + HITS_PER_PAGE)); //$NON-NLS-1$
                prevLink.setImage(PlatformUI.getWorkbench().getSharedImages()
                        .getImage(ISharedImages.IMG_TOOL_BACK));
                prevLink.addHyperlinkListener(new HyperlinkAdapter() {
                    public void linkActivated(HyperlinkEvent e) {
                        resultOffset -= HITS_PER_PAGE;
                        asyncUpdateResults(false, true);
                    }
                });
                nextLink = toolkit
                        .createImageHyperlink(navContainer, SWT.RIGHT);

                nextLink.setImage(PlatformUI.getWorkbench().getSharedImages()
                        .getImage(ISharedImages.IMG_TOOL_FORWARD));
                gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
                gd.grabExcessHorizontalSpace = true;
                nextLink.setLayoutData(gd);
                nextLink.addHyperlinkListener(new HyperlinkAdapter() {
                    public void linkActivated(HyperlinkEvent e) {
                        resultOffset += HITS_PER_PAGE;
                        asyncUpdateResults(false, true);
                    }
                });
            }
            prevLink.setVisible(resultOffset > 0);

            int nextOffset = resultOffset + HITS_PER_PAGE;
            int remainder = hits.size() - nextOffset;
            remainder = Math.min(remainder, HITS_PER_PAGE);

            nextLink.setText(NLS.bind(Messages.EngineResultSection_next,
                    "" + remainder)); //$NON-NLS-1$
            nextLink.setVisible(hits.size() >= resultOffset + HITS_PER_PAGE);
        } else {
            if (prevLink != null) {
                prevLink.getParent().setMenu(null);
                prevLink.getParent().dispose();
                prevLink = null;
                nextLink = null;
            }
        }
    }

    private String getSummary(String elabel, String edesc) {
        if (edesc != null) {
            if (!edesc.equals(elabel)) {
                if (edesc.length() > elabel.length()) {
                    String ldesc = edesc.substring(0, elabel.length());
                    if (ldesc.equalsIgnoreCase(elabel))
                        edesc = edesc.substring(elabel.length() + 1);
                }
                return edesc;
            }
        }
        return null;
    }

    private void updateSectionTitle(int size) {
        if (errorStatus != null) {
            Label label = part.getToolkit().createLabel(section, null);
            label.setImage(PlatformUI.getWorkbench().getSharedImages()
                    .getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
            section.setTextClient(label);
            section.setText(Messages.EngineResultSection_sectionTitle_error);//$NON-NLS-1$
        } else {
            section.setTextClient(null);
        }
        if (size == 1)
            section.setText(NLS.bind(
                    Messages.EngineResultSection_sectionTitle_hit, desc
                            .getLabel(), "" //$NON-NLS-1$
                            + hits.size()));
        else if (size <= HITS_PER_PAGE)
            section.setText(NLS.bind(
                    Messages.EngineResultSection_sectionTitle_hits, desc
                            .getLabel(), "" + hits.size())); //$NON-NLS-1$
        else {
            int from = (resultOffset + 1);
            int to = (resultOffset + HITS_PER_PAGE);
            to = Math.min(to, size);
            section.setText(NLS.bind(
                    Messages.EngineResultSection_sectionTitle_hitsRange,
                    new String[] { desc.getLabel(),
                            "" + from, "" + to, "" + size })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    private void doBookmark(final String label, String href) {
        final String fhref = href.substring(4);
        BusyIndicator.showWhile(container.getDisplay(), new Runnable() {
            public void run() {
                BaseHelpSystem.getBookmarkManager().addBookmark(fhref, label);
            }
        });
    }

    public void dispose() {
        part.parent.unhookFormText(searchResults);
        if (!section.isDisposed()) {
            recursiveSetMenu(section, null);
            section.dispose();
        }
    }

    private void recursiveSetMenu(Control control, Menu menu) {
        control.setMenu(menu);
        if (control instanceof Composite) {
            Composite parent = (Composite) control;
            Control[] children = parent.getChildren();
            for (int i = 0; i < children.length; i++) {
                recursiveSetMenu(children[i], menu);
            }
        }
    }
}
