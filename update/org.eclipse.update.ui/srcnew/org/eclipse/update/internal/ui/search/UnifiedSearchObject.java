/*
 * Created on Apr 18, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.ui.search;

import java.util.ArrayList;

import org.eclipse.update.internal.ui.model.SiteBookmark;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UnifiedSearchObject extends SearchObject {
	private SiteBookmark[] bookmarks;

	/**
	 * 
	 */
	public UnifiedSearchObject() {
		super();
	}

	/**
	 * @param name
	 * @param descriptor
	 */
	public UnifiedSearchObject(
		String name,
		SearchCategoryDescriptor descriptor) {
		super(name, descriptor);
	}

	/**
	 * @param name
	 * @param descriptor
	 * @param categoryFixed
	 */
	public UnifiedSearchObject(
		String name,
		SearchCategoryDescriptor descriptor,
		boolean categoryFixed) {
		super(name, descriptor, categoryFixed);
	}

	public void computeSearchSources(ArrayList sources) {
		super.computeSearchSources(sources);
		if (bookmarks != null) {
			for (int i = 0; i < bookmarks.length; i++) {
				sources.add(bookmarks[i]);
			}
		}
	}

	public void setSelectedBookmarks(SiteBookmark[] bookmarks) {
		this.bookmarks = bookmarks;
	}
}
