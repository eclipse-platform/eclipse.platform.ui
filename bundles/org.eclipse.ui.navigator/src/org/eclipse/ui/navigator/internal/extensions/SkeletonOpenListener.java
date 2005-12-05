/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class SkeletonOpenListener implements ICommonOpenListener {

	public static final SkeletonOpenListener INSTANCE = new SkeletonOpenListener();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IOpenListener#open(org.eclipse.jface.viewers.OpenEvent)
	 */
	public void open(OpenEvent event) {

	}

	/**
	 * 
	 */
	public SkeletonOpenListener() {
		super();
	}

	public void initialize(CommonNavigator aCommonNavigator,
			INavigatorContentService aContentService) {

	}
}
