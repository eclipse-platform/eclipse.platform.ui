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
package org.eclipse.team.ui.synchronize;

import java.util.*;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.ISharedImages;

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
public abstract class AbstractSynchronizeLabelProvider implements ILabelProvider {
	
	// Cache for images that have been overlayed
	private Map fgImageCache;
	
	// Font used to display busy elements
	private Font busyFont;
	
	// Contains direction images
	private CompareConfiguration compareConfig = new CompareConfiguration();
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		Image base = getDelegateImage(element);
		if (isDecorationEnabled() && base != null) {
			Image decorateImage = decorateImage(base, element);
			base = decorateImage;				
		}
		if (isIncludeOverlays() && base != null) {
			base = addOverlays(base, element);
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
		if(busyFont != null) {
			busyFont.dispose();
		}
		if (fgImageCache != null) {
			Iterator it = fgImageCache.values().iterator();
			while (it.hasNext()) {
				Image element = (Image) it.next();
				element.dispose();
			}
		}
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
	
	private Image addOverlays(Image base, Object element) {
		if (!isIncludeOverlays())
			return base;
		// if the folder is already conflicting then don't bother propagating
		// the conflict
		List overlays = new ArrayList();
		List locations = new ArrayList();
		
		// Decorate with the busy indicator
		if (isBusy(element)) {
			overlays.add(TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_HOURGLASS_OVR));
			locations.add(new Integer(OverlayIcon.TOP_LEFT));
		}
		// Decorate with propagated conflicts and problem markers
		if (!isConflicting(element)) {
			if (hasDecendantConflicts(element)) {
				overlays.add(TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_CONFLICT_OVR));
				locations.add(new Integer(OverlayIcon.BOTTOM_RIGHT));
			}
		}
		int severity = getMarkerSeverity(element);
		if (severity == IMarker.SEVERITY_ERROR) {
			overlays.add(TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_ERROR_OVR));
			locations.add(new Integer(OverlayIcon.BOTTOM_LEFT));
		} else if (severity == IMarker.SEVERITY_WARNING) {
			overlays.add(TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_WARNING_OVR));
			locations.add(new Integer(OverlayIcon.BOTTOM_LEFT));
		}
		if (!overlays.isEmpty()) {
			ImageDescriptor[] overlayImages = (ImageDescriptor[]) overlays.toArray(new ImageDescriptor[overlays.size()]);
			int[] locationInts = new int[locations.size()];
			for (int i = 0; i < locations.size(); i++) {
				locationInts[i] = ((Integer) locations.get(i)).intValue();
			}
			ImageDescriptor overlay = new OverlayIcon(base, overlayImages, locationInts, new Point(base.getBounds().width, base.getBounds().height));
			if (fgImageCache == null) {
				fgImageCache = new HashMap(10);
			}
			Image conflictDecoratedImage = (Image) fgImageCache.get(overlay);
			if (conflictDecoratedImage == null) {
				conflictDecoratedImage = overlay.createImage();
				fgImageCache.put(overlay, conflictDecoratedImage);
			}
			return conflictDecoratedImage;
		}
		return base;
	}
	
	/**
	 * Indicate whether the overlays provided by this class should be applied.
	 * By default, <code>true</code> is returned. Subclasses may override
	 * and control individual overlays by overriding the appropriate
	 * query methods.
	 * @return hether the overlays provided by this class should be applied
	 */
	protected boolean isIncludeOverlays() {
		return true;
	}

	/**
	 * Return the marker severity (one of IMarker.SEVERITY_ERROR or
	 * IMarker.SEVERITY_WARNING) to be overlayed on the given element or -1 if
	 * there are no markers. By Default, the element is adapted to resource
	 * mapping in order to look for markers.
	 * <p>
	 * Although this class handles providing the label updates, it does not react 
	 * to marker changes. Subclasses must issue label updates when the markers on 
	 * a logical model element change.
	 * 
	 * @param element
	 *            the element
	 * @return the marker severity
	 */
	protected int getMarkerSeverity(Object element) {
		ResourceMapping mapping = Utils.getResourceMapping(element);
		int result = -1;
		if (mapping != null) {
			try {
				IMarker[] markers = mapping.findMarkers(IMarker.PROBLEM, true, null);
				for (int i = 0; i < markers.length; i++) {
					IMarker marker = markers[i];
					Integer severity = (Integer) marker.getAttribute(IMarker.SEVERITY);
					if (severity != null) {
						if (severity.intValue() == IMarker.SEVERITY_ERROR) {
							return IMarker.SEVERITY_ERROR;
						} else if (severity.intValue() == IMarker.SEVERITY_WARNING) {
							result = IMarker.SEVERITY_WARNING;
						}
					}
				}
			} catch (CoreException e) {
				// Ignore
			}
		}
		return result;
	}

	/**
	 * Return whether the given element has decendant conflicts.
	 * By defautl, <code>false</code> is returned. Subclasses 
	 * may override.
	 * @param element the element
	 * @return whether the given element has decendant conflicts
	 */
	protected boolean hasDecendantConflicts(Object element) {
		return false;
	}

	private boolean isConflicting(Object element) {
		IDiffNode node = getDiff(element);
		if (node != null) {
			if (node instanceof IThreeWayDiff) {
				IThreeWayDiff twd = (IThreeWayDiff) node;
				return twd.getDirection() == IThreeWayDiff.CONFLICTING;
			}
		}
		return false;
	}

	/**
	 * Return whether the given element is busy (i.e. is involved
	 * in an opertion. By default, <code>false</code> is returned.
	 * Subclasses may override.
	 * @param element the element
	 * @return hether the given element is busy
	 */
	protected boolean isBusy(Object element) {
		return false;
	}

	/**
	 * Method that provides a custom font for elements that are
	 * busy. Although this label provider does not implement
	 * {@link IFontProvider}, subclasses that wish to get 
	 * busy indication using a font can do so.
	 * @param element the element
	 * @return the font to indicate tahtthe element is busy
	 */
	public Font getFont(Object element) {
		if(isBusy(element)) {
			if (busyFont == null) {
				Font defaultFont = JFaceResources.getDefaultFont();
				FontData[] data = defaultFont.getFontData();
				for (int i = 0; i < data.length; i++) {
					data[i].setStyle(SWT.ITALIC);
				}				
				busyFont = new Font(TeamUIPlugin.getStandardDisplay(), data);
			}
			return busyFont;
		}
		return null;
	}
	
}
