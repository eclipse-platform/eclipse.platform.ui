/*
 * Created on May 29, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.search;

/**
 * This interface is used for update site adapter used 
 * for specific query serches. It adds a mapping ID 
 * that can be used when mapping file is specified.
 * If a matching mapping is found for this ID, 
 * the replacement URL found in the mapping file will be
 * used instead of this adapter.
 */
public interface IQueryUpdateSiteAdapter extends IUpdateSiteAdapter {
	public String getMappingId();

}
