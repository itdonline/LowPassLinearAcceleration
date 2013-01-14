package com.kircherelectronics.lowpasslinearacceleration;

import java.util.ArrayList;
import java.util.LinkedList;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import android.graphics.Color;

/**
 * Acceleration View is responsible for creating and managing all of the plotter
 * components related to Acceleration.
 * 
 * @author Kaleb
 * 
 */
public class PlotView
{
	private int windowSize = 100;

	private double maxRange = 10;
	private double minRange = -10;

	private XYPlot dynamicAPlot;

	private SimpleXYSeries gravityWikiXAxisSeries;
	private SimpleXYSeries gravityWikiYAxisSeries;
	private SimpleXYSeries gravityWikiZAxisSeries;

	private SimpleXYSeries accelWikiXAxisSeries;
	private SimpleXYSeries accelWikiYAxisSeries;
	private SimpleXYSeries accelWikiZAxisSeries;

	private SimpleXYSeries gravityADevXAxisSeries;
	private SimpleXYSeries gravityADevYAxisSeries;
	private SimpleXYSeries gravityADevZAxisSeries;

	private SimpleXYSeries accelADevXAxisSeries;
	private SimpleXYSeries accelADevYAxisSeries;
	private SimpleXYSeries accelADevZAxisSeries;

	private SimpleXYSeries rawXAxisSeries;
	private SimpleXYSeries rawYAxisSeries;
	private SimpleXYSeries rawZAxisSeries;

	private LinkedList<Number> gravityWikiXAxisHistory;
	private LinkedList<Number> gravityWikiYAxisHistory;
	private LinkedList<Number> gravityWikiZAxisHistory;

	private LinkedList<Number> accelWikiXAxisHistory;
	private LinkedList<Number> accelWikiYAxisHistory;
	private LinkedList<Number> accelWikiZAxisHistory;

	private LinkedList<Number> gravityADevXAxisHistory;
	private LinkedList<Number> gravityADevYAxisHistory;
	private LinkedList<Number> gravityADevZAxisHistory;

	private LinkedList<Number> accelADevXAxisHistory;
	private LinkedList<Number> accelADevYAxisHistory;
	private LinkedList<Number> accelADevZAxisHistory;

	private LinkedList<Number> rawXAxisHistory;
	private LinkedList<Number> rawYAxisHistory;
	private LinkedList<Number> rawZAxisHistory;

	private ArrayList<LinkedList<Number>> histories;

	private boolean drawXAxis = true;
	private boolean drawYAxis = true;
	private boolean drawZAxis = true;

	private boolean drawRaw = true;
	private boolean drawGravity = true;
	private boolean drawAccel = true;

	private boolean drawWikiLP = true;
	private boolean drawAndDevLP = false;

