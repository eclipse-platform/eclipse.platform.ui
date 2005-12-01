package org.eclipse.ui.navigator.internal;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.NavigatorActivationService;
import org.eclipse.ui.navigator.internal.extensions.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.internal.extensions.INavigatorViewerDescriptor;

public class Utilities {

	public static boolean isApplicable(INavigatorViewerDescriptor aViewerDescriptor, INavigatorContentDescriptor aContentDescriptor, Object anElement) {		
		return isActive(aViewerDescriptor, aContentDescriptor) && isVisible(aViewerDescriptor, aContentDescriptor) && isEnabled( aContentDescriptor, anElement); 	
	}
		
	public static boolean isApplicable(INavigatorViewerDescriptor aViewerDescriptor, INavigatorContentDescriptor aContentDescriptor, IStructuredSelection aSelection) {		
		return isActive(aViewerDescriptor, aContentDescriptor) && isVisible(aViewerDescriptor, aContentDescriptor) && isEnabled( aContentDescriptor, aSelection); 	
	}
		
	public static boolean isActive(INavigatorViewerDescriptor aViewerDescriptor, INavigatorContentDescriptor aContentDescriptor) {
		return NavigatorActivationService.getInstance().isNavigatorExtensionActive(aViewerDescriptor.getViewerId(), aContentDescriptor.getId());
	}
	
	public static boolean isVisible(INavigatorViewerDescriptor aViewerDescriptor, INavigatorContentDescriptor aContentDescriptor) {
		return aViewerDescriptor.isVisibleExtension(aContentDescriptor.getId());
	}

	public static boolean isEnabled(INavigatorContentDescriptor aContentDescriptor, Object anElement) {
		return aContentDescriptor.isEnabledFor(anElement);
	}
	
	public static boolean isEnabled(INavigatorContentDescriptor aContentDescriptor, IStructuredSelection aSelection) {
		return aContentDescriptor.isEnabledFor(aSelection);
	}
}
