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
package org.eclipse.ui.internal.themes;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.swt.graphics.Font;

/**
 * Theme descriptor for a view tab.
 *
 * @since 3.0
 */
public class TabThemeDescriptor implements ITabThemeDescriptor {

	public static final int TABLOOKSHOWTEXTONLY = 1;
	public static final int TABLOOKSHOWICONSONLY = 2;
	public static final int TABLOOKSHOWICONSANDTEXT = 3;
	
	private static final String TAG_TABLOOK="tabTheme";//$NON-NLS-1$	
	private static final String TAG_TABBORDERINFO="borderInfo";//$NON-NLS-1$
	private static final String TAG_TABIMAGEINFO="tabImage";//$NON-NLS-1$
	private static final String TAG_TABMARGININFO="marginInfo";//$NON-NLS-1$
	private static final String ATT_POSITION="position";//$NON-NLS-1$
	private static final String ATT_POSITIONTOP="top";//$NON-NLS-1$	
	private static final String ATT_POSITIONBOTTOM="bottom";//$NON-NLS-1$
	private static final String ATT_POSITIONALL="bottom";//$NON-NLS-1$
	private static final String ATT_SHOWINTAB="showInTab";//$NON-NLS-1$
	private static final String ATT_SHOWINTABICONS="iconsOnly";//$NON-NLS-1$
	private static final String ATT_SHOWINTABICONSANDTEXT="iconsAndText";//$NON-NLS-1$
	private static final String ATT_FIXEDHEIGHT="fixedHeight";//$NON-NLS-1$
	private static final String ATT_FIXEDWIDTH="fixedWidth";//$NON-NLS-1$
	private static final String ATT_DRAGINFOLDER="dragInFolder";//$NON-NLS-1$	
	private static final String ATT_SHOWCLOSE="showClose";//$NON-NLS-1$
	private static final String ATT_COLOR="color";//$NON-NLS-1$
	private static final String ATT_SIZE="size";//$NON-NLS-1$
	private static final String ATT_IMAGETYPE="imageType";//$NON-NLS-1$
	private static final String ATT_IMAGETYPESELECTED="selected";//$NON-NLS-1$
	private static final String ATT_IMAGETYPEUNSELECTED="unselected";//$NON-NLS-1$
	private static final String ATT_IMAGETYPEHOVER="hover";//$NON-NLS-1$	
	private static final String ATT_IMAGETYPEMOUSEDOWN="mousedown";//$NON-NLS-1$	
	private static final String ATT_IMAGETYPECLOSEACTIVE="closeActive";//$NON-NLS-1$	
	private static final String ATT_IMAGETYPECLOSEINACTIVE="closeInactive";//$NON-NLS-1$
	private static final String ATT_IMAGETYPECLOSEHOVER="closeHover";//$NON-NLS-1$				
	private static final String ATT_IMAGE="image";//$NON-NLS-1$
	private static final String ATT_TITLE_FONT = "font";//$NON-NLS-1$
	private static final String ATT_TITLE_HOVER_TEXT_COLOR = "hoverTextColor";//$NON-NLS-1$
	private static final String ATT_TITLE_ACTIVE_TEXT_COLOR = "activeTextColor";//$NON-NLS-1$
	private static final String ATT_TITLE_DEACTIVATED_TEXT_COLOR = "inactiveTextColor";//$NON-NLS-1$
	private static final String ATT_BORDER_STYLE = "borderStyle";//$NON-NLS-1$
	private static final String ATT_ITEM_MARGINS = "itemMargins";//$NON-NLS-1$
	private static final String ATT_SHOWTOOLTIP="showTooltip";//$NON-NLS-1$	
			
