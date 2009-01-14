package org.eclipse.e4.demo.e4photo;

import org.eclipse.e4.core.services.ComputedValue;
import org.eclipse.e4.core.services.Context;

public class RealmValue extends ComputedValue {

	@Override
	protected Object compute(Context context) {
		return new LockRealm();
	}

}
