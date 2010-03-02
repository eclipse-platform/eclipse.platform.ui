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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class used to associate BundleInfo's that have the same provider and
 * image.  The algorithm in <code>java.util.zip.CRC32</code> is used to determine
 * image identity.
 */
public class AboutFeaturesButtonManager {
    private Map providerMap = new HashMap();

    private static class Key {
        public String providerName;

        public Long crc;

        /**
         * @param crc must not be null
         */
        public Key(String providerName, Long crc) {
            this.providerName = providerName;
            this.crc = crc;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Key)) {
				return false;
			}
            Key other = (Key) o;
            if (!providerName.equals(other.providerName)) {
				return false;
			}
            return crc.equals(other.crc);
        }

        public int hashCode() {
            return providerName.hashCode();
        }
    }

    /**
     * @return true if a button should be added (i.e., the argument has an image
     *         and it does not already have a button)
     */
    public boolean add(AboutBundleGroupData info) {
        // no button for features without an image
        Long crc = info.getFeatureImageCrc();
        if (crc == null) {
			return false;
		}

        String providerName = info.getProviderName();
        Key key = new Key(providerName, crc);

        List infoList = (List) providerMap.get(key);
        if (infoList != null) {
            infoList.add(info);
            return false;
        }

        infoList = new ArrayList();
        infoList.add(info);
        providerMap.put(key, infoList);
        return true;
    }

    /**
     * Return an array of all bundle groups that share the argument's provider and
     * image.  Returns an empty array if there isn't any related information.
     */
    public AboutBundleGroupData[] getRelatedInfos(AboutBundleGroupData info) {
        // if there's no image, then there won't be a button
        Long crc = info.getFeatureImageCrc();
        if (crc == null) {
			return new AboutBundleGroupData[0];
		}

        String providerName = info.getProviderName();
        Key key = new Key(providerName, crc);

        List infoList = (List) providerMap.get(key);
        if (infoList == null) {
			return new AboutBundleGroupData[0];
		}

        return (AboutBundleGroupData[]) infoList
                .toArray(new AboutBundleGroupData[0]);
    }
}
