/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Anton Leherbauer, Wind River - bug 146788
 *     rob.stryker@jboss.com - bug 243824 [CommonNavigator] lacks table / tree-table support
 * 
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IDescriptionProvider;

/**
 * @since 3.2
 */
public class SafeDelegateCommonLabelProvider implements ICommonLabelProvider, IColorProvider, IFontProvider, ITreePathLabelProvider, ITableLabelProvider, IStyledLabelProvider {

	private final ILabelProvider delegateLabelProvider;

	/**
	 * @param aLabelProvider
	 *            A non-null label provider.
	 * 
	 */
	public SafeDelegateCommonLabelProvider(ILabelProvider aLabelProvider) {
		super();
		delegateLabelProvider = aLabelProvider;
	}

	/**
	 * <p>
	 * No-op.
	 * </p>
	 * 
	 * @see org.eclipse.ui.navigator.ICommonLabelProvider#init(ICommonContentExtensionSite)
	 */
	public void init(ICommonContentExtensionSite aConfig) {
	}

	/**
	 * <p>
	 * If the delegate label provider implements <code>IDescriptionProvider</code>,
	 * it is used to retrieve the status bar message. 
	 * </p>
	 * <p>
	 * Returns <b>null </b> otherwise, forcing the CommonNavigator to provide the default
	 * message.
	 * </p>
	 * 
	 * @see org.eclipse.ui.navigator.ICommonLabelProvider#getDescription(java.lang.Object)
	 */
	public String getDescription(Object element) {
		/* The following few lines were contributed as part of a patch. */
		if (delegateLabelProvider instanceof IDescriptionProvider) {
			IDescriptionProvider provider = (IDescriptionProvider) delegateLabelProvider;
			return provider.getDescription(element);
		}
		return null;
	}

	/**
	 * @param listener
	 */
	public void addListener(ILabelProviderListener listener) {
		delegateLabelProvider.addListener(listener);
	}

	/**
	 * 
	 */
	public void dispose() {
		delegateLabelProvider.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return delegateLabelProvider.equals(obj);
	}

	public Image getImage(Object element) {
		return delegateLabelProvider.getImage(element);
	}

	public String getText(Object element) {
		return delegateLabelProvider.getText(element);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider#getStyledText(java.lang.Object)
	 */
	public StyledString getStyledText(Object element) {
		if (delegateLabelProvider instanceof IStyledLabelProvider) {
			return ((IStyledLabelProvider)delegateLabelProvider).getStyledText(element);
		}
		String text= getText(element);
		if (text == null)
			text= ""; //$NON-NLS-1$
		return new StyledString(text);
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		if (delegateLabelProvider instanceof ITableLabelProvider) {
			return ((ITableLabelProvider)delegateLabelProvider).getColumnImage(element, columnIndex);
		}
		return getImage(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (delegateLabelProvider instanceof ITableLabelProvider) {
			return ((ITableLabelProvider)delegateLabelProvider).getColumnText(element, columnIndex);
		}
		return getText(element);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return delegateLabelProvider.hashCode();
	}

	public boolean isLabelProperty(Object element, String property) {
		return delegateLabelProvider.isLabelProperty(element, property);
	}

	/**
	 * @param listener
	 */
	public void removeListener(ILabelProviderListener listener) {
		delegateLabelProvider.removeListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return delegateLabelProvider.toString();
	}

	public void restoreState(IMemento aMemento) {

	}

	public void saveState(IMemento aMemento) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreePathLabelProvider#updateLabel(org.eclipse.jface.viewers.ViewerLabel, org.eclipse.jface.viewers.TreePath)
	 */
	public void updateLabel(ViewerLabel label, TreePath elementPath) {
		if (delegateLabelProvider instanceof ITreePathLabelProvider) {
			ITreePathLabelProvider tplp = (ITreePathLabelProvider) delegateLabelProvider;
			String text = label.getText() != null ? label.getText() : ""; //$NON-NLS-1$
			Image image = label.getImage();
			tplp.updateLabel(label, elementPath);
			if(label.getText() == null)
				label.setText(text);
			if(label.getImage() == null && image != null)
				label.setImage(image);
		} else {
			Image image = getImage(elementPath.getLastSegment());
			if(image != null)
				label.setImage(image);
			String text = getText(elementPath.getLastSegment());
			if(text != null)
				label.setText(text);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		if(delegateLabelProvider instanceof IColorProvider) {
			return ((IColorProvider)delegateLabelProvider).getForeground(element);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		if(delegateLabelProvider instanceof IColorProvider) {
			return ((IColorProvider)delegateLabelProvider).getBackground(element);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
	 */
	public Font getFont(Object element) {
		if(delegateLabelProvider instanceof IFontProvider) {
			return ((IFontProvider)delegateLabelProvider).getFont(element);
		}
		return null;
	}
}
