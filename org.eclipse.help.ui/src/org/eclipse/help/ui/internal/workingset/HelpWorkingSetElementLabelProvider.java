package org.eclipse.help.ui.internal.workingset;

/*
 * (c) Copyright IBM Corp. 2002. 
 * All Rights Reserved.
 */


import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;

public class HelpWorkingSetElementLabelProvider extends LabelProvider {

	/**
	 * Constructor for HelpWorkingSetElementLabelProvider.
	 */
	public HelpWorkingSetElementLabelProvider() {
		super();
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof HelpResource && 
			((HelpResource)element).asResource() != null)
			return ((HelpResource)element).asResource().getLabel();
		else
			return null;
	}

}
