/*
 * Created on Feb 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.navigator.internal.extensions;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ICommonOpenListener;
import org.eclipse.ui.navigator.NavigatorContentService;

/**
 * @author Admin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DelegateCommonOpenListener implements ICommonOpenListener{

	private IOpenListener openListener;
	

	/* (non-Javadoc)
	 * @see org.eclipse.wst.common.navigator.internal.views.extensions.ICommonOpenListener#initialize(org.eclipse.ui.part.ViewPart)
	 */
	public DelegateCommonOpenListener(IOpenListener anOpenListener) {
		super();
		openListener = anOpenListener;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IOpenListener#open(org.eclipse.jface.viewers.OpenEvent)
	 */
	public void open(OpenEvent event) {
		openListener.open(event);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		
		return openListener.equals(obj);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		
		return openListener.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		
		return openListener.toString();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.wst.common.navigator.internal.views.extensions.ICommonOpenListener#initialize(org.eclipse.wst.common.navigator.views.CommonNavigator, org.eclipse.wst.common.navigator.internal.views.extensions.NavigatorContentService)
	 */
	public void initialize(CommonNavigator aCommonNavigator, NavigatorContentService aContentService) {
			
	}
	
	
}