	private String id;
	private IConfigurationElement configElement;
	private boolean customTabDefined;
	private int tabPosition = -1;
	private ImageDescriptor selectedImageDesc;
	private ImageDescriptor unselectedImageDesc;
	private ImageDescriptor hoverImageDesc;
	private ImageDescriptor mouseDownImageDesc;
	private ImageDescriptor closeActiveImageDesc;
	private ImageDescriptor closeInactiveImageDesc;
	private ImageDescriptor closeHoverImageDesc;	
	private int	showInTab = TABLOOKSHOWTEXTONLY;
	private TabMarginInfo topMargin;
	private TabMarginInfo allMargins;
	private int tabFixedWidth = 0;
	private int tabFixedHeight = 0;	
	private boolean showClose;
	private boolean dragInFolder;
	private String titleFont;
	private int borderStyle = SWT.BORDER;
	private String hoverTextColor;
	private String activeTextColor;
	private String deactivatedTextColor;
	private int [] itemMargins;  /*top,left,bottom,right */
	private boolean showTooltip;	
		
	/**
	 * Create a new TabThemeDescriptor for an extension.
	 */
	public TabThemeDescriptor(IConfigurationElement e) throws CoreException {
		configElement = e;
		processExtension();
	}
	
	/*
	 * load a TabThemeDescriptor from the registry.
	 */
	private void processExtension() throws CoreException {
		String position = configElement.getAttribute(ATT_POSITION);
		if (position.equals(ATT_POSITIONTOP))
			tabPosition = SWT.TOP;
		else if (position.equals(ATT_POSITIONBOTTOM))
			tabPosition = SWT.BOTTOM;
		
		String showintab = configElement.getAttribute(ATT_SHOWINTAB);
		if (showintab != null) {
			if (showintab.equalsIgnoreCase(ATT_SHOWINTABICONS))
				showInTab = TABLOOKSHOWICONSONLY;
			if (showintab.equalsIgnoreCase(ATT_SHOWINTABICONSANDTEXT))
				showInTab = TABLOOKSHOWICONSANDTEXT;
		}

		String fixedheight = configElement.getAttribute(ATT_FIXEDHEIGHT);
		int fixedsize = 0;
		try {
			fixedsize = Integer.parseInt(fixedheight);
		} 
		catch (Exception e) {
			/* do nothing */
		}
		if (fixedsize > 0)
			tabFixedHeight = fixedsize;
	
		String fixedwidth = configElement.getAttribute(ATT_FIXEDWIDTH);
		fixedsize = 0;
		try {
			fixedsize = Integer.parseInt(fixedwidth);
		} 
		catch (Exception e) {
			/* do nothing */
		}		
		if (fixedsize > 0)
			tabFixedWidth = fixedsize;
		
		showClose = new Boolean(configElement.getAttribute(ATT_SHOWCLOSE)).booleanValue();
		dragInFolder = new Boolean(configElement.getAttribute(ATT_DRAGINFOLDER)).booleanValue();
		showTooltip = new Boolean(configElement.getAttribute(ATT_SHOWTOOLTIP)).booleanValue();			

		String style = configElement.getAttribute(ATT_BORDER_STYLE);
		if (style != null){
			if (style.equalsIgnoreCase("none"))//$NON-NLS-1$
				borderStyle = SWT.NONE;
			else if (style.equalsIgnoreCase("shadow"))//$NON-NLS-1$
				borderStyle = SWT.BORDER|SWT.FLAT;
		}
		
		/* get the font */
		titleFont = configElement.getAttribute(ATT_TITLE_FONT);
		
		/* get the foreground colors */
		hoverTextColor = configElement.getAttribute(ATT_TITLE_HOVER_TEXT_COLOR);
		activeTextColor = configElement.getAttribute(ATT_TITLE_ACTIVE_TEXT_COLOR);
		deactivatedTextColor = configElement.getAttribute(ATT_TITLE_DEACTIVATED_TEXT_COLOR);

		/* get the item margins */
		itemMargins = processItemMargins(configElement.getAttribute(ATT_ITEM_MARGINS));
		
		IConfigurationElement [] marginchildren = configElement.getChildren(TAG_TABMARGININFO);
		if (marginchildren.length > 0)
			processTabMarginInfo(marginchildren);
		
		IConfigurationElement [] borderchildren = configElement.getChildren(TAG_TABBORDERINFO);
		if (borderchildren.length > 0)	 	
			processTabBorderInfo(borderchildren);

		IConfigurationElement [] imagechildren = configElement.getChildren(TAG_TABIMAGEINFO);
		if (imagechildren.length > 0)
			processTabImageInfo(imagechildren);

		customTabDefined = true;
	}	
	