	/**
	 * Initialize a new Acceleration View object.
	 * 
	 * @param activity
	 *            the Activity that owns this View.
	 */
	public PlotView(XYPlot dynamicAPlot)
	{
		histories = new ArrayList<LinkedList<Number>>();

		gravityWikiXAxisHistory = new LinkedList<Number>();
		gravityWikiYAxisHistory = new LinkedList<Number>();
		gravityWikiZAxisHistory = new LinkedList<Number>();

		histories.add(gravityWikiXAxisHistory);
		histories.add(gravityWikiYAxisHistory);
		histories.add(gravityWikiZAxisHistory);

		gravityADevXAxisHistory = new LinkedList<Number>();
		gravityADevYAxisHistory = new LinkedList<Number>();
		gravityADevZAxisHistory = new LinkedList<Number>();

		histories.add(gravityADevXAxisHistory);
		histories.add(gravityADevYAxisHistory);
		histories.add(gravityADevZAxisHistory);

		accelWikiXAxisHistory = new LinkedList<Number>();
		accelWikiYAxisHistory = new LinkedList<Number>();
		accelWikiZAxisHistory = new LinkedList<Number>();

		histories.add(accelWikiXAxisHistory);
		histories.add(accelWikiYAxisHistory);
		histories.add(accelWikiZAxisHistory);

		accelADevXAxisHistory = new LinkedList<Number>();
		accelADevYAxisHistory = new LinkedList<Number>();
		accelADevZAxisHistory = new LinkedList<Number>();

		histories.add(accelADevXAxisHistory);
		histories.add(accelADevYAxisHistory);
		histories.add(accelADevZAxisHistory);

		rawXAxisHistory = new LinkedList<Number>();
		rawYAxisHistory = new LinkedList<Number>();
		rawZAxisHistory = new LinkedList<Number>();

		histories.add(rawXAxisHistory);
		histories.add(rawYAxisHistory);
		histories.add(rawZAxisHistory);

		rawXAxisSeries = new SimpleXYSeries("xRaw");
		rawYAxisSeries = new SimpleXYSeries("yRaw");
		rawZAxisSeries = new SimpleXYSeries("zRaw");

		gravityWikiXAxisSeries = new SimpleXYSeries("xGrav");
		gravityWikiYAxisSeries = new SimpleXYSeries("yGrav");
		gravityWikiZAxisSeries = new SimpleXYSeries("zGrav");

		accelWikiXAxisSeries = new SimpleXYSeries("xAccel");
		accelWikiYAxisSeries = new SimpleXYSeries("yAccel");
		accelWikiZAxisSeries = new SimpleXYSeries("zAccel");

		gravityADevXAxisSeries = new SimpleXYSeries("xGrav");
		gravityADevYAxisSeries = new SimpleXYSeries("yGrav");
		gravityADevZAxisSeries = new SimpleXYSeries("zGrav");

		accelADevXAxisSeries = new SimpleXYSeries("xAccel");
		accelADevYAxisSeries = new SimpleXYSeries("yAccel");
		accelADevZAxisSeries = new SimpleXYSeries("zAccel");

		this.dynamicAPlot = dynamicAPlot;

		dynamicAPlot.setRangeBoundaries(minRange, maxRange, BoundaryMode.FIXED);
		dynamicAPlot.setDomainBoundaries(0, windowSize, BoundaryMode.FIXED);

		dynamicAPlot.addSeries(
				rawXAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(0, 0, 255), Color.rgb(0, 0,
						255), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				rawYAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(0, 255, 0), Color.rgb(0,
						255, 0), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				rawZAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(255, 0, 0), Color.rgb(255,
						0, 0), Color.TRANSPARENT));

