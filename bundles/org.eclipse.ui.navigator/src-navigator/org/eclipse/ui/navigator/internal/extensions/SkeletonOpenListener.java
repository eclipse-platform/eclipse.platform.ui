/*
 * Created on Feb 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.navigator.internal.extensions;

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
public class SkeletonOpenListener implements ICommonOpenListener {

	public static final SkeletonOpenListener INSTANCE = new SkeletonOpenListener();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IOpenListener#open(org.eclipse.jface.viewers.OpenEvent)
	 */
	public void open(OpenEvent event) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	public SkeletonOpenListener() {
		super();
		// TODO Auto-generated constructor stub
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.wst.common.navigator.internal.views.extensions.ICommonOpenListener#initialize(org.eclipse.wst.common.navigator.views.CommonNavigator, org.eclipse.wst.common.navigator.internal.views.extensions.NavigatorContentService)
	 */
	public void initialize(CommonNavigator aCommonNavigator, NavigatorContentService aContentService) {
		// TODO Auto-generated method stub
		
	}
}