	private boolean processTabMarginInfo(IConfigurationElement [] children) {
		String position = null;
		String color = null;
		String size = null;	
		int marginsize = 0;
		int marginposition = -1;
		
		for (int nX = 0; nX < children.length; nX ++) {
			 IConfigurationElement child = children[nX];
			position = child.getAttribute(ATT_POSITION);
			 size = child.getAttribute(ATT_SIZE);
			 color = child.getAttribute(ATT_COLOR);	
				
			 if (position != null) {
				 if (position.equals(ATT_POSITIONTOP))
					 marginposition = SWT.TOP;
				 else if (position.equals(ATT_POSITIONBOTTOM))
					 marginposition = SWT.BOTTOM;
				 else if (position.equals(ATT_POSITIONALL))
					 marginposition = SWT.DEFAULT;
			 }
			
			 try {
				 marginsize = Integer.parseInt(size);
			 } 
			 catch (Exception e) {
			 	/* do nothing */
			 }
			 
			 Color margincolor = null;
			 RGB rgbvalue;
			 try {
				 rgbvalue = StringConverter.asRGB(color);
				 margincolor = new Color(null, rgbvalue);
		
			} 
			catch (Exception e){
				/* TODO error handling?? */
			}	
			if (marginposition == SWT.TOP)
				topMargin = new TabMarginInfo(marginposition, marginsize, margincolor);
		   else if (marginposition == SWT.DEFAULT)
			   allMargins = new TabMarginInfo(marginposition, marginsize, margincolor);	
		 }
			
		 return true;
	 }

	private boolean processTabBorderInfo(IConfigurationElement [] children) {
		return true;
 	}

