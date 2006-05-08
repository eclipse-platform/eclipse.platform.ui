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
package org.eclipse.ui.internal.about;

import java.io.IOException;
import java.net.URL;
import com.ibm.icu.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * An abstract parent that describes data that can be displayed in a table in one of
 * the about dialogs.
 * @since 3.0 
 */
public abstract class AboutData {
    private String providerName;

    private String name;

    private String version;

    private String id;

    private String versionedId = null;

    protected AboutData(String providerName, String name, String version,
            String id) {
        this.providerName = providerName == null ? "" : providerName; //$NON-NLS-1$
        this.name = name == null ? "" : name; //$NON-NLS-1$
        this.version = version == null ? "" : version; //$NON-NLS-1$
        this.id = id == null ? "" : id; //$NON-NLS-1$
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getVersion() {
        return version;
    }

    public String getVersionedId() {
        if (versionedId == null) {
			versionedId = getId() + "_" + getVersion(); //$NON-NLS-1$
		}
        return versionedId;
    }

    /**
     * Modify the argument array to reverse the sort order. 
     * @param infos
     */
    private static void reverse(AboutData[] infos) {
        List infoList = Arrays.asList(infos);
        Collections.reverse(infoList);
        for (int i = 0; i < infos.length; ++i) {
			infos[i] = (AboutData) infoList.get(i);
		}
    }

    /**
     * Modify the argument array to be sorted by provider. If the reverse
     * boolean is true, the array is assumed to already be sorted and the
     * direction of sort (ascending vs. descending) is reversed.  Entries
     * with the same name are sorted by name.
     * 
     * @param reverse
     *            if true then the order of the argument is reversed without
     *            examining the value of the fields
     * @param infos
     *            the data to be sorted
     */
    public static void sortByProvider(boolean reverse, AboutData[] infos) {
        if (reverse) {
            reverse(infos);
            return;
        }

        Arrays.sort(infos, new Comparator() {
            Collator collator = Collator.getInstance(Locale.getDefault());

            public int compare(Object a, Object b) {
                AboutData info1 = (AboutData) a;
                AboutData info2 = (AboutData) b;

                String provider1 = info1.getProviderName();
                String provider2 = info2.getProviderName();

                if (!provider1.equals(provider2)) {
					return collator.compare(provider1, provider2);
				}

                return collator.compare(info1.getName(), info2.getName());
            }
        });
    }

    /**
     * Modify the argument array to be sorted by name. If the reverse
     * boolean is true, the array is assumed to already be sorted and the
     * direction of sort (ascending vs. descending) is reversed.
     * 
     * @param reverse
     *            if true then the order of the argument is reversed without
     *            examining the value of the fields
     * @param infos
     *            the data to be sorted
     */
    public static void sortByName(boolean reverse, AboutData[] infos) {
        if (reverse) {
            reverse(infos);
            return;
        }

        Arrays.sort(infos, new Comparator() {
            Collator collator = Collator.getInstance(Locale.getDefault());

            public int compare(Object a, Object b) {
                AboutData info1 = (AboutData) a;
                AboutData info2 = (AboutData) b;
                return collator.compare(info1.getName(), info2.getName());
            }
        });
    }

    /**
     * Modify the argument array to be sorted by version. If the reverse
     * boolean is true, the array is assumed to already be sorted and the
     * direction of sort (ascending vs. descending) is reversed.  Entries
     * with the same name are sorted by name.
     * 
     * @param reverse
     *            if true then the order of the argument is reversed without
     *            examining the value of the fields
     * @param infos
     *            the data to be sorted
     */
    public static void sortByVersion(boolean reverse, AboutData[] infos) {
        if (reverse) {
            reverse(infos);
            return;
        }

        Arrays.sort(infos, new Comparator() {
            Collator collator = Collator.getInstance(Locale.getDefault());

            public int compare(Object a, Object b) {
                AboutData info1 = (AboutData) a;
                AboutData info2 = (AboutData) b;

                String version1 = info1.getVersion();
                String version2 = info2.getVersion();

                if (!version1.equals(version2)) {
					return collator.compare(version1, version2);
				}

                return collator.compare(info1.getName(), info2.getName());
            }
        });
    }

    /**
     * Modify the argument array to be sorted by id. If the reverse
     * boolean is true, the array is assumed to already be sorted and the
     * direction of sort (ascending vs. descending) is reversed.  Entries
     * with the same name are sorted by name.
     * 
     * @param reverse
     *            if true then the order of the argument is reversed without
     *            examining the value of the fields
     * @param infos
     *            the data to be sorted
     */
    public static void sortById(boolean reverse, AboutData[] infos) {
        if (reverse) {
            reverse(infos);
            return;
        }

        Arrays.sort(infos, new Comparator() {
            Collator collator = Collator.getInstance(Locale.getDefault());

            public int compare(Object a, Object b) {
                AboutData info1 = (AboutData) a;
                AboutData info2 = (AboutData) b;

                String id1 = info1.getId();
                String id2 = info2.getId();

                if (!id1.equals(id2)) {
					return collator.compare(id1, id2);
				}

                return collator.compare(info1.getName(), info2.getName());
            }
        });
    }

    protected static URL getURL(String value) {
        try {
            if (value != null) {
				return new URL(value);
			}
        } catch (IOException e) {
            // do nothing
        }

        return null;
    }

    protected static ImageDescriptor getImage(URL url) {
        return url == null ? null : ImageDescriptor.createFromURL(url);
    }

    protected static ImageDescriptor getImage(String value) {
        return getImage(getURL(value));
    }
}
