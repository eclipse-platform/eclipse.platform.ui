/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import java.util.Set;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

public class ThemeManager implements IThemeManager {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.themes.IThemeManager#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		// FIXME compat addPropertyChangeListener
		E4Util.unsupported("addPropertyChangeListener"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.themes.IThemeManager#getCurrentTheme()
	 */
	public ITheme getCurrentTheme() {
		// FIXME compat getCurrentTheme
		E4Util.unsupported("getCurrentTheme"); //$NON-NLS-1$
		return new Theme();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.themes.IThemeManager#getTheme(java.lang.String)
	 */
	public ITheme getTheme(String id) {
		// FIXME compat getTheme
		E4Util.unsupported("getTheme"); //$NON-NLS-1$
		return getCurrentTheme();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.themes.IThemeManager#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		// FIXME compat removePropertyChangeListener
		E4Util.unsupported("removePropertyChangeListener"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.themes.IThemeManager#setCurrentTheme(java.lang.String)
	 */
	public void setCurrentTheme(String id) {
		// FIXME compat setCurrentTheme
		E4Util.unsupported("setCurrentTheme"); //$NON-NLS-1$
	}

	class Theme implements ITheme {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ui.themes.ITheme#addPropertyChangeListener(org.eclipse
		 * .jface.util.IPropertyChangeListener)
		 */
		public void addPropertyChangeListener(IPropertyChangeListener listener) {
			// FIXME compat addPropertyChangeListener
			E4Util.unsupported("addPropertyChangeListener"); //$NON-NLS-1$
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.themes.ITheme#dispose()
		 */
		public void dispose() {
			// FIXME compat dispose
			E4Util.unsupported("dispose"); //$NON-NLS-1$
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.themes.ITheme#getBoolean(java.lang.String)
		 */
		public boolean getBoolean(String key) {
			// FIXME compat getBoolean
			E4Util.unsupported("getBoolean"); //$NON-NLS-1$
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.themes.ITheme#getColorRegistry()
		 */
		public ColorRegistry getColorRegistry() {
			// FIXME compat getColorRegistry
			E4Util.unsupported("getColorRegistry"); //$NON-NLS-1$
			return JFaceResources.getColorRegistry();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.themes.ITheme#getFontRegistry()
		 */
		public FontRegistry getFontRegistry() {
			// FIXME compat getFontRegistry
			E4Util.unsupported("getFontRegistry"); //$NON-NLS-1$
			return JFaceResources.getFontRegistry();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.themes.ITheme#getId()
		 */
		public String getId() {
			// FIXME compat getId
			E4Util.unsupported("getId"); //$NON-NLS-1$
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.themes.ITheme#getInt(java.lang.String)
		 */
		public int getInt(String key) {
			// FIXME compat getInt
			E4Util.unsupported("getInt"); //$NON-NLS-1$
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.themes.ITheme#getLabel()
		 */
		public String getLabel() {
			// FIXME compat getLabel
			E4Util.unsupported("getLabel"); //$NON-NLS-1$
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.themes.ITheme#getString(java.lang.String)
		 */
		public String getString(String key) {
			// FIXME compat getString
			E4Util.unsupported("getString"); //$NON-NLS-1$
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.themes.ITheme#keySet()
		 */
		public Set keySet() {
			// FIXME compat keySet
			E4Util.unsupported("keySet"); //$NON-NLS-1$
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ui.themes.ITheme#removePropertyChangeListener(org.eclipse
		 * .jface.util.IPropertyChangeListener)
		 */
		public void removePropertyChangeListener(IPropertyChangeListener listener) {
			// FIXME compat removePropertyChangeListener
			E4Util.unsupported("removePropertyChangeListener"); //$NON-NLS-1$
		}

	}

}
