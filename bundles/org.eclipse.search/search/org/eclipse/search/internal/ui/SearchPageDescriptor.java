/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Michael Fraenkel (fraenkel@us.ibm.com) - contributed a fix for:
 *       o Search dialog not respecting activity enablement
 *         (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=45729)
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.StringConverter;

import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.PlatformUI;

import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchPageScoreComputer;

/**
 * Proxy that represents a search page.
 */
class SearchPageDescriptor implements IPluginContribution, Comparable<SearchPageDescriptor> {

	public final static String PAGE_TAG= "page"; //$NON-NLS-1$
	private final static String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
	private final static String ICON_ATTRIBUTE= "icon"; //$NON-NLS-1$
	private final static String CLASS_ATTRIBUTE= "class"; //$NON-NLS-1$
	private final static String LABEL_ATTRIBUTE= "label"; //$NON-NLS-1$
	private final static String SIZE_ATTRIBUTE= "sizeHint"; //$NON-NLS-1$
	private final static String TAB_POSITION_ATTRIBUTE= "tabPosition"; //$NON-NLS-1$
	private final static String EXTENSIONS_ATTRIBUTE= "extensions"; //$NON-NLS-1$
	private final static String SHOW_SCOPE_SECTION_ATTRIBUTE= "showScopeSection"; //$NON-NLS-1$
	private final static String CAN_SEARCH_ENCLOSING_PROJECTS= "canSearchEnclosingProjects"; //$NON-NLS-1$
	private final static String CAN_SEARCH_OPENED_EDITORS = "canSearchOpenedEditors"; //$NON-NLS-1$
	private final static String ENABLED_ATTRIBUTE= "enabled"; //$NON-NLS-1$
	private final static String SEARCH_VIEW_HELP_CONTEXT_ID_ATTRIBUTE= "searchViewHelpContextId"; //$NON-NLS-1$

	public final static Point UNKNOWN_SIZE= new Point(SWT.DEFAULT, SWT.DEFAULT);

	// dialog store id constants
	private final static String SECTION_ID= "Search"; //$NON-NLS-1$
	private final static String STORE_ENABLED_PAGE_IDS= SECTION_ID + ".enabledPageIds"; //$NON-NLS-1$
	private final static String STORE_PROCESSED_PAGE_IDS= SECTION_ID + ".processedPageIds"; //$NON-NLS-1$

	private static List<String> fgEnabledPageIds;

	private static class ExtensionScorePair {
		public String extension;
		public int score;
		public ExtensionScorePair(String extension, int score) {
			this.extension= extension;
			this.score= score;
		}
	}

	private IConfigurationElement fElement;
	private List<ExtensionScorePair> fExtensionScorePairs;
	private int fWildcardScore= ISearchPageScoreComputer.UNKNOWN;
	private ISearchPage fCreatedPage;

	/**
	 * Creates a new search page node with the given configuration element.
	 * @param element The configuration element
	 */
	public SearchPageDescriptor(IConfigurationElement element) {
		fElement= element;
	}

	/**
	 * Creates a new search page from this node.
	 * @param container The parent container
	 * @return the created page or null if the creation failed
	 * @throws CoreException Page creation failed
	 */
	public ISearchPage createObject(ISearchPageContainer container) throws CoreException {
		if (fCreatedPage == null) {
			fCreatedPage= (ISearchPage) fElement.createExecutableExtension(CLASS_ATTRIBUTE);
			fCreatedPage.setTitle(getLabel());
			fCreatedPage.setContainer(container);
		}
		return fCreatedPage;
	}

	public ISearchPage getPage() {
		return fCreatedPage;
	}


	public void dispose() {
		if (fCreatedPage != null) {
			fCreatedPage.dispose();
			fCreatedPage= null;
		}
	}

	//---- XML Attribute accessors ---------------------------------------------

	/**
	 * Returns the page's id.
	 * @return The id of the page
	 */
	public String getId() {
		return fElement.getAttribute(ID_ATTRIBUTE);
	}

	/**
	 * Returns the page's image
	 * @return ImageDescriptor of the image or null if creating failed
	 */
	public ImageDescriptor getImage() {
		String imageName= fElement.getAttribute(ICON_ATTRIBUTE);
		if (imageName == null)
			return null;
		Bundle bundle = Platform.getBundle(getPluginId());
		return SearchPluginImages.createImageDescriptor(bundle, IPath.fromOSString(imageName), true);
	}

