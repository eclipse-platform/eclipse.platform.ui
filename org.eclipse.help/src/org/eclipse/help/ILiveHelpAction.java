/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help;
/**
 * Live Help Extension.  Classes that implement this interface
 * can be used as live help extensions.
 * When user clicks live help link on the help document,
 * the class will be loaded and run.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @since 2.0
 */
public interface ILiveHelpAction extends Runnable {
	/**
	 * This method will be called upon instantiation of the
	 * live help extension.  The data will be passed as specified
	 * in the help document live help link.
	 * @param data - initialization data as a String
	 */
	public void setInitializationString(String data);
}