	private boolean processTabImageInfo(IConfigurationElement [] children) {
		String pluginId = configElement.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier();
		String imageType = null;
		String image = null;
		
		for (int nX = 0; nX < children.length; nX ++) {
			IConfigurationElement child = children[nX];
			imageType = child.getAttribute(ATT_IMAGETYPE);
			image = child.getAttribute(ATT_IMAGE);
			
			if (imageType != null && image != null) {
				if (imageType.equalsIgnoreCase(ATT_IMAGETYPESELECTED))
					selectedImageDesc = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, image);	
				else if (imageType.equalsIgnoreCase(ATT_IMAGETYPEUNSELECTED))
					unselectedImageDesc = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, image);
				else if (imageType.equalsIgnoreCase(ATT_IMAGETYPEHOVER))
					hoverImageDesc = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, image);
				else if (imageType.equalsIgnoreCase(ATT_IMAGETYPEMOUSEDOWN))
					mouseDownImageDesc = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, image);
				else if (imageType.equalsIgnoreCase(ATT_IMAGETYPECLOSEACTIVE))
					closeActiveImageDesc = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, image);	
				else if (imageType.equalsIgnoreCase(ATT_IMAGETYPECLOSEINACTIVE))
					closeInactiveImageDesc = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, image);
				else if (imageType.equalsIgnoreCase(ATT_IMAGETYPECLOSEHOVER))
						closeHoverImageDesc = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, image);									
			}	
		}				
		return true;
	}
	
	class TabMarginInfo {
		public int position;
		public int marginSize;
		public Color marginColor;
		
		public TabMarginInfo(int position, int size, Color margincolor) {
			this.position = position;
			this.marginSize = size;
			this.marginColor = margincolor;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#getCloseActiveImageDesc()
	 */
	public ImageDescriptor getCloseActiveImageDesc() {
		return closeActiveImageDesc;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#getCloseInactiveImageDesc()
	 */
	public ImageDescriptor getCloseInactiveImageDesc() {
		return closeInactiveImageDesc;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#getCloseInactiveImageDesc()
	 */
	public ImageDescriptor getCloseHoverImageDesc() {
		return closeHoverImageDesc;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#isCustomTabDefined()
	 */
	public boolean isCustomTabDefined() {
		return customTabDefined;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#isDragInFolder()
	 */
	public boolean isDragInFolder() {
		return dragInFolder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#getHoverImageDesc()
	 */
	public ImageDescriptor getHoverImageDesc() {
		return hoverImageDesc;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#getMouseDownImageDesc()
	 */
	public ImageDescriptor getMouseDownImageDesc() {
		return mouseDownImageDesc;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#getSelectedImageDesc()
	 */
	public ImageDescriptor getSelectedImageDesc() {
		return selectedImageDesc;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#isShowClose()
	 */
	public boolean isShowClose() {
		return showClose;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#isShowClose()
	 */
	public boolean isShowTooltip() {
		return showTooltip;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#getShowInTab()
	 */
	public int getShowInTab() {
		return showInTab;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#getTabFixedHeight()
	 */
	public int getTabFixedHeight() {
		return tabFixedHeight;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#getTabFixedWidth()
	 */
	public int getTabFixedWidth() {
		return tabFixedWidth;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#getTabPosition()
	 */
	public int getTabPosition() {
		return tabPosition;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#getUnselectedImageDesc()
	 */
	public ImageDescriptor getUnselectedImageDesc() {
		return unselectedImageDesc;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#getTabMarginSize(int)
	 */
	public int getTabMarginSize(int position) {
		if 	((position == SWT.TOP) && (topMargin != null))
			return topMargin.marginSize;
	   if 	((position == SWT.DEFAULT) && (allMargins != null))
		   return allMargins.marginSize;		 
		return -1;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.ITabThemeDescriptor#getTabMarginColor(int)
	 */
	public Color getTabMarginColor(int position) {
		if 	((position == SWT.TOP) && (topMargin != null))
			return topMargin.marginColor;
	   if 	((position == SWT.DEFAULT) && (allMargins != null))
		   return allMargins.marginColor;				 
		return null;
	}
	
	public Font  getFont (String key) {
		if (titleFont == null)
			return null;
		return new Font(null, StringConverter.asFontData(titleFont));
	}

	public Color getColor (String key) {
		String result = hoverTextColor;
		if (key == IThemeDescriptor.TAB_TITLE_TEXT_COLOR_ACTIVE)
			result = activeTextColor;
		else if (key == IThemeDescriptor.TAB_TITLE_TEXT_COLOR_DEACTIVATED)
			result = deactivatedTextColor;
		if (result == null)
			return null;
						
		return new Color(null, StringConverter.asRGB(result));
	}
	
	public int  getBorderStyle () {
		return borderStyle;
	}
	
	private Color [] createColors (String[] value) {
		Color [] result = new Color [value.length];
		for (int i = 0; i <= result.length-1; i++) {
			result[i] = new Color(null, StringConverter.asRGB(value[i]));
		}
		return result;
	}

	/*
	 * Builds an array out of strings like "10|6|4|4"
	 */
	private int [] processItemMargins (String value) {
		if (value != null) {
			value = StringConverter.removeWhiteSpaces(value);
			ArrayList list = new ArrayList();
			StringTokenizer stok = new StringTokenizer(value, "|");
			int [] result = new int[stok.countTokens()];
			for (int i = 0; i <=  result.length -1; i++) {
				result[i] = Integer.valueOf(stok.nextToken()).intValue();
			}
			return result;
		}
		return null;
	}

	/* return the Margins - the following order
	 * 				left, top, right, bottom
	 */		
	public int [] getItemMargins () {
		return itemMargins;
	}
	
}