	/**
	 * @return Returns the page's label.
	 */
	public String getLabel() {
		return fElement.getAttribute(LABEL_ATTRIBUTE);
	}

	/**
	 * @return  Returns <code>true</code> if the scope section needs
	 * to be shown in the dialog.
	 */
	public boolean showScopeSection() {
		return Boolean.parseBoolean(fElement.getAttribute(SHOW_SCOPE_SECTION_ATTRIBUTE));
	}

	/**
	 * Returns <code>true</code> if the page is initially
	 * shown in the Search dialog.
	 *
	 * This attribute is optional and defaults to <code>true</code>.
	 * @return Returns if the page should be initially shown
	 */
	public boolean isInitiallyEnabled() {
		String strVal= fElement.getAttribute(ENABLED_ATTRIBUTE);
		return strVal == null || Boolean.parseBoolean(strVal);
	}

	/**
	 * Returns <code>true</code> if the page can handle
	 * searches in enclosing projects. The value should be ignored if <code>showScopeSection()</code>
	 * returns <code>false</code>.
	 *
	 * This attribute is optional and defaults to <code>false</code>.
	 * @return Returns if the page can handle searches in enclosing projects
	 */
	public boolean canSearchInProjects() {
		return Boolean.parseBoolean(fElement.getAttribute(CAN_SEARCH_ENCLOSING_PROJECTS));
	}

	/**
	 * Returns <code>true</code> if the page can handle searches in opened
	 * editors. The value should be ignored if <code>showScopeSection()</code>
	 * returns <code>false</code>.
	 *
	 * This attribute is optional and defaults to <code>false</code>.
	 * 
	 * @return Returns if the page can handle searches in opened editors
	 */
	public boolean canSearchInOpenedEditors() {
		return Boolean.parseBoolean(fElement.getAttribute(CAN_SEARCH_OPENED_EDITORS));
	}

	/**
	 * @return Returns the page's preferred size
	 */
	public Point getPreferredSize() {
		String sizeHint= fElement.getAttribute(SIZE_ATTRIBUTE);
		if (sizeHint != null) {
			int commaSep= sizeHint.indexOf(',');
			if (commaSep != -1) {
				try {
					int xval= Integer.parseInt(sizeHint.substring(0, commaSep).trim());
					int yval= Integer.parseInt(sizeHint.substring(commaSep + 1).trim());
					return new Point(xval, yval);
				} catch (NumberFormatException e) {
				}
			}
		}
		return UNKNOWN_SIZE;
	}

	/**
	 * Returns the page's tab position relative to the other tabs.
	 * @return	the tab position or <code>Integer.MAX_VALUE</code> if not defined in
	 *			the plugins.xml file
	 */
	public int getTabPosition() {
		int position= Integer.MAX_VALUE / 2;
		String str= fElement.getAttribute(TAB_POSITION_ATTRIBUTE);
		if (str != null)
			try {
				position= Integer.parseInt(str);
		} catch (NumberFormatException ex) {
			ExceptionHandler.log(ex, SearchMessages.Search_Error_createSearchPage_message);
			// position is Integer.MAX_VALUE;
		}
		return position;
	}

	boolean isEnabled() {
		return getEnabledPageIds().contains(getId());
	}

	/**
	 * Returns the help context for help shown in search view.
	 *
	 * @return the help context id or <code>null</code> if not defined
	 */
	public String getSearchViewHelpContextId() {
		return fElement.getAttribute(SEARCH_VIEW_HELP_CONTEXT_ID_ATTRIBUTE);
	}

	static void setEnabled(Object[] enabledDescriptors) {
		fgEnabledPageIds= new ArrayList<>(5);
		for (Object enabledDescriptor : enabledDescriptors) {
			if (enabledDescriptor instanceof SearchPageDescriptor)
				fgEnabledPageIds.add(((SearchPageDescriptor)enabledDescriptor).getId());
		}
		storeEnabledPageIds();
	}

