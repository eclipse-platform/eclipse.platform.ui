package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.Attributes;

public class TaskEntry extends LogEntry {
	private List /* of Result */ results = new ArrayList();
	
	public TaskEntry(LogEntryContainer parent, Attributes attributes) {
		this(parent, attributes.getValue("name"));
	}
	
	public TaskEntry(LogEntryContainer parent, String name) {
		super(parent, name);
	}
	
	public void accept(ILogEntryVisitor visitor) {
		visitor.visitTaskEntry(this);
	}
	
	/**
	 * Returns the average number of milliseconds elapsed, or -1 if unknown.
	 */
	public int getAverageMillis() {
		int totalMillis = 0;
		int totalRuns = 0;
		for (Iterator it = results.iterator(); it.hasNext();) {
			Result result = (Result) it.next();
			totalMillis += result.getMillis();
			totalRuns += result.getRuns();
		}
		if (totalRuns == 0) return -1;
		return totalMillis / totalRuns;
	}
	
	/**
	 * Returns the standard deviation of the sample.
	 * sqrt((n * sum(X^2) - sum(X)^2) / (n * (n-1)))
	 */
	public double getStandardDeviation() {
		double sumOfSquares = 0.0, sum = 0.0;
		int totalRuns = 0;
		for (Iterator it = results.iterator(); it.hasNext();) {
			Result result = (Result) it.next();
			if (result.getRuns() == 0) continue;
			totalRuns += result.getRuns();
			sum += result.getMillis();
			double average = (double)result.getMillis() / result.getRuns();
			sumOfSquares += average * average * result.getRuns();
		}
		if (totalRuns == 0) return 0;
		return Math.sqrt((sumOfSquares * totalRuns - sum * sum) / (totalRuns * (totalRuns - 1)));
	}
	
	/**
	 * Returns a 95% confidence interval from the mean represented by getAverageMillis()
	 * Uses the formula:
	 *   1.960 * stdev() / sqrt(n)
	 */
	public int getConfidenceInterval() {
		return (int) (1.960 * getStandardDeviation() / Math.sqrt(getTotalRuns()));
	}
	
	/**
	 * Returns the number of times this task was run.
	 */
	public int getTotalRuns() {
		int totalRuns = 0;
		for (Iterator it = results.iterator(); it.hasNext();) {
			Result result = (Result) it.next();
			totalRuns += result.getRuns();
		}
		return totalRuns;
	}
	
	/**
	 * Returns an array of all Results for this task.
	 */
	public Result[] getResults() {
		return (Result[]) results.toArray(new Result[results.size()]);
	}
	
	/**
	 * Adds a result.
	 * @param result the result
	 */
	public void addResult(Result result) {
		results.add(result);
	}
	

}
