/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.themes.IColorFactory;

/**
 * Registry reader for themes.
 *
 * @since 3.0
 */
public class ThemeRegistryReader extends RegistryReader {

    /**
     * The translation bundle in which to look up internationalized text.
     */
    private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(ThemeRegistryReader.class.getName());

    private Collection categoryDefinitions = new HashSet();

    private Collection colorDefinitions = new HashSet();

    private Collection fontDefinitions = new HashSet();

    private ThemeDescriptor themeDescriptor = null;

    private ThemeRegistry themeRegistry;

    private Map dataMap = new HashMap();

    /**
     * ThemeRegistryReader constructor comment.
     */
    public ThemeRegistryReader() {
        super();
    }

    /**
     * Returns the category definitions.
     *
     * @return the category definitions
     */
    public Collection getCategoryDefinitions() {
        return categoryDefinitions;
    }

    /**
     * Returns the color definitions.
     *
     * @return the color definitions
     */
    public Collection getColorDefinitions() {
        return colorDefinitions;
    }

    /**
     * Returns the data map.
     * 
     * @return the data map
     */
    public Map getData() {
        return dataMap;
    }

    /**     
     * Returns the font definitions.
     *
     * @return the font definitions
     */
    public Collection getFontDefinitions() {
        return fontDefinitions;
    }

    /**
     * Read a category.
     * 
     * @param element the element to read
     * @return the new category
     */
    private ThemeElementCategory readCategory(IConfigurationElement element) {
        String name = element.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);

        String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
        String parentId = element.getAttribute(IWorkbenchRegistryConstants.ATT_PARENT_ID);

        String description = null;

        IConfigurationElement[] descriptions = element
                .getChildren(IWorkbenchRegistryConstants.TAG_DESCRIPTION);

        if (descriptions.length > 0) {
			description = descriptions[0].getValue();
		}

