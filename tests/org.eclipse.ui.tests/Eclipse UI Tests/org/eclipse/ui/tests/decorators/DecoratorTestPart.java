package org.eclipse.ui.tests.decorators;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * The DecoratorTestPart is the abstract superclass of the ViewParts that are
 * used for decorator tests.
 * 
 */
public abstract class DecoratorTestPart extends ViewPart {

	public boolean waitingForDecoration = true;
	

	private long endTime;

	public DecoratorTestPart() {
		super();
	}

	/**
	 * Get the label provider for the receiver.
	 * 
	 * @return
	 */
	protected DecoratingLabelProvider getLabelProvider() {

		IDecoratorManager manager = PlatformUI.getWorkbench()
				.getDecoratorManager();
		manager.addListener(new ILabelProviderListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ILabelProviderListener#labelProviderChanged(org.eclipse.jface.viewers.LabelProviderChangedEvent)
			 */
			public void labelProviderChanged(LabelProviderChangedEvent event) {
				if(event.getElements() == null)
					return;
				//Reset the end time each time we get an update
				endTime = System.currentTimeMillis() + 1000;
					
			}
		});
		return new DecoratingLabelProvider(new TestLabelProvider(), manager);

	}

	public void readAndDispatchForUpdates() {
		while (System.currentTimeMillis() < endTime)
			Display.getCurrent().readAndDispatch();

	}

	public void setUpForDecorators() {
		endTime = System.currentTimeMillis() + 1000;
		
	}


}
