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
package org.eclipse.ui.internal.presentations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ColorSchemeService;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.themes.ITheme;

/**
 * Controls the appearance of views stacked into the workbench.
 * 
 * @since 3.0
 */
public class EditorPresentation extends BasicStackPresentation {

    private IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault()
            .getPreferenceStore();

    private CTabFolder2Adapter showListListener = new CTabFolder2Adapter() {

        public void showList(CTabFolderEvent event) {
            CTabFolder tabFolder = getTabFolder();
            event.doit = false;
            Point p = tabFolder.toDisplay(event.x, event.y);
            p.y += +event.height;
            EditorPresentation.this.showList(tabFolder.getShell(), p.x, p.y);
        }
    };

    private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if (IPreferenceConstants.EDITOR_TAB_POSITION
                    .equals(propertyChangeEvent.getProperty())
                    && !isDisposed()) {
                int tabLocation = preferenceStore
                        .getInt(IPreferenceConstants.EDITOR_TAB_POSITION);
                setTabPosition(tabLocation);
            } else if (IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS
                    .equals(propertyChangeEvent.getProperty())
                    && !isDisposed()) {
                boolean traditionalTab = preferenceStore
                        .getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS);
                setTabStyle(traditionalTab);
            }

            boolean multiChanged = IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS
                    .equals(propertyChangeEvent.getProperty());
            boolean styleChanged = IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS
                    .equals(propertyChangeEvent.getProperty());
            CTabFolder tabFolder = getTabFolder();

            if ((multiChanged || styleChanged) && tabFolder != null) {
                if (multiChanged) {
                    boolean multi = preferenceStore
                            .getBoolean(IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS);
                    tabFolder.setSingleTab(!multi);
                } else {
                    boolean simple = preferenceStore
                            .getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS);
                    tabFolder.setSimpleTab(simple);
                }

                CTabItem[] tabItems = tabFolder.getItems();

                for (int i = 0; i < tabItems.length; i++) {
                    CTabItem tabItem = tabItems[i];
                    initTab(tabItem, getPartForTab(tabItem));
                }
            }
        }
    };

    public EditorPresentation(Composite parent, IStackPresentationSite newSite,
            int flags) {
        super(new CTabFolder(parent, SWT.BORDER), newSite);
        CTabFolder tabFolder = getTabFolder();
        tabFolder.addCTabFolder2Listener(showListListener);
        preferenceStore.addPropertyChangeListener(propertyChangeListener);
        int tabLocation = preferenceStore
                .getInt(IPreferenceConstants.EDITOR_TAB_POSITION);
        setTabPosition(tabLocation);
        tabFolder.setSingleTab(!preferenceStore
                .getBoolean(IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS));
        setTabStyle(preferenceStore
                .getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
        // do not support close box on unselected tabs.
        tabFolder.setUnselectedCloseVisible(true);
        // do not support icons in unselected tabs.
        tabFolder.setUnselectedImageVisible(true);
        //tabFolder.setBorderVisible(true);
        // set basic colors
        ColorSchemeService.setTabAttributes(this, tabFolder);
        updateGradient();
        tabFolder.setMinimizeVisible((flags & SWT.MIN) != 0);
        tabFolder.setMaximizeVisible((flags & SWT.MAX) != 0);
    }

	public void dispose() {
	    preferenceStore.removePropertyChangeListener(propertyChangeListener);	    
	    super.dispose();
	}
    
    protected void initTab(CTabItem tabItem, IPresentablePart part) {
        tabItem.setText(getLabelText(part, true, (getTabFolder().getStyle() & SWT.MULTI) == 0));
        tabItem.setImage(getLabelImage(part));
        tabItem.setToolTipText(getLabelToolTipText(part));
    }

    String getLabelText(IPresentablePart presentablePart, boolean dirtyLeft, boolean includePath) {
        String title = presentablePart.getTitle().trim();
        String text = title;

        if (includePath) {
            String titleTooltip = presentablePart.getTitleToolTip().trim();

            if (titleTooltip.endsWith(title))
                    titleTooltip = titleTooltip.substring(0,
                            titleTooltip.lastIndexOf(title)).trim();

            if (titleTooltip.endsWith("\\")) //$NON-NLS-1$
                    titleTooltip = titleTooltip.substring(0,
                            titleTooltip.lastIndexOf("\\")).trim(); //$NON-NLS-1$

            if (titleTooltip.endsWith("/")) //$NON-NLS-1$
                    titleTooltip = titleTooltip.substring(0,
                            titleTooltip.lastIndexOf("/")).trim(); //$NON-NLS-1$

            if (titleTooltip.length() >= 1) text += " - " + titleTooltip; //$NON-NLS-1$
        }

        if (presentablePart.isDirty()) {
            if (dirtyLeft)
                text = "* " + text; //$NON-NLS-1$
            else 
                text = text + " *"; //$NON-NLS-1$
        }

        return text;
    }
    
    Image getLabelImage(IPresentablePart presentablePart) {
        return presentablePart.getTitleImage();
    }

    String getLabelToolTipText(IPresentablePart presentablePart) {
        return presentablePart.getTitleToolTip();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.skins.Presentation#setActive(boolean)
     */
    public void setActive(boolean isActive) {
        super.setActive(isActive);

        updateGradient();
    }

    /**
     * Set the tab folder tab style to a tradional style tab
     * 
     * @param traditionalTab
     *            <code>true</code> if traditional style tabs should be used
     *            <code>false</code> otherwise.
     */
    protected void setTabStyle(boolean traditionalTab) {
        // set the tab style to non-simple
        getTabFolder().setSimpleTab(traditionalTab);
    }

    private void showList(Shell parentShell, int x, int y) {
        final CTabFolder tabFolder = getTabFolder();
        ArrayList items = new ArrayList(Arrays.asList(tabFolder.getItems()));

        for (Iterator iterator = items.iterator(); iterator.hasNext();) {
            CTabItem tabItem = (CTabItem) iterator.next();

            if (tabItem.isShowing()) iterator.remove();
        }

        if (items.isEmpty()) return;
		int shellStyle = SWT.RESIZE | SWT.ON_TOP | SWT.NO_TRIM;
		int tableStyle = SWT.V_SCROLL | SWT.H_SCROLL;
        final EditorList info = new EditorList(
                tabFolder.getShell(), shellStyle, tableStyle);
        info.setInput(this);
        Point size = info.computeSizeHint();
        int minX = 50; //labelComposite.getSize().x;
        int minY = 300;
        if (size.x < minX) size.x = minX;
        if (size.y < minY) size.y = minY;
        info.setSize(size.x, size.y);
        Rectangle bounds = Display.getCurrent().getBounds();
        if (x + size.x > bounds.width) x = bounds.width - size.x;
        if (y + size.y > bounds.height) y = bounds.height - size.y;
        info.setLocation(new Point(x, y));
        info.setVisible(true);
        info.setFocus();
        info.getTableViewer().getTable().getShell().addListener(SWT.Deactivate,
                new Listener() {

                    public void handleEvent(Event event) {
                        info.setVisible(false);
                    }
                });
    }

    /**
     * Update the tab folder's colours to match the current theme settings and
     * active state
     */
    private void updateGradient() {
        Color fgColor;
        ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager()
                .getCurrentTheme();
        FontRegistry fontRegistry = currentTheme.getFontRegistry();
        ColorRegistry colorRegistry = currentTheme.getColorRegistry();
        Color [] bgColors = new Color[2];
        int [] percent = new int[1];
        boolean vertical;
        if (isActive()) {
            fgColor = colorRegistry
                    .get(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR);
            bgColors[0] = colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START);
            bgColors[1] = colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END);
            percent[0] = currentTheme.getInt(IWorkbenchThemeConstants.ACTIVE_TAB_PERCENT);
            vertical = currentTheme.getBoolean(IWorkbenchThemeConstants.ACTIVE_TAB_VERTICAL);            
            
        } else {
            fgColor = colorRegistry
            		.get(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR);           
            bgColors[0] = colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START);
            bgColors[1] = colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END);
            percent[0] = currentTheme.getInt(IWorkbenchThemeConstants.INACTIVE_TAB_PERCENT);
            vertical = currentTheme.getBoolean(IWorkbenchThemeConstants.INACTIVE_TAB_VERTICAL);            
        }

        getTabFolder()
        .setFont(
                fontRegistry
                        .get(IWorkbenchThemeConstants.TAB_TEXT_FONT));

        drawGradient(fgColor, bgColors, percent, vertical);
    }
    
    void setSelection(CTabItem tabItem) {
        selectPart(getPartForTab(tabItem));
    }
    
    void close(IPresentablePart presentablePart) {
        getSite().close(presentablePart);
    }
}