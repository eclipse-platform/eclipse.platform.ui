/*
 * Created on May 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.search;

import org.eclipse.update.core.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IUpdateSearchResultCollector {
   void setRequest(UpdateSearchRequest request);
   UpdateSearchRequest getRequest();
   void accept(ISite originatingSite, IFeature match);
}