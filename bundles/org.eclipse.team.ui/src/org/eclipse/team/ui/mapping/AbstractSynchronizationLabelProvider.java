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
package org.eclipse.team.ui.mapping;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.*;

/**
 * A label provider wrapper that adds synchronization image and/or text decorations
 * to the image and label obtained from the delegate provider.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public abstract class AbstractSynchronizationLabelProvider implements ILabelProvider {
	
	// Contains direction images
	private CompareConfiguration compareConfig = new CompareConfiguration();
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		Image base = getDelegateImage(element);
		if (isDecorationEnabled() && base != null) {
			return decorateImage(base, element);				
		}
		return base;
	}

	/**
	 * Decorate the image with the appropriate diff decorations.
	 * By default, this method uses the diff associated with
	 * the given element to determine how to decorate the image.
	 * It then uses the {@link CompareConfiguration#getImage(Image, int)}
	 * method to apply the decoration to the base image.
	 * @param base the base image to be decorated.
	 * @param element the element
	 * @return the image decorated appropriately using the diff associated with
	 * the element
	 * @see #getDiff(Object)
	 * @see CompareConfiguration#getImage(Image, int)
	 */
	protected Image decorateImage(Image base, Object element) {
		IDiffNode node = getDiff(element);
		Image decoratedImage;
		decoratedImage = getCompareImage(base, node);				
		// The reason we still overlay the compare image is to
		// ensure that the image width for all images shown in the viewer
		// are consistent.
		return decoratedImage;
	}

	/**
	 * Return the image for the item from the delegate label provider.
	 * @param element the element
	 * @return the image for the item from the delegate label provider
	 */
	protected Image getDelegateImage(Object element) {
		ILabelProvider modelLabelProvider = getDelegateLabelProvider();
		Image base = modelLabelProvider.getImage(element);
		return base;
	}

	private Image getCompareImage(Image base, IDiffNode node) {
		int compareKind = 0;
		if (node != null) {
			switch (node.getKind()) {
			case IDiffNode.ADD:
				compareKind = Differencer.ADDITION;
				break;
			case IDiffNode.REMOVE:
				compareKind = Differencer.DELETION;
				break;
			case IDiffNode.CHANGE:
				compareKind = Differencer.CHANGE;
				break;
			}
			if (node instanceof IThreeWayDiff) {
				IThreeWayDiff twd = (IThreeWayDiff) node;			
				switch (twd.getDirection()) {
				case IThreeWayDiff.OUTGOING :
					compareKind |= Differencer.RIGHT;
					break;
				case IThreeWayDiff.INCOMING :
					compareKind |= Differencer.LEFT;
					break;
				case IThreeWayDiff.CONFLICTING :
					compareKind |= Differencer.LEFT;
					compareKind |= Differencer.RIGHT;
					break;
				}
			}
		}	
		return compareConfig.getImage(base, compareKind);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		String base = getDelegateText(element);
		if (isSyncInfoInTextEnabled()) {
			return decorateText(base, element);
		}
		return base;
	}

	/**
	 * Obtain the text for the object from the delegate label provider.
	 * @param element the element
	 * @return the text label for the element
	 */
	protected String getDelegateText(Object element) {
		ILabelProvider modelLabelProvider = getDelegateLabelProvider();
		String base = modelLabelProvider.getText(element);
		if (base == null || base.length() == 0) {
			if (element instanceof ModelProvider) {
				ModelProvider provider = (ModelProvider) element;
				base = provider.getDescriptor().getLabel();
			}
		}
		return base;
	}
	
	/**
	 * Decorate the text with the appropriate diff decorations.
	 * By default, this method uses the diff associated with
	 * the given element to determine how to decorate the text.
	 * @param base the base text to be decorated.
	 * @param element the element
	 * @return the text decorated appropriately using the diff associated with
	 * the element
	 * @see #getDiff(Object)
	 */
	protected String decorateText(String base, Object element) {
		IDiffNode node = getDiff(element);
		if (node != null && node.getKind() != IDiffNode.NO_CHANGE) {			
			String syncKindString = node.toDiffString();
			return NLS.bind(TeamUIMessages.AbstractSynchronizationLabelProvider_0, new String[] { base, syncKindString }); 
		}
		return base;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		getDelegateLabelProvider().addListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		compareConfig.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return getDelegateLabelProvider().isLabelProperty(element, property);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		getDelegateLabelProvider().removeListener(listener);
	}
	
	/**
	 * Returns whether the synchronization state should be included in the
	 * text of the label. By default, the Team preference is used to determine
	 * what to return. Subclasses may override.
	 * @return whether the synchronization state should be included in the
	 * text of the label
	 */
	protected boolean isSyncInfoInTextEnabled() {
		return isDecorationEnabled() && TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_VIEW_SYNCINFO_IN_LABEL);
	}
	
	/**
	 * Return the label provider that will return the text and image 
	 * appropriate for the given model element. Subclasses are responsible for
	 * disposing of the label provider.
	 * @return the label provider that will return the text and image 
	 * appropriate for the given model element
	 */
	protected abstract ILabelProvider getDelegateLabelProvider();
	
	/**
	 * Return whether the label provider should decorate with the synchronization state.
	 * @return whether the label provider should decorate with the synchronization state
	 */
	protected abstract boolean isDecorationEnabled();
	
	/**
	 * Return the sync kind of the given element. This is used
	 * to determine how to decorate the image and label of the
	 * element. The sync kind is described in the {@link SyncInfo}
	 * class. A <code>null</code> is returned by default.
	 * @param element the element being tested
	 * @return the sync kind of the given element
	 */
	protected IDiffNode getDiff(Object element) {
		return null;
	}
	
}
