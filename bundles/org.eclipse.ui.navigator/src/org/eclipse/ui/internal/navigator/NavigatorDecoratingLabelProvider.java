/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     rob.stryker@jboss.com - bug 243824 [CommonNavigator] lacks table / tree-table support
 *******************************************************************************/
package org.eclipse.ui.internal.navigator;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;

/**
 * Decorating label provider with styled label support for the navigator.
 * <p>
 * If the wrapped label provider is a {@link DelegatingStyledCellLabelProvider.IStyledLabelProvider} it will use
 * the styled label it provides.
 * </p>
 * <p>The label provider listens to the built-in decoration colors ({@link JFacePreferences#QUALIFIER_COLOR}, 
 * {@link JFacePreferences#COUNTER_COLOR} and {@link JFacePreferences#DECORATIONS_COLOR}. If other
 * colors are used, it is the responsibility of the wrapped label provider to fire the refresh.
 * </p>
 */
public class NavigatorDecoratingLabelProvider extends DecoratingStyledCellLabelProvider implements IPropertyChangeListener, ILabelProvider, ITableLabelProvider {

	private static class StyledLabelProviderAdapter implements IStyledLabelProvider, ITableLabelProvider, IColorProvider, IFontProvider {

		private final ILabelProvider provider;

		public StyledLabelProviderAdapter(ILabelProvider provider) {
			this.provider= provider;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			return provider.getImage(element);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider#getStyledText(java.lang.Object)
		 */
		public StyledString getStyledText(Object element) {
			if (provider instanceof IStyledLabelProvider) {
				return ((IStyledLabelProvider) provider).getStyledText(element);
			}
			String text= provider.getText(element);
			if (text == null)
				text= ""; //$NON-NLS-1$
			return new StyledString(text);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
			provider.addListener(listener);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
			provider.dispose();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return provider.isLabelProperty(element, property);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
			provider.removeListener(listener);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
		 */
		public Color getBackground(Object element) {
			if (provider instanceof IColorProvider) {
				return ((IColorProvider) provider).getBackground(element);
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
		 */
		public Color getForeground(Object element) {
			if (provider instanceof IColorProvider) {
				return ((IColorProvider) provider).getForeground(element);
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
		 */
		public Font getFont(Object element) {
			if (provider instanceof IFontProvider) {
				return ((IFontProvider) provider).getFont(element);
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			if (provider instanceof ITableLabelProvider) {
				return ((ITableLabelProvider) provider).getColumnImage(element, columnIndex);
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			if (provider instanceof ITableLabelProvider) {
				return ((ITableLabelProvider) provider).getColumnText(element, columnIndex);
			}
			return null;
		}	
	}
	
	/**
	 * Creates a {@link NavigatorDecoratingLabelProvider}
	 * 
	 * @param commonLabelProvider the label provider to use
	 */
	public NavigatorDecoratingLabelProvider(ILabelProvider commonLabelProvider) {
		super(new StyledLabelProviderAdapter(commonLabelProvider), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator(), null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StyledCellLabelProvider#initialize(org.eclipse.jface.viewers.ColumnViewer, org.eclipse.jface.viewers.ViewerColumn)
	 */
	public void initialize(ColumnViewer viewer, ViewerColumn column) {
		PlatformUI.getPreferenceStore().addPropertyChangeListener(this);
		JFaceResources.getColorRegistry().addListener(this);
		
		setOwnerDrawEnabled(showColoredLabels());
		
		super.initialize(viewer, column);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		PlatformUI.getPreferenceStore().removePropertyChangeListener(this);
		JFaceResources.getColorRegistry().removeListener(this);
	}
	
	private void refresh() {
		ColumnViewer viewer= getViewer();
		if (viewer == null) {
			return;
		}
		boolean showColoredLabels= showColoredLabels();
		if (showColoredLabels != isOwnerDrawEnabled()) {
			setOwnerDrawEnabled(showColoredLabels);
			viewer.refresh();
		} else if (showColoredLabels) {
			viewer.refresh();
		}
	}
	
	private static boolean showColoredLabels() {
		return PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property= event.getProperty();
		if (property.equals(JFacePreferences.QUALIFIER_COLOR) || property.equals(JFacePreferences.COUNTER_COLOR) || property.equals(JFacePreferences.DECORATIONS_COLOR)
				|| property.equals(IWorkbenchPreferenceConstants.USE_COLORED_LABELS)) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					refresh();
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		return getStyledText(element).getString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return ((StyledLabelProviderAdapter)getStyledStringProvider()).getColumnImage(element, columnIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		return ((StyledLabelProviderAdapter)getStyledStringProvider()).getColumnText(element, columnIndex);
	}
}
