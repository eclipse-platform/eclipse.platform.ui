/*
 * Created on Jan 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.internal.registry;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.preferences.PreferenceTransferElement;

/**
 * Preference Transfer registry reader to read extenders of the preferenceTranser schema.
 * 
 * @since 3.1
 */
public class PreferenceTransferRegistryReader extends RegistryReader {
    private List preferenceTransfers;

    private String pluginPoint;

    /**
     * Comment for <code>TAG_TRANSFER</code>
     */
    public final static String TAG_TRANSFER = "transfer";//$NON-NLS-1$

    /**
     * <code>ATT_NAME</code> the name of the transfer
     */
    public final static String ATT_NAME = "name";//$NON-NLS-1$

    /**
     * <code>ATT_CLASS</code> an optional class for the transfer, must implement <code>IPreferenceTransfer</code>
     */
    public final static String ATT_CLASS = "class";//$NON-NLS-1$

    /**
     * <code>ATT_ICON</code> an optional icon used when displaying the transfer
     */
    public final static String ATT_ICON = "icon";//$NON-NLS-1$

    /**
     * <code>ATT_ID</code> the id for the transfer
     */
    public final static String ATT_ID = "id";//$NON-NLS-1$

    private static final String TAG_MAPPING = "mapping"; //$NON-NLS-1$
    private static final String TAG_ENTRY = "entry"; //$NON-NLS-1$
    
    private static final String ATT_SCOPE = "scope"; //$NON-NLS-1$
    private static final String ATT_NODE = "nodes"; //$NON-NLS-1$
    private static final String ATT_KEYS = "keys"; //$NON-NLS-1$
 
    /**
     *  Create an instance of this class.
     *
     *  @param pluginPointId java.lang.String
     */
    public PreferenceTransferRegistryReader(String pluginPointId) {
        pluginPoint = pluginPointId;
    }

    /**
     * Adds new wizard to the provided collection. Override to
     * provide more logic.
     */
    protected void addNewElementToResult(PreferenceTransferElement element,
            IConfigurationElement config) {
        preferenceTransfers.add(element);
    }


    /**
     * Returns a new PreferenceTransferElement configured according to the parameters
     * contained in the passed Registry.  
     *
     * May answer null if there was not enough information in the Extension to create 
     * an adequate wizard
     */
    protected PreferenceTransferElement createPreferenceTransferElement(
            IConfigurationElement element) {
        // PreferenceTransfers must have a name and class attribute
        if (element.getAttribute(ATT_NAME) == null) {
            logMissingAttribute(element, ATT_NAME);
            return null;
        }
        
        // must specifiy a mapping OR class
        if (element.getChildren(TAG_MAPPING) == null && element.getAttribute(ATT_CLASS) == null) {
            logMissingElement(element, TAG_MAPPING);
            return null;
        }
        
        
        return new PreferenceTransferElement(element);
    }

    /**
     * Returns a sorted list of preference transfers.
     *
     *@return an array of <code>IPreferenceTransfer</code> objects
     */
    public PreferenceTransferElement[] getPreferenceTransfers() {
        readPreferenceTransfers();
        PreferenceTransferElement[] transfers = new PreferenceTransferElement[preferenceTransfers.size()];
        Collections.sort(preferenceTransfers, new Comparator() {
            public int compare(Object o1, Object o2) {
                String name1 = ((PreferenceTransferElement)o1).getName();
                String name2 = ((PreferenceTransferElement)o2).getName();
                              
                return Collator.getInstance().compare(name1, name2);
            }});
        preferenceTransfers.toArray(transfers);
        return transfers;
    }

    /**
     * Implement this method to read element attributes.
     */
    public boolean readElement(IConfigurationElement element) {
        if (!element.getName().equals(TAG_TRANSFER))
            return false;
        PreferenceTransferElement transfer = createPreferenceTransferElement(element);
        if (transfer != null)
            addNewElementToResult(transfer, element);
        return true;
    }

    /**
     * Reads the wizards in a registry.  
     */
    protected void readPreferenceTransfers() {
        preferenceTransfers = new ArrayList();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        readRegistry(registry, WorkbenchPlugin.PI_WORKBENCH, pluginPoint);
    }
    


   /**
     * @param configElement
     * @return the child configuration elements
     */
    public static IConfigurationElement[] getMappings(IConfigurationElement configElement) {
        IConfigurationElement[] children = configElement.getChildren(TAG_MAPPING);
        if (children.length < 1) {
            logMissingElement(configElement, TAG_MAPPING);
            return null;
        } 
        return children;
    }

    /**
     * @param element
     * @return the scope attribute for this element
     */
    public static String getScope(IConfigurationElement element) {
        return element.getAttribute(ATT_SCOPE);
    }

    /**
     * @param element
     * @return the maps mapping nodes to keys for this element
     */
    public static Map getEntry(IConfigurationElement element) {
        IConfigurationElement[] entries = element.getChildren(TAG_ENTRY);
        if (entries.length == 0)
            return null;
        Map map = new HashMap(entries.length);
        for (int i = 0; i < entries.length; i++) {
            IConfigurationElement entry = entries[i];
            map.put(entry.getAttribute(ATT_NODE), entry.getAttribute(ATT_KEYS));            
        }
        return map;
    }

}