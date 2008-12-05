package org.eclipse.e4.workbench.ui.renderers.swt;

import java.util.Iterator;

import org.eclipse.e4.ui.model.application.Command;
import org.eclipse.e4.ui.model.application.Handler;
import org.eclipse.e4.ui.model.application.Part;
import org.eclipse.e4.workbench.ui.IHandlerService;

public class PartHandlerService implements IHandlerService {
	private Part part;
	
	public PartHandlerService(Part p) {
		part = p;
	}

	public Handler getHandler(Command command) {
		Iterator i = part.getHandlers().iterator();
		while (i.hasNext()) {
			Handler h = (Handler) i.next();
			if (command.equals(h.getCommand())) {
				return h;
			}
		}
		return null;
	}

}
