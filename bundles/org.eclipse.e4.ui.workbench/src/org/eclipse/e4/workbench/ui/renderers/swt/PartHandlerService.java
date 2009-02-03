package org.eclipse.e4.workbench.ui.renderers.swt;

import java.util.Iterator;

import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.workbench.ui.IHandlerService;

public class PartHandlerService implements IHandlerService {
	private MPart part;
	
	public PartHandlerService(MPart p) {
		part = p;
	}

	public MHandler getHandler(MCommand command) {
		Iterator i = part.getHandlers().iterator();
		while (i.hasNext()) {
			MHandler h = (MHandler) i.next();
			if (command.equals(h.getCommand())) {
				return h;
			}
		}
		return null;
	}

}
