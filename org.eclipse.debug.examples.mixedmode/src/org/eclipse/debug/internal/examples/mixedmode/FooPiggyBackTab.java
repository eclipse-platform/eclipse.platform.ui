package org.eclipse.debug.internal.examples.mixedmode;

import java.util.HashSet;
import java.util.Set;

public class FooPiggyBackTab extends FooTab {

	/**
	 * @see org.eclipse.debug.internal.examples.mixedmode.FooTab#getName()
	 */
	@Override
	public String getName() {
		return Messages.FooPiggyBackTab_0;
	}

	/**
	 * @see org.eclipse.debug.internal.examples.mixedmode.FooTab#getId()
	 */
	@Override
	public String getId() {
		return Messages.FooPiggyBackTab_1;
	}

	/**
	 * @return the set of modes this tab works with
	 */
	@Override
	public Set<String> getModes() {
		if (fOptions == null) {
			fOptions = new HashSet<String>();
			fOptions.add(Messages.FooPiggyBackTab_2);
			fOptions.add(Messages.FooPiggyBackTab_3);
		}
		return fOptions;
	}

}