		dynamicAPlot.addSeries(
				gravityWikiXAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(100, 100, 255), Color.rgb(
						100, 100, 255), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				gravityWikiYAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(100, 255, 100), Color.rgb(
						100, 255, 100), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				gravityWikiZAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(255, 100, 100), Color.rgb(
						255, 100, 100), Color.TRANSPARENT));

		dynamicAPlot.addSeries(
				accelWikiXAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(200, 200, 255), Color.rgb(
						200, 200, 255), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				accelWikiYAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(200, 255, 200), Color.rgb(
						200, 255, 200), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				accelWikiZAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(255, 200, 200), Color.rgb(
						255, 200, 200), Color.TRANSPARENT));

		dynamicAPlot.addSeries(
				gravityADevXAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(100, 100, 255), Color.rgb(
						100, 100, 255), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				gravityADevYAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(100, 255, 100), Color.rgb(
						100, 255, 100), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				gravityADevZAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(255, 100, 100), Color.rgb(
						255, 100, 100), Color.TRANSPARENT));

		dynamicAPlot.addSeries(
				accelADevXAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(200, 200, 255), Color.rgb(
						200, 200, 255), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				accelADevYAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(200, 255, 200), Color.rgb(
						200, 255, 200), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				accelADevZAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(255, 200, 200), Color.rgb(
						255, 200, 200), Color.TRANSPARENT));

		dynamicAPlot.setDomainStepValue(1);
		dynamicAPlot.setTicksPerRangeLabel(3);
		dynamicAPlot.setDomainLabel(".1/Sec");
		dynamicAPlot.getDomainLabelWidget().pack();
		dynamicAPlot.setRangeLabel("E");
		dynamicAPlot.getRangeLabelWidget().pack();
		dynamicAPlot.disableAllMarkup();
	}

	public double getMaxRange()
	{
		return maxRange;
	}

	public double getMinRange()
	{
		return minRange;
	}

	public int getWindowSize()
	{
		return windowSize;
	}

	public boolean isDrawXAxis()
	{
		return drawXAxis;
	}

	public boolean isDrawYAxis()
	{
		return drawYAxis;
	}

	public boolean isDrawZAxis()
	{
		return drawZAxis;
	}

	public boolean isDrawRaw()
	{
		return drawRaw;
	}

	public boolean isDrawGravity()
	{
		return drawGravity;
	}

	public boolean isDrawAccel()
	{
		return drawAccel;
	}

	public boolean isDrawWikiLP()
	{
		return drawWikiLP;
	}

	public boolean isDrawAndDevLP()
	{
		return drawAndDevLP;
	}

	public void setDrawXAxis(boolean drawXAxis)
	{
		if (!drawXAxis)
		{
			accelWikiXAxisHistory.removeAll(accelWikiXAxisHistory);
			gravityWikiXAxisHistory.removeAll(gravityWikiXAxisHistory);

			accelADevXAxisHistory.removeAll(accelADevXAxisHistory);
			gravityADevXAxisHistory.removeAll(gravityADevXAxisHistory);

			rawXAxisHistory.removeAll(rawXAxisHistory);
		}

		this.drawXAxis = drawXAxis;
	}

	public void setDrawYAxis(boolean drawYAxis)
	{
		if (!drawYAxis)
		{
			accelWikiYAxisHistory.removeAll(accelWikiYAxisHistory);
			gravityWikiYAxisHistory.removeAll(gravityWikiYAxisHistory);

			accelADevYAxisHistory.removeAll(accelADevYAxisHistory);
			gravityADevYAxisHistory.removeAll(gravityADevYAxisHistory);

			rawYAxisHistory.removeAll(rawYAxisHistory);
		}

		this.drawYAxis = drawYAxis;
	}

	public void setDrawZAxis(boolean drawZAxis)
	{
		if (!drawZAxis)
		{
			accelWikiZAxisHistory.removeAll(accelWikiZAxisHistory);
			gravityWikiZAxisHistory.removeAll(gravityWikiZAxisHistory);

			accelADevZAxisHistory.removeAll(accelADevZAxisHistory);
			gravityADevZAxisHistory.removeAll(gravityADevZAxisHistory);

			rawZAxisHistory.removeAll(rawZAxisHistory);
		}

		this.drawZAxis = drawZAxis;
	}

	public void setDrawRaw(boolean drawRaw)
	{
		if (!drawRaw)
		{
			rawXAxisHistory.removeAll(rawXAxisHistory);
			rawYAxisHistory.removeAll(rawYAxisHistory);
			rawZAxisHistory.removeAll(rawZAxisHistory);
		}

		this.drawRaw = drawRaw;
	}

	public void setDrawGravity(boolean drawGravity)
	{
		if (!drawGravity)
		{
			gravityADevXAxisHistory.removeAll(gravityADevXAxisHistory);
			gravityADevYAxisHistory.removeAll(gravityADevYAxisHistory);
			gravityADevZAxisHistory.removeAll(gravityADevZAxisHistory);

			gravityWikiXAxisHistory.removeAll(gravityWikiXAxisHistory);
			gravityWikiYAxisHistory.removeAll(gravityWikiYAxisHistory);
			gravityWikiZAxisHistory.removeAll(gravityWikiZAxisHistory);
		}

		this.drawGravity = drawGravity;
	}

	public void setDrawAccel(boolean drawAccel)
	{
		if (!drawAccel)
		{
			accelADevXAxisHistory.removeAll(accelADevXAxisHistory);
			accelADevYAxisHistory.removeAll(accelADevYAxisHistory);
			accelADevZAxisHistory.removeAll(accelADevZAxisHistory);

			accelWikiXAxisHistory.removeAll(accelWikiXAxisHistory);
			accelWikiYAxisHistory.removeAll(accelWikiYAxisHistory);
			accelWikiZAxisHistory.removeAll(accelWikiZAxisHistory);
		}

		this.drawAccel = drawAccel;
	}

	public void setDrawWikiLP(boolean drawWikiLP)
	{
		if (!drawWikiLP)
		{
			gravityWikiXAxisHistory.removeAll(gravityWikiXAxisHistory);
			gravityWikiYAxisHistory.removeAll(gravityWikiYAxisHistory);
			gravityWikiZAxisHistory.removeAll(gravityWikiZAxisHistory);

			accelWikiXAxisHistory.removeAll(accelWikiXAxisHistory);
			accelWikiYAxisHistory.removeAll(accelWikiYAxisHistory);
			accelWikiZAxisHistory.removeAll(accelWikiZAxisHistory);
		}

		this.drawWikiLP = drawWikiLP;
	}

	public void setDrawAndDevLP(boolean drawAndDevLP)
	{
		if (!drawAndDevLP)
		{
			gravityADevXAxisHistory.removeAll(gravityADevXAxisHistory);
			gravityADevYAxisHistory.removeAll(gravityADevYAxisHistory);
			gravityADevZAxisHistory.removeAll(gravityADevZAxisHistory);

			accelADevXAxisHistory.removeAll(accelADevXAxisHistory);
			accelADevYAxisHistory.removeAll(accelADevYAxisHistory);
			accelADevZAxisHistory.removeAll(accelADevZAxisHistory);
		}

		this.drawAndDevLP = drawAndDevLP;
	}

	public void setMaxRange(double maxRange)
	{
		this.maxRange = maxRange;
		dynamicAPlot.setRangeBoundaries(minRange, maxRange, BoundaryMode.FIXED);
	}

	public void setMinRange(double minRange)
	{
		this.minRange = minRange;
		dynamicAPlot.setRangeBoundaries(minRange, maxRange, BoundaryMode.FIXED);
	}

	public void setWindowSize(int windowSize)
	{
		this.windowSize = windowSize;
	}

	/**
	 * Set the acceleration data.
	 * 
	 * @param ax
	 *            the x-axis.
	 * @param ay
	 *            the y-axis.
	 * @param az
	 *            the z-axis.
	 */
	public void setData(float[] raw, float[] gravityWiki,
			float[] linearAccelWiki, float[] gravityADev,
			float[] linearAccelADev)
	{
		for (int i = 0; i < histories.size(); i++)
		{
			enforceWindowLimit(histories.get(i));
		}

		if (drawXAxis)
		{
			if (drawAccel)
			{
				if (drawWikiLP)
				{
					accelWikiXAxisHistory.addLast(linearAccelWiki[0]);
				}
				if (drawAndDevLP)
				{
					accelADevXAxisHistory.addLast(linearAccelADev[0]);
				}
			}
			if (drawGravity)
			{
				if (drawWikiLP)
				{
					gravityWikiXAxisHistory.addLast(gravityWiki[0]);
				}
				if (drawAndDevLP)
				{
					gravityADevXAxisHistory.addLast(gravityADev[0]);
				}
			}
			if (drawRaw)
			{
				rawXAxisHistory.addLast(raw[0]);
			}
		}

		if (drawYAxis)
		{
			if (drawAccel)
			{
				if (drawWikiLP)
				{
					accelWikiYAxisHistory.addLast(linearAccelWiki[1]);
				}
				if (drawAndDevLP)
				{
					accelADevYAxisHistory.addLast(linearAccelADev[1]);
				}
			}
			if (drawGravity)
			{
				if (drawWikiLP)
				{
					gravityWikiYAxisHistory.addLast(gravityWiki[1]);
				}
				if (drawAndDevLP)
				{
					gravityADevYAxisHistory.addLast(gravityADev[1]);
				}
			}
			if (drawRaw)
			{
				rawYAxisHistory.addLast(raw[1]);
			}
		}

		if (drawZAxis)
		{
			if (drawAccel)
			{
				if (drawWikiLP)
				{
					accelWikiZAxisHistory.addLast(linearAccelWiki[2]);
				}
				if (drawAndDevLP)
				{
					accelADevZAxisHistory.addLast(linearAccelADev[2]);
				}
			}
			if (drawGravity)
			{
				if (drawWikiLP)
				{
					gravityWikiZAxisHistory.addLast(gravityWiki[2]);
				}
				if (drawAndDevLP)
				{
					gravityADevZAxisHistory.addLast(gravityADev[2]);
				}
			}
			if (drawRaw)
			{
				rawZAxisHistory.addLast(raw[2]);
			}
		}

		accelWikiXAxisSeries.setModel(accelWikiXAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		accelWikiYAxisSeries.setModel(accelWikiYAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		accelWikiZAxisSeries.setModel(accelWikiZAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

		gravityWikiXAxisSeries.setModel(gravityWikiXAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		gravityWikiYAxisSeries.setModel(gravityWikiYAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		gravityWikiZAxisSeries.setModel(gravityWikiZAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

		accelADevXAxisSeries.setModel(accelADevXAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		accelADevYAxisSeries.setModel(accelADevYAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		accelADevZAxisSeries.setModel(accelADevZAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

		gravityADevXAxisSeries.setModel(gravityADevXAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		gravityADevYAxisSeries.setModel(gravityADevYAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		gravityADevZAxisSeries.setModel(gravityADevZAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

		rawXAxisSeries.setModel(rawXAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		rawYAxisSeries.setModel(rawYAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		rawZAxisSeries.setModel(rawZAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

		dynamicAPlot.redraw();
	}

	private void enforceWindowLimit(LinkedList<Number> data)
	{
		if (data.size() > windowSize)
		{
			data.removeFirst();
		}
	}
}
