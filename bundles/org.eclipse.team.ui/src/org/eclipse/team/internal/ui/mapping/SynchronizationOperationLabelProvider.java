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
package org.eclipse.team.internal.ui.mapping;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.jface.viewers.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.*;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.*;

public abstract class SynchronizationOperationLabelProvider extends LabelProvider {

	// Cache for folder images that have been overlayed with conflict icon
	private Map fgImageCache;
	
	// Contains direction images
	CompareConfiguration compareConfig = new CompareConfiguration();

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		LabelProvider modelLabelProvider = getModelLabelProvider();
		Image base = modelLabelProvider.getImage(element);
		if (base != null) {
			int kind = getSyncKind(element);
			Image decoratedImage;
			decoratedImage = getCompareImage(base, kind);				
			// The reason we still overlay the compare image is to
			// ensure that the image width for all images shown in the viewer
			// are consistent.
			return decoratedImage;				
		}
		return base;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		LabelProvider modelLabelProvider = getModelLabelProvider();
		String base = modelLabelProvider.getText(element);
		if (isSyncInfoInTextEnabled()) {
			int kind = getSyncKind(element);
			if (kind != SyncInfo.IN_SYNC) {
				String syncKindString = SyncInfo.kindToString(kind);
				return NLS.bind(TeamUIMessages.TeamSubscriberSyncPage_labelWithSyncKind, new String[] { base, syncKindString }); // 
			}
		}
		return base;
	}

	/**
	 * Returns whether the synchronization state should be included in the
	 * text of the label. By default, the Team preference is used to determine
	 * what to return.
	 * @return whether the synchronization state should be included in the
	 * text of the label
	 */
	protected boolean isSyncInfoInTextEnabled() {
		return TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_VIEW_SYNCINFO_IN_LABEL);
	}
	
	private Image getCompareImage(Image base, int kind) {
		switch (kind & SyncInfo.DIRECTION_MASK) {
			case SyncInfo.OUTGOING :
				kind = (kind & ~SyncInfo.OUTGOING) | SyncInfo.INCOMING;
				break;
			case SyncInfo.INCOMING :
				kind = (kind & ~SyncInfo.INCOMING) | SyncInfo.OUTGOING;
				break;
		}
		return compareConfig.getImage(base, kind);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		compareConfig.dispose();
		if (fgImageCache != null) {
			Iterator it = fgImageCache.values().iterator();
			while (it.hasNext()) {
				Image element = (Image) it.next();
				element.dispose();
			}
		}
	}
	
	/**
	 * Return the label provider that will return the text and image 
	 * appropriate for the given model element. Subclasses are responsible for
	 * disposing of the label provider.
	 * @return the label provider that will return the text and image 
	 * appropriate for the given model element
	 */
	protected abstract LabelProvider getModelLabelProvider();
	
	/**
	 * Return the sync kind of the given element. This is used
	 * to determine how to decorate the image and label of the
	 * element.
	 * @param element the element being tested
	 * @return the sync kind of the given element
	 */
	protected abstract int getSyncKind(Object element);

}
