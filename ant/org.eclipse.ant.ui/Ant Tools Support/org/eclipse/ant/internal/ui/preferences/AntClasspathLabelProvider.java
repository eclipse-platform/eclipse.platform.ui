/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.preferences;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
import org.eclipse.ant.core.IAntClasspathEntry;
import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Label provider for classpath elements
 */
public class AntClasspathLabelProvider implements ILabelProvider, IColorProvider {


	private AntClasspathBlock fBlock;
	
	public AntClasspathLabelProvider(AntClasspathBlock block) {
		fBlock= block;
	}

	private Image getFolderImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
	}

	private Image getJarImage() {
		return AntUIImages.getImage(IAntUIConstants.IMG_JAR_FILE);
	}

	public Image getClasspathImage() {
		return AntUIImages.getImage(IAntUIConstants.IMG_TAB_CLASSPATH);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		String file;
		if (element instanceof ClasspathEntry) {
			ClasspathEntry entry = (ClasspathEntry) element;
			file= entry.toString();
			if (file.endsWith("/")) { //$NON-NLS-1$
				return getFolderImage();
			} 
			return getJarImage();
		}
		
		return getClasspathImage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof IAntClasspathEntry) {
			StringBuffer label= new StringBuffer(((IAntClasspathEntry)element).getLabel());
			if (element instanceof GlobalClasspathEntries && ((GlobalClasspathEntries)element).getType() == ClasspathModel.ANT_HOME) {
				AntCorePreferences prefs= AntCorePlugin.getPlugin().getPreferences();
				String defaultAntHome= prefs.getDefaultAntHome();
				String currentAntHome= fBlock.getAntHome();
				label.append(" ("); //$NON-NLS-1$
				if (defaultAntHome.equals(currentAntHome)) {
					label.append(AntPreferencesMessages.getString("AntClasspathLabelProvider.0")); //$NON-NLS-1$
				} else {
					label.append(fBlock.getAntHome());	
				}
				label.append(')');
			}
			return label.toString();
		}
		return element.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		if (element instanceof GlobalClasspathEntries) {
			Display display= Display.getCurrent();
			return display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);		
		} else if (element instanceof ClasspathEntry) {
			
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}
}