	private static List<String> getEnabledPageIds() {
		if (fgEnabledPageIds == null) {
			List<SearchPageDescriptor> descriptors= SearchPlugin.getDefault().getSearchPageDescriptors();

			String[] enabledPageIds= getDialogSettings().getArray(STORE_ENABLED_PAGE_IDS);
			if (enabledPageIds == null)
				fgEnabledPageIds= new ArrayList<>(descriptors.size());
			else
				fgEnabledPageIds= new ArrayList<>(Arrays.asList(enabledPageIds));


			List<String> processedPageIds;
			String[] processedPageIdsArr= getDialogSettings().getArray(STORE_PROCESSED_PAGE_IDS);
			if (processedPageIdsArr == null)
				processedPageIds= new ArrayList<>(descriptors.size());
			else
				processedPageIds= new ArrayList<>(Arrays.asList(processedPageIdsArr));

			// Enable pages based on contribution
			Iterator<SearchPageDescriptor> iter= descriptors.iterator();
			while (iter.hasNext()) {
				SearchPageDescriptor desc= iter.next();
				if (processedPageIds.contains(desc.getId()))
					continue;

				processedPageIds.add(desc.getId());
				if (desc.isInitiallyEnabled())
					fgEnabledPageIds.add(desc.getId());
			}

			getDialogSettings().put(STORE_PROCESSED_PAGE_IDS, processedPageIds.toArray(new String[processedPageIds.size()]));
			storeEnabledPageIds();
		}
		return fgEnabledPageIds;
	}

	private static void storeEnabledPageIds() {
		getDialogSettings().put(STORE_ENABLED_PAGE_IDS, fgEnabledPageIds.toArray(new String[fgEnabledPageIds.size()]));
	}

	private static IDialogSettings getDialogSettings() {
		IDialogSettings settings = PlatformUI
				.getDialogSettingsProvider(FrameworkUtil.getBundle(SearchPageDescriptor.class)).getDialogSettings();
		IDialogSettings section= settings.getSection(SECTION_ID);
		if (section == null)
			// create new section
			section= settings.addNewSection(SECTION_ID);
		return section;
	}

	@Override
	public int compareTo(SearchPageDescriptor o) {
		int myPos= getTabPosition();
		int objsPos= o.getTabPosition();
		if (myPos == Integer.MAX_VALUE && objsPos == Integer.MAX_VALUE || myPos == objsPos)
			return getLabel().compareTo(o.getLabel());

		return myPos - objsPos;
	}

	//---- Suitability tests ---------------------------------------------------

	/**
	 * Returns the score for this page with the given input element.
	 * @param element The input element
	 * @return The scope for the page
	 */
	public int computeScore(Object element) {
		if (element instanceof IAdaptable) {
			int score= ISearchPageScoreComputer.UNKNOWN;

			ISearchPageScoreComputer tester= ((IAdaptable)element).getAdapter(ISearchPageScoreComputer.class);
			if (tester != null)
				score= tester.computeScore(getId(), element);

			IResource resource= ((IAdaptable)element).getAdapter(IResource.class);
			if (resource != null && resource.getType() == IResource.FILE) {
				String extension= resource.getFileExtension();
				if (extension != null)
					score= Math.max(score, getScoreForFileExtension(extension));
			}
			if (score != ISearchPageScoreComputer.UNKNOWN)
				return score;
		}
		if (fWildcardScore != ISearchPageScoreComputer.UNKNOWN)
			return fWildcardScore;

		return ISearchPageScoreComputer.LOWEST;
	}

	private int getScoreForFileExtension(String extension) {
		if (fExtensionScorePairs == null)
			readExtensionScorePairs();

		int size= fExtensionScorePairs.size();
		for (int i= 0; i < size; i++) {
			ExtensionScorePair p= fExtensionScorePairs.get(i);
			if (extension.equals(p.extension))
				return p.score;
		}

		return ISearchPageScoreComputer.UNKNOWN;
	}

	private void readExtensionScorePairs() {
		fExtensionScorePairs= new ArrayList<>(3);
		String content= fElement.getAttribute(EXTENSIONS_ATTRIBUTE);
		if (content == null)
			return;
		StringTokenizer tokenizer= new StringTokenizer(content, ","); //$NON-NLS-1$
		while (tokenizer.hasMoreElements()) {
			String token= tokenizer.nextToken().trim();
			int pos= token.indexOf(':');
			if (pos != -1) {
				String extension= token.substring(0, pos);
				int score= StringConverter.asInt(token.substring(pos+1).trim(), ISearchPageScoreComputer.UNKNOWN);
				if (extension.equals("*")) { //$NON-NLS-1$
					fWildcardScore= score;
				} else {
					fExtensionScorePairs.add(new ExtensionScorePair(extension, score));
				}
			}
		}
	}

	@Override
	public String getLocalId() {
		return getId();
	}

	@Override
	public String getPluginId() {
		return fElement.getContributor().getName();
	}
}
