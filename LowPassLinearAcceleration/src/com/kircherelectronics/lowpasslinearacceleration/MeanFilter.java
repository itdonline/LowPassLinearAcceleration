package com.kircherelectronics.lowpasslinearacceleration;

import java.util.LinkedList;
import java.util.List;

/**
 * Implements a mean filter designed to smooth the data points based on a mean.
 * 
 * @author Kaleb
 * 
 */
public class MeanFilter
{
	// The size of the mean filters rolling window.
	private int MEAN_FILTER_WINDOW = 10;

	private boolean dataInit;

	private LinkedList<LinkedList<Float>> dataLists;

	/**
	 * Initialize a new MeanFilter object.
	 */
	public MeanFilter()
	{
		dataLists = new LinkedList<LinkedList<Float>>();
		dataInit = false;
	}

	/**
	 * Filter the data.
	 * 
	 * @param iterator
	 *            contains input the data.
	 * @return the filtered output data.
	 */
	public float[] filterFloat(float[] data)
	{
		for (int i = 0; i < data.length; i++)
		{
			// Initialize the data structures for the data set.
			if (!dataInit)
			{
				dataLists.add(new LinkedList<Float>());
			}

			dataLists.get(i).addLast(data[i]);

			if (dataLists.get(i).size() > MEAN_FILTER_WINDOW)
			{
				dataLists.get(i).removeFirst();
			}
		}
		
		dataInit = true;

		float[] means = new float[dataLists.size()];

		for (int i = 0; i < dataLists.size(); i++)
		{
			means[i] = (float) getMean(dataLists.get(i));
		}
		
		return means;
	}

	/**
	 * Get the mean of the data set.
	 * 
	 * @param data
	 *            the data set.
	 * @return the mean of the data set.
	 */
	private double getMean(List<Float> data)
	{
		double m = 0;
		double count = 0;

		for (int i = 0; i < data.size(); i++)
		{
			m += data.get(i);
			count++;
		}

		if (count != 0)
		{
			m = m / count;
		}

		return m;
	}

}
