package org.eclipse.help.internal.search;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Progress monitor for indexing
 * @since 2.0
 */
public class IndexProgressMonitor implements IProgressMonitor
{
	private boolean isCancelled;
	private int totalWork = -1;
	private int currWork;

	public IndexProgressMonitor()
	{
	}
	public void beginTask(String name, int totalWork)
	{
		this.totalWork = totalWork;
	}

	public void done()
	{
	}

	public void setTaskName(String name)
	{
	}

	public boolean isCanceled()
	{
		return isCancelled;
	}

	public void setCanceled(boolean b)
	{
		isCancelled = b;
	}

	public void subTask(String name)
	{
	}

	public void worked(int work)
	{
		currWork += work;
		if (currWork > totalWork)
			currWork = totalWork;
		else if (currWork < 0)
			currWork = 0;

		internalWorked(work);
	}

	public void internalWorked(double work)
	{
	}

	public int getPercentage()
	{
		if (totalWork == -1)
			return 0;
		if (currWork == totalWork || totalWork == 0)
			return 100;
		return (int) (100 * currWork / totalWork);
	}
}