package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.widgets.Shell;


/**
 * Interface of a factory of information controls.
 */
public interface IInformationControlCreator {
	
	/**
	 * Creates a new information control parented to the given shell.
	 * 
	 * @param parent the parent shell
	 */
	IInformationControl createInformationControl(Shell parent);
}

