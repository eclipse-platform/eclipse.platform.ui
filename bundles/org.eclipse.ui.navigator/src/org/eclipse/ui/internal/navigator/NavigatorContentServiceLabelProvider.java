/*******************************************************************************
 * Copyright (c) 2003, 2015, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fair Issac Corp - bug 287103 - NCSLabelProvider does not properly handle overrides
 *     Stefan Winkler <stefan@winklerweb.net> - bug 178019 - CNF Tooltip support
 *******************************************************************************/
package org.eclipse.ui.internal.navigator;


import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.IToolTipProvider;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentExtension;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * <p>
 * Provides relevant labels based on the associated
 * {@link INavigatorContentService}for the contents of a
 * TreeViewer.
 * </p>
 *
 * <p>
 * Except for the dependency on
 * {@link INavigatorContentService}, this class has no
 * dependencies on the rest of the Common Navigator framework. Tree viewers that would like to use
 * the extensions defined by the Common Navigator, without using the actual view part or other
 * pieces of functionality (filters, sorting, etc) may choose to use this class, in effect using an
 * extensible label provider.
 * </p>
 *
 * @since 3.2
 *
 * @see org.eclipse.ui.internal.navigator.NavigatorContentService
 * @see org.eclipse.ui.internal.navigator.NavigatorContentServiceContentProvider
 */
