package org.eclipse.ui.internal.progress;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * @author tod
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ProgressServiceJob extends Job {
	
	IRunnableWithProgress runnable;
	
	public ProgressServiceJob(IRunnableWithProgress progressRunnable){
		runnable = progressRunnable;		
	}


	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJob#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int run(IProgressMonitor monitor) {
		try{
			runnable.run(monitor);
			return 0;
		}
		catch (InvocationTargetException e){
			return 1;
		}
		catch(InterruptedException e){
			return 1;
		}
	}

	
}
