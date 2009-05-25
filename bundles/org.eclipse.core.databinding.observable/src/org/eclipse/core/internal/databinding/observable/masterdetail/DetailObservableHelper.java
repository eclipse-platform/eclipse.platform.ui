/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 246782)
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable.masterdetail;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.internal.databinding.observable.Util;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/* package */class DetailObservableHelper {
	/* package */static void warnIfDifferentRealms(Realm detailRealm,
			Realm innerObservableRealm) {
		if (!Util.equals(detailRealm, innerObservableRealm)) {
			Throwable throwable = new Throwable();
			throwable.fillInStackTrace();
			String message = "Inner observable realm (" + innerObservableRealm //$NON-NLS-1$
					+ ") not equal to detail realm (" //$NON-NLS-1$
					+ detailRealm + ")"; //$NON-NLS-1$
			Policy.getLog().log(
					new Status(IStatus.WARNING, Policy.JFACE_DATABINDING,
							message, throwable));
		}
	}
}
