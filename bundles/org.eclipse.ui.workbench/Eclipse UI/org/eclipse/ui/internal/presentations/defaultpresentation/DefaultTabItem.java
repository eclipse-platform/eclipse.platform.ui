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
package org.eclipse.ui.internal.presentations.defaultpresentation;

import java.text.MessageFormat;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.presentations.newapi.PartInfo;
import org.eclipse.ui.internal.presentations.newapi.WidgetTabItem;
import org.eclipse.ui.internal.util.Util;

/**
 * @since 3.1
 */
public class DefaultTabItem extends WidgetTabItem {
    
    public static String DIRTY_PREFIX = "*";
    
    private boolean busy = false;
    private boolean bold = false;
    private Font lastFont = null;
    private String shortName = "";
    private String longName = "";
    
    public DefaultTabItem(DefaultTabFolder parent, int index, int flags) {
        super(parent.getFolder().createItem(flags, index));
        updateFont();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabItem#getBounds()
     */
    public Rectangle getBounds() {
        return getItem().getBounds();
    }
    
    public CTabItem getItem() {
        return (CTabItem)getWidget();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabItem#isShowing()
     */
    public boolean isShowing() {
        return getItem().isShowing();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabItem#setInfo(org.eclipse.ui.internal.presentations.newapi.PartInfo)
     */
    public void setInfo(PartInfo info) {
        CTabItem tabItem = getItem();
        
        shortName = computeShortName(info);
        longName = computeLongName(info);
        
        updateTabText();
        
        if (tabItem.getImage() != info.image) {
            tabItem.setImage(info.image);
        }

        String toolTipText = info.toolTip;
        if (toolTipText.equals(Util.ZERO_LENGTH_STRING)) {
            toolTipText = null;
        }

        if (!Util.equals(toolTipText, tabItem.getToolTipText())) {
            tabItem.setToolTipText(toolTipText);
        }
    }
    
    public void updateTabText() {
        CTabItem tabItem = getItem();
        
        String newName = tabItem.getParent().getSingle() ? longName : shortName;
        
        if (!Util.equals(newName, tabItem.getText())) {
            tabItem.setText(newName);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabItem#setBold(boolean)
     */
    public void setBold(boolean bold) {
        this.bold = bold;
        super.setBold(bold);
        updateFont();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabItem#setBusy(boolean)
     */
    public void setBusy(boolean busy) {
        this.busy = busy;
        super.setBusy(busy);
        updateFont();
    }
    
    private void updateFont() {
        CTabItem tabItem = getItem();
        
	    // Set the font if necessary
	    FontRegistry registry = PlatformUI.getWorkbench().getThemeManager()
	            .getCurrentTheme().getFontRegistry();
	
	    // Determine the parent font. We will set the tab's font
	    Font targetFont = null;
	
	    if (busy) {
	        targetFont = registry
	                .getItalic(IWorkbenchThemeConstants.TAB_TEXT_FONT);
	    } else {
	        
	        if (bold) {
	            targetFont = registry
	                    .getBold(IWorkbenchThemeConstants.TAB_TEXT_FONT);
	        }
	    }
	
	    if (lastFont != targetFont) {
	        tabItem.setFont(targetFont);
	        lastFont = targetFont;
	    }
    }

    private static String computeShortName(PartInfo info) {
        String text = info.name;
        
        if (info.dirty) {
            text = DIRTY_PREFIX + text; //$NON-NLS-1$
        }
        
        return text;
    }
    
    private static String computeLongName(PartInfo info) {
        String text = info.name;

        String contentDescription = info.contentDescription;

        if (contentDescription.equals("")) { //$NON-NLS-1$

            String titleTooltip = info.toolTip.trim();

            if (titleTooltip.endsWith(info.name))
                titleTooltip = titleTooltip.substring(0,
                        titleTooltip.lastIndexOf(info.name)).trim();

            if (titleTooltip.endsWith("\\")) //$NON-NLS-1$
                titleTooltip = titleTooltip.substring(0,
                        titleTooltip.lastIndexOf("\\")).trim(); //$NON-NLS-1$

            if (titleTooltip.endsWith("/")) //$NON-NLS-1$
                titleTooltip = titleTooltip.substring(0,
                        titleTooltip.lastIndexOf("/")).trim(); //$NON-NLS-1$

            contentDescription = titleTooltip;
        }

        if (!contentDescription.equals("")) { //$NON-NLS-1$
            text = MessageFormat
                    .format(
                            WorkbenchMessages
                                    .getString("EditorPart.AutoTitleFormat"), new String[] { text, contentDescription }); //$NON-NLS-1$
        }

        if (info.dirty) {
            text = DIRTY_PREFIX + text; //$NON-NLS-1$
        }
        
        return text;
    }
}
