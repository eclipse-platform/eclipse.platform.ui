/*
 * Copyright (c) 2002, Roscoe Rush. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 0.5 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclipse.org/
 *
 */
package org.eclipse.ui.externaltools.internal.ant.antview.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.externaltools.internal.ant.antview.views.AntView;
import org.eclipse.ui.externaltools.internal.ant.antview.views.AntViewContentProvider;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;

public class ClearAction extends Action {
	/**
	 * Constructor for ClearAction
	 */
	public ClearAction(String label, ImageDescriptor imageDescriptor) {
		super(label, imageDescriptor);
		setToolTipText(label);
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
        AntView antView = AntUtil.getAntView();
        if (antView == null) {
        	return;
        }
		AntViewContentProvider viewContentProvider =  antView.getViewContentProvider();
       
        viewContentProvider.clear();
		
		antView.refresh();
	}
}