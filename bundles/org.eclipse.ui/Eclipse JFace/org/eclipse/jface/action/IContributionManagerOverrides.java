package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * This interface is used by instances of <code>IContributionItem</code>
 * to determine if the values for certain properties have been overriden
 * by their manager.
 * 
 * @since 2.0
 */
public interface IContributionManagerOverrides {
	/**
	 * Id for the enable allowed property. Value is 
	 * <code>"enableAllowed"</code>.
	 * 
	 * @since 2.0
	 */
	public final static String P_ENABLE_ALLOWED = "enableAllowed";
	
	/**
	 * Returns true if the given contribution item is 
	 * allowed to enable.
	 * 
	 * @param the contribution item for which enable allowed is 
	 * determined
	 * @since 2.0
	 */
	public boolean getEnabledAllowed(IContributionItem item);
	
	public Integer getAccelerator(IContributionItem item);
	public String getAcceleratorText(IContributionItem item);
	public String getText(IContributionItem item);
}
