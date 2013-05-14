/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
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
		if (id == null || id.length() == 0) {
			logger.error(e, unidentifiedMessage);
		} else {
			logger.error(e, NLS.bind(identifiedMessage, id));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.ISaveHandler#save(org.eclipse.e4.ui.model.application
	 * .ui.basic.MPart, boolean)
	 */
	public boolean save(MPart dirtyPart, boolean confirm) {
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

		Object client = dirtyPart.getObject();
		try {
			ContextInjectionFactory.invoke(client, Persist.class, dirtyPart.getContext());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.modeling.ISaveHandler#saveParts(java.util.Collection,
	 * boolean)
	 */
	public boolean saveParts(Collection<MPart> dirtyParts, boolean confirm) {
		if (confirm) {
			List<MPart> dirtyPartsList = Collections.unmodifiableList(new ArrayList<MPart>(
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.modeling.ISaveHandler#promptToSave(org.eclipse.e4.ui.model.
	 * application.ui.basic.MPart)
	 */
	public Save promptToSave(MPart dirtyPart) {
		return Save.YES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.modeling.ISaveHandler#promptToSave(java.util.Collection)
	 */
	public Save[] promptToSave(Collection<MPart> dirtyParts) {
		Save[] rc = new Save[dirtyParts.size()];
		for (int i = 0; i < rc.length; i++) {
			rc[i] = Save.YES;
		}
		return rc;
	}

}
