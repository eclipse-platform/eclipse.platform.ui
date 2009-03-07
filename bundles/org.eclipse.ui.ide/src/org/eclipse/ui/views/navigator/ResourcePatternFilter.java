/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * Filter used to determine whether resources are to be shown or not.
 * 
 * @since 2.0
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
public class ResourcePatternFilter extends ViewerFilter {
    private String[] patterns;

    private StringMatcher[] matchers;

    static final String COMMA_SEPARATOR = ",";//$NON-NLS-1$

    static final String FILTERS_TAG = "resourceFilters";//$NON-NLS-1$

    /**
     * Creates a new resource pattern filter.
     */
    public ResourcePatternFilter() {
        super();
    }

    /**
     * Return the currently configured StringMatchers. If there aren't any look
     * them up.
     */
    private StringMatcher[] getMatchers() {

        if (this.matchers == null) {
			initializeFromPreferences();
		}

        return this.matchers;
    }

    /**
     * Gets the patterns for the receiver. Returns the cached values if there
     * are any - if not look it up.
     */
    public String[] getPatterns() {

        if (this.patterns == null) {
			initializeFromPreferences();
		}

        return this.patterns;

    }

    /**
     * Initializes the filters from the preference store.
     */
    private void initializeFromPreferences() {
        // get the filters that were saved by ResourceNavigator.setFiltersPreference
        IPreferenceStore viewsPrefs = IDEWorkbenchPlugin.getDefault()
                .getPreferenceStore();
        String storedPatterns = viewsPrefs.getString(FILTERS_TAG);

        if (storedPatterns.length() == 0) {
            // try to migrate patterns from old workbench preference store location
            IPreferenceStore workbenchPrefs = PrefUtil.getInternalPreferenceStore();
            storedPatterns = workbenchPrefs.getString(FILTERS_TAG);
            if (storedPatterns.length() > 0) {
                viewsPrefs.setValue(FILTERS_TAG, storedPatterns);
                workbenchPrefs.setValue(FILTERS_TAG, ""); //$NON-NLS-1$
            }
        }

        if (storedPatterns.length() == 0) {
            // revert to all filter extensions with selected == "true"
            // if there are no filters in the preference store
            List defaultFilters = FiltersContentProvider.getDefaultFilters();
            String[] patterns = new String[defaultFilters.size()];
            defaultFilters.toArray(patterns);
            setPatterns(patterns);
            return;
        }

        //Get the strings separated by a comma and filter them from the currently
        //defined ones
        List definedFilters = FiltersContentProvider.getDefinedFilters();
        StringTokenizer entries = new StringTokenizer(storedPatterns,
                COMMA_SEPARATOR);
        List patterns = new ArrayList();

        while (entries.hasMoreElements()) {
            String nextToken = entries.nextToken();
            if (definedFilters.indexOf(nextToken) > -1) {
				patterns.add(nextToken);
			}
        }

        //Convert to an array of Strings
        String[] patternArray = new String[patterns.size()];
        patterns.toArray(patternArray);
        setPatterns(patternArray);

    }

    /* (non-Javadoc)
     * Method declared on ViewerFilter.
     */
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        IResource resource = null;
        if (element instanceof IResource) {
            resource = (IResource) element;
        } else if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            resource = (IResource) adaptable.getAdapter(IResource.class);
        }
        if (resource != null) {
            String name = resource.getName();
            StringMatcher[] testMatchers = getMatchers();
            for (int i = 0; i < testMatchers.length; i++) {
                if (testMatchers[i].match(name)) {
					return false;
				}
            }
            return true;
        }
        return true;
    }

    /**
     * Sets the patterns to filter out for the receiver.
     */
    public void setPatterns(String[] newPatterns) {

        this.patterns = newPatterns;
        this.matchers = new StringMatcher[newPatterns.length];
        for (int i = 0; i < newPatterns.length; i++) {
            //Reset the matchers to prevent constructor overhead
            matchers[i] = new StringMatcher(newPatterns[i], true, false);
        }
    }
}
