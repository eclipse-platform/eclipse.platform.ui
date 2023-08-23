package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;
import org.eclipse.ui.part.ViewPart;

/**
 * @since 3.4
 *
 */
public class MarkerViewFindHandler implements IHandler {
	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Adapted from https://www.eclipse.org/forums/index.php/t/104714/
		Object appContextObj = event.getApplicationContext();
		if (appContextObj instanceof IEvaluationContext appContext) {
			ViewPart viewPart = (ViewPart) appContext.getVariable(ISources.ACTIVE_PART_NAME);
			ExtendedMarkersView mv = (ExtendedMarkersView) viewPart;
			mv.getFindReplaceAction().run();
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isHandled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
