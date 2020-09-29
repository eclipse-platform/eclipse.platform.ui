/*******************************************************************************
 * Copyright (c) 2013, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *     Christoph Läubrich - Bug 538301
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.osgi.util.NLS;

public class PartServiceSaveHandler implements ISaveHandler {

	public Logger logger;

	private void log(String unidentifiedMessage, String identifiedMessage, String id, Exception e) {
		if (logger == null) {
			return;
		}
		if (id == null || id.isEmpty()) {
			logger.error(e, unidentifiedMessage);
		} else {
			logger.error(e, NLS.bind(identifiedMessage, id));
		}
	}

	@Override
	public boolean save(MPart dirtyPart, boolean confirm) {
		Object client = dirtyPart.getObject();
		IEclipseContext context = dirtyPart.getContext();
		if (client == null || context == null) {
			log("Failed to persist contents of part", //$NON-NLS-1$
					"Failed to persist contents of part ({0}) because the part was not rendered", //$NON-NLS-1$
					dirtyPart.getElementId(), new RuntimeException());
			return false;
		}
		if (confirm) {
			switch (promptToSave(dirtyPart)) {
			case NO:
				return true;
			case CANCEL:
				return false;
			case YES:
				break;
			}
		}
		try {
			ContextInjectionFactory.invoke(client, Persist.class, context);
		} catch (InjectionException e) {
			log("Failed to persist contents of part", "Failed to persist contents of part ({0})", //$NON-NLS-1$ //$NON-NLS-2$
					dirtyPart.getElementId(), e);
			return false;
		} catch (RuntimeException e) {
			log("Failed to persist contents of part via DI", //$NON-NLS-1$
					"Failed to persist contents of part ({0}) via DI", dirtyPart.getElementId(), e); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	@Override
	public boolean saveParts(Collection<MPart> dirtyParts, boolean confirm) {
		if (confirm) {
			List<MPart> dirtyPartsList = Collections.unmodifiableList(new ArrayList<>(
					dirtyParts));
			Save[] decisions = promptToSave(dirtyPartsList);
			for (Save decision : decisions) {
				if (decision == Save.CANCEL) {
					return false;
				}
			}

			for (int i = 0; i < decisions.length; i++) {
				if (decisions[i] == Save.YES) {
					if (!save(dirtyPartsList.get(i), false)) {
						return false;
					}
				}
			}
			return true;
		}

		for (MPart dirtyPart : dirtyParts) {
			if (!save(dirtyPart, false)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Save promptToSave(MPart dirtyPart) {
		return Save.YES;
	}

	@Override
	public Save[] promptToSave(Collection<MPart> dirtyParts) {
		Save[] rc = new Save[dirtyParts.size()];
		Arrays.fill(rc, Save.YES);
		return rc;
	}

}
