package org.eclipse.ui;

/**
 * Plugins that register a startup extension will be activated after
 * the Workbench initializes and have an opportunity to run 
 * code that can't be implemented using the normal contribution 
 * mechanisms.
 * 
 * <p>
 * @since 2.0
 */
public interface IStartup {
	/**
	 * Will be called in a separed thread after the workbench initializes.
	 */
	public void earlyStartup();
}
 