        return new ThemeElementCategory(name, id, parentId, description,
                element.getNamespace(), element);
    }

    /**
     * Read a color.
     * 
     * @param element the element to read
     * @return the new color
     */
    private ColorDefinition readColor(IConfigurationElement element) {
        String name = element.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);

        String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);

        String defaultMapping = element.getAttribute(IWorkbenchRegistryConstants.ATT_DEFAULTS_TO);

        String value = getPlatformSpecificColorValue(element
                .getChildren(IWorkbenchRegistryConstants.TAG_COLORVALUE));

        if (value == null) {
            value = getColorValue(element);
        }

        if ((value == null && defaultMapping == null)
                || (value != null && defaultMapping != null)) {
            logError(element, RESOURCE_BUNDLE.getString("Colors.badDefault")); //$NON-NLS-1$
            return null;
        }

        String categoryId = element.getAttribute(IWorkbenchRegistryConstants.ATT_CATEGORY_ID);

        String description = null;
        boolean isEditable = true;
        String isEditableString = element.getAttribute(IWorkbenchRegistryConstants.ATT_IS_EDITABLE);
        if (isEditableString != null) {
            isEditable = Boolean.valueOf(isEditableString).booleanValue();
        }

        IConfigurationElement[] descriptions = element
                .getChildren(IWorkbenchRegistryConstants.TAG_DESCRIPTION);

        if (descriptions.length > 0) {
			description = descriptions[0].getValue();
		}

        return new ColorDefinition(name, id, defaultMapping, value, categoryId,
                isEditable, description, element.getDeclaringExtension()
                        .getNamespace());
    }

    /**
     * Gets the color value, either via the value attribute or from a color 
     * factory.
     * 
     * @param element the element to check
     * @return the color string
     */
    private String getColorValue(IConfigurationElement element) {
        if (element == null) {
			return null;
		}

        String value = element.getAttribute(IWorkbenchRegistryConstants.ATT_VALUE);
        if (value == null) {
            value = checkColorFactory(element);
        }
        return value;
    }

    /**
     * Check for platform specific color values.  This will return the 
     * "best match" for the current platform.
     * 
     * @param elements the elements to check
     * @return the platform specific color, if any
     */
    private String getPlatformSpecificColorValue(
            IConfigurationElement[] elements) {
        return getColorValue(getBestPlatformMatch(elements));
    }

    /**
     * Get the element that has os/ws attributes that best match the current 
     * platform.
     * 
     * @param elements the elements to check
     * @return the best match, if any
     */
    private IConfigurationElement getBestPlatformMatch(
            IConfigurationElement[] elements) {
        IConfigurationElement match = null;

        String osname = Platform.getOS();
        String wsname = Platform.getWS();

        for (int i = 0; i < elements.length; i++) {
            IConfigurationElement element = elements[i];
            String elementOs = element.getAttribute(IWorkbenchRegistryConstants.ATT_OS);
            String elementWs = element.getAttribute(IWorkbenchRegistryConstants.ATT_WS);

            if (osname.equalsIgnoreCase(elementOs)) {
                if (wsname.equalsIgnoreCase(elementWs)) {
                    // best possible match.  Return
                    return element;
                }
                match = element;
            } else if (wsname.equalsIgnoreCase(elementWs)) {
                match = element;
            }
        }

        return match;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
     */
    public boolean readElement(IConfigurationElement element) {
        String elementName = element.getName();
        if (themeDescriptor == null && elementName.equals(IWorkbenchRegistryConstants.TAG_COLORDEFINITION)) {
            ColorDefinition definition = readColor(element);
            if (definition != null) {
                if (!colorDefinitions.contains(definition)) {
	                colorDefinitions.add(definition);
	                themeRegistry.add(definition);
                }
            }
            return true;
        } else if (themeDescriptor != null
                && elementName.equals(IWorkbenchRegistryConstants.TAG_COLOROVERRIDE)) {
            ColorDefinition definition = readColor(element);
            if (definition != null) {                
                themeDescriptor.add(definition);
            }
            return true;
        } else if (themeDescriptor == null
                && elementName.equals(IWorkbenchRegistryConstants.TAG_FONTDEFINITION)) {
            FontDefinition definition = readFont(element);
            if (definition != null) {
                if (!fontDefinitions.contains(definition)) {
	                fontDefinitions.add(definition);
	                themeRegistry.add(definition);
                }
            }
            return true;
        } else if (themeDescriptor != null
                && elementName.equals(IWorkbenchRegistryConstants.TAG_FONTOVERRIDE)) {
            FontDefinition definition = readFont(element);
            if (definition != null) {
                themeDescriptor.add(definition);
            }
            return true;
        } else if (themeDescriptor == null
                && elementName.equals(IWorkbenchRegistryConstants.TAG_CATEGORYDEFINITION)) {
            ThemeElementCategory definition = readCategory(element);
            if (definition != null) {
                if (!categoryDefinitions.contains(definition)) {
	                categoryDefinitions.add(definition);
	                themeRegistry.add(definition);
                }
            }
            return true;
        } else if (element.getName().equals(IWorkbenchRegistryConstants.TAG_THEME)) {
            if (themeDescriptor != null) {
				logError(element, RESOURCE_BUNDLE
                        .getString("Themes.badNesting")); //$NON-NLS-1$
			} else {
                themeDescriptor = readTheme(element);
                if (themeDescriptor != null) {
                    readElementChildren(element);
                    themeDescriptor = null;
                }
                return true;
            }
        } else if (themeDescriptor != null
                && elementName.equals(IWorkbenchRegistryConstants.TAG_DESCRIPTION)) {
            themeDescriptor.setDescription(element.getValue());
            return true;
        } else if (elementName.equals(IWorkbenchRegistryConstants.TAG_DATA)) {
            String name = element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
            String value = element.getAttribute(IWorkbenchRegistryConstants.ATT_VALUE);
            if (name == null || value == null) {
                logError(element, RESOURCE_BUNDLE.getString("Data.badData")); //$NON-NLS-1$			    
            } else {
                if (themeDescriptor != null) {
                    themeDescriptor.setData(name, value);
                } else {
                    themeRegistry.setData(name, value);
                    if (!dataMap.containsKey(name)) {
						dataMap.put(name, value);
					}
                }
            }
            return true;
        } else if (elementName.equals(IWorkbenchRegistryConstants.TAG_CATEGORYPRESENTATIONBINDING)) {
            String categoryId = element.getAttribute(IWorkbenchRegistryConstants.ATT_CATEGORY_ID);
            String presentationId = element.getAttribute(IWorkbenchRegistryConstants.ATT_PRESENTATIONID);
            themeRegistry.addCategoryPresentationBinding(categoryId,
                    presentationId);
            return true;
        }

        return false;
    }

    /**
     * Read a font.
     * 
     * @param element the element to read
     * @return the new font
     */
    private FontDefinition readFont(IConfigurationElement element) {
        String name = element.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);

        String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);

        String defaultMapping = element.getAttribute(IWorkbenchRegistryConstants.ATT_DEFAULTS_TO);

        String value = getPlatformSpecificFontValue(element
                .getChildren(IWorkbenchRegistryConstants.TAG_FONTVALUE));
        if (value == null) {
            value = element.getAttribute(IWorkbenchRegistryConstants.ATT_VALUE);
        }

        if (value != null && defaultMapping != null) {
            logError(element, RESOURCE_BUNDLE.getString("Fonts.badDefault")); //$NON-NLS-1$
            return null;
        }

        String categoryId = element.getAttribute(IWorkbenchRegistryConstants.ATT_CATEGORY_ID);

        boolean isEditable = true;
        String isEditableString = element.getAttribute(IWorkbenchRegistryConstants.ATT_IS_EDITABLE);
        if (isEditableString != null) {
            isEditable = Boolean.valueOf(isEditableString).booleanValue();
        }

        String description = null;

        IConfigurationElement[] descriptions = element
                .getChildren(IWorkbenchRegistryConstants.TAG_DESCRIPTION);

        if (descriptions.length > 0) {
			description = descriptions[0].getValue();
		}

        return new FontDefinition(name, id, defaultMapping, value, categoryId,
                isEditable, description);
    }

    /**
     * Check for platform specific font values.  This will return the 
     * "best match" for the current platform.
     * 
     * @param elements the elements to check
     * @return the platform specific font, if any
     */
    private String getPlatformSpecificFontValue(IConfigurationElement[] elements) {
        return getFontValue(getBestPlatformMatch(elements));
    }

    /**
     * Gets the font valu from the value attribute.
     * 
     * @param element the element to check
     * @return the font string
     */
    private String getFontValue(IConfigurationElement element) {
        if (element == null) {
			return null;
		}

        return element.getAttribute(IWorkbenchRegistryConstants.ATT_VALUE);
    }

    /**
     * Attempt to load the color value from the colorFactory attribute.
     *
     * @param element the element to load from 
     * @return the value, or null if it could not be obtained
     */
    private String checkColorFactory(IConfigurationElement element) {
        String value = null;
        if (element.getAttribute(IWorkbenchRegistryConstants.ATT_COLORFACTORY) != null
                || element.getChildren(IWorkbenchRegistryConstants.ATT_COLORFACTORY).length > 0) {
            try {
                IColorFactory factory = (IColorFactory) element
                        .createExecutableExtension(IWorkbenchRegistryConstants.ATT_COLORFACTORY);
                value = StringConverter.asString(factory.createColor());
            } catch (Exception e) {
                WorkbenchPlugin.log(RESOURCE_BUNDLE
                        .getString("Colors.badFactory"), //$NON-NLS-1$ 
                        WorkbenchPlugin.getStatus(e));
            }
        }
        return value;
    }

    /**
     * Read a theme.
     * 
     * @param element the element to read
     * @return the new theme
     */
    protected ThemeDescriptor readTheme(IConfigurationElement element) {
        ThemeDescriptor desc = null;

        String id = element.getAttribute(ThemeDescriptor.ATT_ID);
        if (id == null) {
			return null;
		}
        //see if the theme has already been created in another extension
        desc = (ThemeDescriptor) themeRegistry.findTheme(id);
        //if not, create it
        if (desc == null) {
            desc = new ThemeDescriptor(id);
            themeRegistry.add(desc);
        }
        //set the name as applicable
        desc.extractName(element);
    
        return desc;
    }

    /**
     * Read the theme extensions within a registry.
     * 
     * @param in the registry to read
     * @param out the registry to write to
     */
    public void readThemes(IExtensionRegistry in, ThemeRegistry out) {
        // this does not seem to really ever be throwing an the exception
        setRegistry(out);
        readRegistry(in, PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_THEMES);

        // support for old font definitions
        readRegistry(in, PlatformUI.PLUGIN_ID,
                IWorkbenchRegistryConstants.PL_FONT_DEFINITIONS);
    }

    /**
     * Set the output registry.
     * 
     * @param out the output registry
     */
    public void setRegistry(ThemeRegistry out) {
        themeRegistry = out;
    }
}