public class NavigatorContentServiceLabelProvider extends EventManager
		implements ILabelProvider, IColorProvider, IFontProvider, ITreePathLabelProvider, ITableLabelProvider,
		ILabelProviderListener, IStyledLabelProvider, IToolTipProvider {

	private final NavigatorContentService contentService;
	private final boolean isContentServiceSelfManaged;
	private final ReusableViewerLabel reusableLabel = new ReusableViewerLabel();


	/**
	 * <p>
	 * Uses the supplied content service to acquire the available extensions.
	 * </p>
	 *
	 * @param aContentService
	 *            The associated NavigatorContentService that should be used to acquire information.
	 */
	public NavigatorContentServiceLabelProvider(NavigatorContentService aContentService) {
		contentService = aContentService;
		isContentServiceSelfManaged = false;
	}

	/**
	 * <p>
	 * Return the appropriate image for anElement. The image will be used as the icon for anElement
	 * when displayed in the tree viewer. This method uses information from its contentService to
	 * know what extensions to use to supply the correct label.
	 * </p>
	 * {@inheritDoc}
	 *
	 * @param anElement
	 *            An element from the Tree Viewer
	 * @return The Image that will be used as the icon when anElement is displayed in the viewer.
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object anElement) {
		return getColumnImage(anElement, -1);
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		Collection contentExtensions = contentService.findPossibleLabelExtensions(element);
		Image image = null;
		for (Iterator itr = contentExtensions.iterator(); itr.hasNext() && image == null; ) {
			image = findImage((NavigatorContentExtension) itr.next(), element, columnIndex);
		}
		return image;
	}

	/**
	 * <p>
	 * Return a String representation of anElement to be used as the display name in the tree
	 * viewer.
	 * </p>
	 *
	 * @param anElement
	 *            An element from the Tree Viewer
	 * @return The String label to display for the object when represented in the viewer.
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object anElement) {
		return getColumnText(anElement, -1);
	}

	@Override
	public String getColumnText(Object anElement, int aColumn) {
		ILabelProvider[] labelProviders = contentService.findRelevantLabelProviders(anElement);
		if (labelProviders.length == 0) {
			return NLS.bind(CommonNavigatorMessages.NavigatorContentServiceLabelProvider_Error_no_label_provider_for_0_, makeSmallString(anElement));
		}
		String text = null;
		for (ILabelProvider labelProvider : labelProviders) {
			if (labelProvider instanceof ITableLabelProvider && aColumn != -1) {
				text = ((ITableLabelProvider)labelProvider).getColumnText(anElement, aColumn);
			} else {
				text = labelProvider.getText(anElement);
			}
			if (text != null && text.length() > 0) {
				return text;
			}
		}
		return text;
	}

	@Override
	public StyledString getStyledText(Object anElement) {
		Collection extensions = contentService.findPossibleLabelExtensions(anElement);
		if (extensions.isEmpty()) {
			return new StyledString(NLS.bind(CommonNavigatorMessages.NavigatorContentServiceLabelProvider_Error_no_label_provider_for_0_, makeSmallString(anElement)));
		}

		StyledString text = null;
		for (Iterator itr = extensions.iterator(); itr.hasNext() && text == null; ) {
			text = findStyledText((NavigatorContentExtension) itr.next(), anElement);
		}
		return text != null ? text : new StyledString();
	}

	/**
	 * Search for a styled text label and take overrides into account.
	 * Uses only simple ITreeContentProvider.getParent() style semantics.
	 *
	 * @return the styled text or <code>null</code> if no extension has been found that provides a label
	 */
	private StyledString findStyledText(NavigatorContentExtension foundExtension, Object anElement) {
		ICommonLabelProvider labelProvider= foundExtension.getLabelProvider();
		if (labelProvider instanceof IStyledLabelProvider) {
			StyledString styledText= ((IStyledLabelProvider) labelProvider).getStyledText(anElement);
			// paranoia check for null, although null is not a valid return value for IStyledLabelProvider.getStyledText
			if (styledText != null && styledText.length() > 0) {
				return styledText;
			}
		} else {
			String text= labelProvider.getText(anElement);
			if (text != null && text.length() > 0) {
				return new StyledString(text);
			}
		}
		return null;
	}

	private String makeSmallString(Object obj) {
		if (obj == null) {
			return "null"; //$NON-NLS-1$
		}
		String str = obj.toString();
		int len = str.length();
		return str.substring(0, len < 50 ? len : 49);
	}

	/**
	 * Search for image and take overrides into account.
	 * Uses only simple ITreeContentProvider.getParent() style semantics.
	 */
	private Image findImage(NavigatorContentExtension foundExtension, Object anElement, int aColumn) {
		Image image = null;
		ICommonLabelProvider provider = foundExtension.getLabelProvider();
		if (provider instanceof ITableLabelProvider && aColumn >= 0) {
			image = ((ITableLabelProvider)provider).getColumnImage(anElement, aColumn);
		} else {
			image = provider.getImage(anElement);
		}

		return image;
	}

	@Override
	public Font getFont(Object anElement) {
		ILabelProvider[] labelProviders = contentService.findRelevantLabelProviders(anElement);
		for (ILabelProvider provider : labelProviders) {
			if (provider instanceof IFontProvider fontProvider) {
				Font font = fontProvider.getFont(anElement);
				if (font != null) {
					return font;
				}
			}
		}
		return null;
	}

	@Override
	public Color getForeground(Object anElement) {
		ILabelProvider[] labelProviders = contentService.findRelevantLabelProviders(anElement);
		for (ILabelProvider provider : labelProviders) {
			if (provider instanceof IColorProvider colorProvider) {
				Color color = colorProvider.getForeground(anElement);
				if (color != null) {
					return color;
				}
			}
		}
		return null;
	}

	@Override
	public Color getBackground(Object anElement) {
		ILabelProvider[] labelProviders = contentService.findRelevantLabelProviders(anElement);
		for (ILabelProvider provider : labelProviders) {
			if (provider instanceof IColorProvider colorProvider) {
				Color color = colorProvider.getBackground(anElement);
				if (color != null) {
					return color;
				}
			}
		}
		return null;
	}

	@Override
	public String getToolTipText(Object anElement) {
		ILabelProvider[] labelProviders = contentService.findRelevantLabelProviders(anElement);
		for (ILabelProvider provider : labelProviders) {
			if (provider instanceof IToolTipProvider tooltipProvider) {
				String tooltip = tooltipProvider.getToolTipText(anElement);
				if (tooltip != null && !tooltip.isEmpty()) {
					return tooltip;
				}
			}
		}
		return null;
	}

	/**
	 * <p>
	 * Indicates whether anElelement has aProperty that affects the display of the label.
	 * </p>
	 * {@inheritDoc}
	 *
	 * @param anElement
	 *            An element from the Tree Viewer
	 * @param aProperty
	 *            A property of the given element that could be a label provider
	 * @return True if any of the extensions enabled on anElement consider aProperty a
	 *         label-changing property.
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public boolean isLabelProperty(Object anElement, String aProperty) {
		boolean result = false;
		ILabelProvider[] labelProviders = contentService.findRelevantLabelProviders(anElement);
		for (int i = 0; i < labelProviders.length && !result; i++) {
			result = labelProviders[i].isLabelProperty(anElement, aProperty);
		}
		return result;
	}


	/**
	 * <p>
	 * Label provider listeners are currently supported.
	 * </p>
	 *
	 * {@inheritDoc}
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	@Override
	public void addListener(ILabelProviderListener aListener) {
		addListenerObject(aListener);
	}

	/**
	 * <p>
	 * Label provider listeners are currently supported.
	 * </p>
	 *
	 * {@inheritDoc}
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	@Override
	public void removeListener(ILabelProviderListener aListener) {
		removeListenerObject(aListener);
	}

	/**
	 * <p>
	 * Dispose of the content service, if it was created and not supplied.
	 * </p>
	 * <p>
	 * <b>If a client uses this class outside of the framework of {@link CommonViewer}, this method
	 * must be called when finished. </b>
	 * </p>
	 *
	 * {@inheritDoc}
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		if (isContentServiceSelfManaged) {
			contentService.dispose();
		}

	}

	/**
	 * Fires a label provider changed event to all registered listeners
	 * Only listeners registered at the time this method is called are notified.
	 *
	 * @param event a label provider changed event
	 *
	 * @see ILabelProviderListener#labelProviderChanged
	 */
	protected void fireLabelProviderChanged(
			final LabelProviderChangedEvent event) {
		for (Object listener : getListeners()) {
			final ILabelProviderListener labelProviderListener = (ILabelProviderListener) listener;
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() {
					labelProviderListener.labelProviderChanged(event);
				}
			});

		}
	}

	@Override
	public void updateLabel(ViewerLabel label, TreePath elementPath) {

		Collection contentExtensions = contentService.findPossibleLabelExtensions(elementPath.getLastSegment());
		reusableLabel.reset(label);
		for (Iterator itr = contentExtensions.iterator(); itr.hasNext() && !(reusableLabel.isValid() && reusableLabel.hasChanged()); ) {
			findUpdateLabel((NavigatorContentExtension)itr.next(), reusableLabel, elementPath);
		}
		reusableLabel.fill(label);
	}


	/**
	 * Search for text label and take overrides into account.
	 * Uses only simple ITreeContentProvider.getParent() style semantics.
	 */
	private void findUpdateLabel(NavigatorContentExtension foundExtension, ReusableViewerLabel label, TreePath elementPath) {

		ILabelProvider labelProvider = foundExtension.getLabelProvider();
		if (labelProvider instanceof ITreePathLabelProvider tplp) {
			tplp.updateLabel(label, elementPath);
		} else {
			label.setImage(labelProvider.getImage(elementPath.getLastSegment()));
			label.setText(labelProvider.getText(elementPath.getLastSegment()));
		}
	}

	@Override
	public void labelProviderChanged(LabelProviderChangedEvent event) {
		fireLabelProviderChanged(event);
	}



}
