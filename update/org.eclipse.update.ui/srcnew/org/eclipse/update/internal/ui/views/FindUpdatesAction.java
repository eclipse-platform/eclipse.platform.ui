/*
 * Created on Jun 9, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.update.core.IFeature;

public class FindUpdatesAction extends Action {

	private IFeature feature;

	public FindUpdatesAction(String text) {
		super(text);
	}
	
	public void setFeature(IFeature feature) {
		this.feature = feature;
	}

	public void run() {
		//TODO Dejan to implement this method
		if (feature == null) {
			// search for all updates
		} else {
			// search for updates for the selected feature
		}
	}

}
