package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * This interface is used by instances of <code>IContributionItem</code>
 * to determine if the values for certain properties have been overriden
 * by their manager.
 * <p>
 * This interface is internal to the framework; it should not be implemented outside
 * the framework.
 * </p>
 * 
 * @since 2.0
 */
public interface IContributionManagerOverrides {
	/**
	 * Id for the enabled property. Value is <code>"enabled"</code>.
	 * 
	 * @since 2.0
	 */
	public final static String P_ENABLED = "enabled"; //$NON-NLS-1$
	
	/**
	 * Returns <code>Boolean.TRUE</code> if the given contribution item should 
	 * be enabled, <code>Boolean.FALSE</code> if the item should be disabled, and
	 * <code>null</code> if the item may determine its own enablement.
	 * 
	 * @param the contribution item for which the enable override value is 
	 * determined
	 * @since 2.0
	 */
	public Boolean getEnabled(IContributionItem item);
	
	public Integer getAccelerator(IContributionItem item);
	public String getAcceleratorText(IContributionItem item);
	public String getText(IContributionItem item);
}
