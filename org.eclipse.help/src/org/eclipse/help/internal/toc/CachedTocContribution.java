package org.eclipse.help.internal.toc;

import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;

/*
 * Wraps an existing ITocContribution, pre-fetches all the data in constructor
 * (calls all operations) and returns cached values only.
 */
public class CachedTocContribution implements ITocContribution {

	private String categoryId;
	private String id;
	private String locale;
	private IToc toc;
	
	/*
	 * Constructs a wrapper for the given contribution. All data is fetched
	 * in constructor.
	 */
	public CachedTocContribution(ITocContribution original) {
		categoryId = original.getCategoryId();
		id = original.getId();
		locale = original.getLocale();
		toc = new CachedToc(original.getToc());
	}
	
	public String getCategoryId() {
		return categoryId;
	}

	public String getId() {
		return id;
	}

	public String getLocale() {
		return locale;
	}

	public IToc getToc() {
		return toc;
	}

}
