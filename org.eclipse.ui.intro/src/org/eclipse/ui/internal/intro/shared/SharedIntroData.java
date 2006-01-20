package org.eclipse.ui.internal.intro.shared;


public class SharedIntroData {
	private static SharedIntroData instance;
	
	public static SharedIntroData getInstance() {
		if (instance==null)
			instance = new SharedIntroData();
		return instance;
	}
}