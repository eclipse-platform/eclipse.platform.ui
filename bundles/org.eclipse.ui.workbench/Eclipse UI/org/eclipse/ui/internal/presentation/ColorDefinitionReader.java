/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * The <code>ColorDefinitionReader</code> reads the color definitions from the 
 * plugin registry.
 * 
 * @since 3.0
 */
public class ColorDefinitionReader extends RegistryReader {
	public static String ATT_DEFAULTS_TO = "defaultsTo"; //$NON-NLS-1$
	public static String ATT_ID = "id"; //$NON-NLS-1$
	public static String ATT_CATEGORYID = "categoryId"; //$NON-NLS-1$
	public static String ATT_DIRECTION = "direction"; //$NON-NLS-1$
	public static String ATT_LABEL = "label"; //$NON-NLS-1$
	public static String ATT_VALUE = "value"; //$NON-NLS-1$
	public static String ATT_PERCENTAGE = "percentage"; //$NON-NLS-1$
	public static String ATT_INITIALVALUE = "initialValue"; //$NON-NLS-1$
	public static String TAG_COLORDEFINITION = "colorDefinition"; //$NON-NLS-1$
	public static String TAG_GRADIENTDEFINITION = "gradientDefinition"; //$NON-NLS-1$
	public static String TAG_GRADIENTPART = "gradientPart"; //$NON-NLS-1$
	public static String TAG_COLOROVERRIDE = "colorOverride"; //$NON-NLS-1$

	/**
	 * The translation bundle in which to look up internationalized text.
	 */
	private final static ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(ColorDefinitionReader.class.getName());

	private Collection colorDefinitions = new ArrayList();
	private Collection gradientDefinitions = new ArrayList();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
	 */
	public boolean readElement(IConfigurationElement element) {

	    if (element.getName().equals(TAG_COLORDEFINITION) 
	            || element.getName().equals(TAG_COLOROVERRIDE)) {
			String name = element.getAttribute(ATT_LABEL);
	
			String id = element.getAttribute(ATT_ID);
	
			String defaultMapping = element.getAttribute(ATT_DEFAULTS_TO);
	
			String value = element.getAttribute(ATT_VALUE);
	
			if ((value == null && defaultMapping == null)
				|| (value != null && defaultMapping != null)) {
				logError(element, RESOURCE_BUNDLE.getString("badDefault")); //$NON-NLS-1$
				return true;
			}
	
			String categoryId = element.getAttribute(ATT_CATEGORYID);
			
			String description = null;
	
			IConfigurationElement[] descriptions =
				element.getChildren(TAG_DESCRIPTION);
	
			if (descriptions.length > 0)
				description = descriptions[0].getValue();
	
			colorDefinitions.add(
				new ColorDefinition(
					name,
					id,
					defaultMapping,
					value,
					categoryId,
					description,
					element
						.getDeclaringExtension()
						.getDeclaringPluginDescriptor()
						.getUniqueIdentifier()));
			return true;
	    }
	    else if (element.getName().equals(TAG_GRADIENTDEFINITION)) {
			String name = element.getAttribute(ATT_LABEL);
			
			String id = element.getAttribute(ATT_ID);
			
			int direction = SWT.HORIZONTAL;
			String directionString = element.getAttribute(ATT_DIRECTION);

			if (directionString != null && directionString.equalsIgnoreCase("VERTICAL")) //$NON-NLS-1$ 
			    direction = SWT.VERTICAL;
						
            
			IConfigurationElement[] children = element.getChildren(TAG_GRADIENTPART);			
            
			String values [] = new String[children.length + 1];		
			
            values[0] = element.getAttribute(ATT_INITIALVALUE);
            if (values[0] == null || values[0].equals("")) { //$NON-NLS-1$
                logError(element, RESOURCE_BUNDLE.getString("badGradientValue")); //$NON-NLS-1$
                return true;                    
            }                        

            int [] percentages = new int[children.length];
            
            for (int i = 0; i < children.length; i++) {      
                String value = children[i].getAttribute(ATT_VALUE);
                if (value == null || value.equals("")) { //$NON-NLS-1$
                    logError(element, RESOURCE_BUNDLE.getString("badGradientValue")); //$NON-NLS-1$
                    return true;                    
                }
                values[i + 1] = value;
                
                try {
                    int percentage = Integer.parseInt(children[i].getAttribute(ATT_PERCENTAGE));
                    if (percentage < 0) {
	                    logError(element, RESOURCE_BUNDLE.getString("badPercentage")); //$NON-NLS-1$
	                    return true;
                    }
                    percentages[i] = percentage;
                }
                catch (NumberFormatException e) {
                    logError(element, RESOURCE_BUNDLE.getString("badPercentage")); //$NON-NLS-1$
                    return true;
                }               
            }
	
			String categoryId = element.getAttribute(ATT_CATEGORYID);
			
			String description = null;
	
			IConfigurationElement[] descriptions =
				element.getChildren(TAG_DESCRIPTION);
	
			if (descriptions.length > 0)
				description = descriptions[0].getValue();
	
			gradientDefinitions.add(
				new GradientDefinition(
					name,
					id,
					direction,
					values,
					percentages,
					categoryId,
					description,
					element
						.getDeclaringExtension()
						.getDeclaringPluginDescriptor()
						.getUniqueIdentifier()));
			return true;
	        
	    }
	    return false;
	}

	/**
	 * Read the color extensions within a registry.
	 * 
	 * @param registry the <code>IPluginRegistry</code> to read from.
	 */
	void readRegistry(IPluginRegistry in) {
		readRegistry(in, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_COLOR_DEFINITIONS);
	}
	
	public Collection getColorDefinitions() {
	    ArrayList sorted = new ArrayList(colorDefinitions);
		Collections.sort(sorted, ColorDefinition.ID_COMPARATOR);	    
		return sorted;
	}
		
	public Collection getGradientDefinitions() {
	    ArrayList sorted = new ArrayList(gradientDefinitions);
		Collections.sort(sorted, GradientDefinition.ID_COMPARATOR);	    
		return sorted;
	}
}
