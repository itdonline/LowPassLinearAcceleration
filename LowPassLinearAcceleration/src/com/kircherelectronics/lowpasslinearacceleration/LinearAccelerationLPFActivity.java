package com.kircherelectronics.lowpasslinearacceleration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.XYPlot;
import com.kircherelectronics.lowpasslinearacceleration.dialog.FilterSettingsDialog;
import com.kircherelectronics.lowpasslinearacceleration.dialog.SensorSettingsDialog;
import com.kircherelectronics.lowpasslinearacceleration.filter.LowPassFilter;
import com.kircherelectronics.lowpasslinearacceleration.gauge.GaugeAcceleration;
import com.kircherelectronics.lowpasslinearacceleration.gauge.GaugeRotation;
import com.kircherelectronics.lowpasslinearacceleration.plot.DynamicLinePlot;
import com.kircherelectronics.lowpasslinearacceleration.plot.PlotColor;
import com.kircherelectronics.lowpasslinearacceleration.plot.PlotPrefCallback;
import com.kircherelectronics.lowpasslinearacceleration.prefs.PrefUtils;

/*
 * Low-Pass Linear Acceleration
 * Copyright (C) 2013, Kaleb Kircher - Kircher Engineering, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Implements an Activity that is intended to run low-pass filters on
 * accelerometer inputs in an attempt to find the gravity and linear
 * acceleration components of the accelerometer signal. This is accomplished by
 * using a low-pass filter to filter out signals that are shorter than a
 * pre-determined period. The result is only the long term signal, or gravity,
 * which can then be subtracted from the acceleration to find the linear
 * acceleration.
 * 
 * Currently supports one version of IIR digital low-pass filter. The low-pass
 * filters are classified as recursive, or infinite response filters (IIR). The
 * current, nth sample output depends on both current and previous inputs as
 * well as previous outputs. It is essentially a weighted moving average, which
 * comes in many different flavors depending on the values for the coefficients,
 * a and b.
 * 
 * The low-pass filter is an IIR single-pole implementation. The coefficient, a
 * (alpha), can be adjusted based on the sample period of the sensor to produce
 * the desired time constant that the filter will act on. The time constant is
 * user definable. The LPF takes a simple form of y[0] = alpha * y[0] + (1 -
 * alpha) * x[0]. Alpha is defined as alpha = timeConstant / (timeConstant + dt)
 * where the time constant is the length of signals the filter should act on and
 * dt is the sample period (1/frequency) of the sensor.
 * 
 * @author Kaleb
 * @version %I%, %G%
 */
public class LinearAccelerationLPFActivity extends Activity implements
		SensorEventListener, Runnable, OnTouchListener, PlotPrefCallback
{

	private static final String tag = LinearAccelerationLPFActivity.class
			.getSimpleName();

	// Plot keys for the acceleration plot
	private final static int PLOT_ACCEL_X_AXIS_KEY = 0;
	private final static int PLOT_ACCEL_Y_AXIS_KEY = 1;
	private final static int PLOT_ACCEL_Z_AXIS_KEY = 2;

	// Plot keys for the LPF Android Developer plot
	private final static int PLOT_LPF_AND_DEV_X_AXIS_KEY = 3;
	private final static int PLOT_LPF_AND_DEV_Y_AXIS_KEY = 4;
	private final static int PLOT_LPF_AND_DEV_Z_AXIS_KEY = 5;

	private boolean plotLPFReady = false;

	private boolean invertAxisActive = false;

	// Indicate if the output should be logged to a .csv file
	private boolean logData = false;

	// Touch to zoom constants for the dynamicPlot
	private float distance = 0;
	private float zoom = 1.2f;

	private float lpfTimeConstant = 1;

	// Outputs for the acceleration and LPFs
	private float[] rawAcceleration = new float[3];
	private float[] lpfAcceleration = new float[3];

	// The generation of the log output
	private int generation = 0;

	// Color keys for the acceleration plot
	private int plotAccelXAxisColor;
	private int plotAccelYAxisColor;
	private int plotAccelZAxisColor;

	// Color keys for the LPF Android Developer plot
	private int plotLPFAndDevXAxisColor;
	private int plotLPFAndDevYAxisColor;
	private int plotLPFAndDevZAxisColor;

	// Log output time stamp
	private long logTime = 0;

	// Decimal formats for the UI outputs
	private DecimalFormat df;
	private DecimalFormat dfLong;

	// Graph plot for the UI outputs
	private DynamicLinePlot dynamicPlot;

	// The Acceleration Gauge
	private GaugeRotation gaugeAccelerationTilt;

	// The LPF Gauge
	private GaugeRotation gaugeLPFTilt;

	private GaugeAcceleration gaugeAcceleration;

	private GaugeAcceleration gaugeLPFAcceleration;

	// Handler for the UI plots so everything plots smoothly
	private Handler handler;

	// Icon to indicate logging is active
	private ImageView iconLogger;

	private LowPassFilter lpf;

	// Plot colors
	private PlotColor color;

	// Sensor manager to access the accelerometer sensor
	private SensorManager sensorManager;

	private FilterSettingsDialog filterSettingsDialog;
	private SensorSettingsDialog sensorSettingsDialog;

	// Acceleration plot titles
	private String plotAccelXAxisTitle = "AX";
	private String plotAccelYAxisTitle = "AY";
	private String plotAccelZAxisTitle = "AZ";

	// LPF Android Developer plot tiltes
	private String plotLPFAndDevXAxisTitle = "LPFX";
	private String plotLPFAndDevYAxisTitle = "LPFY";
	private String plotLPFAndDevZAxisTitle = "LPFZ";

	private String frequencySelection;

	// Output log
	private String log;

	// Acceleration UI outputs
	private TextView xAxis;
	private TextView yAxis;
	private TextView zAxis;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.plot_sensor_activity);

		TextView accelerationLable = (TextView) this
				.findViewById(R.id.label_acceleration_name_0);
		accelerationLable.setText("Acceleration");

		TextView lpfLable = (TextView) this
				.findViewById(R.id.label_acceleration_name_1);
		lpfLable.setText("LPF");

		// Get the sensor manager ready
		sensorManager = (SensorManager) this
				.getSystemService(Context.SENSOR_SERVICE);

		initTextViewOutputs();

		initIcons();

		initFilters();

		// Read in the saved prefs
		readFilterPrefs();

		// Initialize the plots
		initColor();
		initPlots();
		initGauges();

		handler = new Handler();
	}

	/**
	 * A callback from the dialogs to update the user preferences.
	 * 
	 */
	@Override
	public void checkPlotPrefs()
	{
		readFilterPrefs();
		readSensorPrefs();

		updateSensorDelay();
	}

	@Override
	public void onPause()
	{
		super.onPause();

		sensorManager.unregisterListener(this);

		if (logData)
		{
			writeLogToFile();
		}

		handler.removeCallbacks(this);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		lpf.reset();

		readFilterPrefs();
		readSensorPrefs();

		handler.post(this);

		updateSensorDelay();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		// Get a local copy of the sensor values
		System.arraycopy(event.values, 0, rawAcceleration, 0,
				event.values.length);

		if (!invertAxisActive)
		{
			rawAcceleration[0] = rawAcceleration[0]
					/ SensorManager.GRAVITY_EARTH;
			rawAcceleration[1] = rawAcceleration[1]
					/ SensorManager.GRAVITY_EARTH;
			rawAcceleration[2] = rawAcceleration[2]
					/ SensorManager.GRAVITY_EARTH;
		}
		else
		{
			rawAcceleration[0] = -rawAcceleration[0]
					/ SensorManager.GRAVITY_EARTH;
			rawAcceleration[1] = -rawAcceleration[1]
					/ SensorManager.GRAVITY_EARTH;
			rawAcceleration[2] = -rawAcceleration[2]
					/ SensorManager.GRAVITY_EARTH;
		}

		lpfAcceleration = lpf.addSamples(rawAcceleration);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings_logger_menu, menu);
		return true;
	}

	/**
	 * Event Handling for Individual menu item selected Identify single menu
	 * item by it's id
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{

		// Log the data
		case R.id.action_log_data:
			startDataLog();
			return true;

			// Start the vector activity
		case R.id.action_vector_view:
			Intent vectorIntent = new Intent(this,
					AccelerationVectorActivity.class);
			startActivity(vectorIntent);
			return true;

			// Log the data
		case R.id.action_settings_filter:
			showFilterSettingsDialog();
			return true;

			// Log the data
		case R.id.action_settings_sensor:
			showSensorSettingsDialog();
			return true;

			// Log the data
		case R.id.action_help:
			showHelpDialog();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Pinch to zoom.
	 */
	@Override
	public boolean onTouch(View v, MotionEvent e)
	{
		// MotionEvent reports input details from the touch screen
		// and other input controls.
		float newDist = 0;

		switch (e.getAction())
		{

		case MotionEvent.ACTION_MOVE:

			// pinch to zoom
			if (e.getPointerCount() == 2)
			{
				if (distance == 0)
				{
					distance = fingerDist(e);
				}

				newDist = fingerDist(e);

				zoom *= distance / newDist;

				dynamicPlot.setMaxRange(zoom * Math.log(zoom));
				dynamicPlot.setMinRange(-zoom * Math.log(zoom));

				distance = newDist;
			}
		}

		return false;
	}

	/**
	 * Output and logs are run on their own thread to keep the UI from hanging
	 * and the output smooth.
	 */
	@Override
	public void run()
	{
		handler.postDelayed(this, 100);

		plotData();
		updateTextViewOutputs();
		logData();
	}

	/**
	 * Create the output graph line chart.
	 */
	private void addAccelerationPlot()
	{
		addPlot(plotAccelXAxisTitle, PLOT_ACCEL_X_AXIS_KEY, plotAccelXAxisColor);
		addPlot(plotAccelYAxisTitle, PLOT_ACCEL_Y_AXIS_KEY, plotAccelYAxisColor);
		addPlot(plotAccelZAxisTitle, PLOT_ACCEL_Z_AXIS_KEY, plotAccelZAxisColor);
	}

	/**
	 * Add the LPF plot.
	 */
	private void addLPFPlot()
	{
		addPlot(plotLPFAndDevXAxisTitle, PLOT_LPF_AND_DEV_X_AXIS_KEY,
				plotLPFAndDevXAxisColor);
		addPlot(plotLPFAndDevYAxisTitle, PLOT_LPF_AND_DEV_Y_AXIS_KEY,
				plotLPFAndDevYAxisColor);
		addPlot(plotLPFAndDevZAxisTitle, PLOT_LPF_AND_DEV_Z_AXIS_KEY,
				plotLPFAndDevZAxisColor);

		plotLPFReady = true;
	}

	/**
	 * Add a plot to the graph.
	 * 
	 * @param title
	 *            The name of the plot.
	 * @param key
	 *            The unique plot key
	 * @param color
	 *            The color of the plot
	 */
	private void addPlot(String title, int key, int color)
	{
		dynamicPlot.addSeriesPlot(title, key, color);
	}

	/**
	 * Show the help dialog.
	 */
	private void showHelpDialog()
	{
		Dialog helpDialog = new Dialog(this);
		helpDialog.setCancelable(true);
		helpDialog.setCanceledOnTouchOutside(true);

		helpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		helpDialog.setContentView(getLayoutInflater().inflate(
				R.layout.help_dialog_view, null));

		helpDialog.show();
	}

	/**
	 * Show a settings dialog.
	 */
	private void showFilterSettingsDialog()
	{
		if (filterSettingsDialog == null)
		{
			filterSettingsDialog = new FilterSettingsDialog(this, this);
			filterSettingsDialog.setCancelable(true);
			filterSettingsDialog.setCanceledOnTouchOutside(true);
		}

		filterSettingsDialog.show();
	}

	/**
	 * Show a settings dialog.
	 */
	private void showSensorSettingsDialog()
	{
		if (sensorSettingsDialog == null)
		{
			sensorSettingsDialog = new SensorSettingsDialog(this, this);
			sensorSettingsDialog.setCancelable(true);
			sensorSettingsDialog.setCanceledOnTouchOutside(true);
		}

		sensorSettingsDialog.show();
	}

	/**
	 * Create the plot colors.
	 */
	private void initColor()
	{
		color = new PlotColor(this);

		plotAccelXAxisColor = color.getDarkBlue();
		plotAccelYAxisColor = color.getDarkGreen();
		plotAccelZAxisColor = color.getDarkRed();

		plotLPFAndDevXAxisColor = color.getLightBlue();
		plotLPFAndDevYAxisColor = color.getLightGreen();
		plotLPFAndDevZAxisColor = color.getLightRed();
	}

	/**
	 * Initialize the filters.
	 */
	private void initFilters()
	{
		lpf = new LowPassFilter();
		lpf.setTimeConstant(lpfTimeConstant);
	}

	/**
	 * Create the RMS Noise bar chart.
	 */
	private void initGauges()
	{
		gaugeAccelerationTilt = (GaugeRotation) findViewById(R.id.gauge_rotation_0);
		gaugeLPFTilt = (GaugeRotation) findViewById(R.id.gauge_rotation_1);

		gaugeAcceleration = (GaugeAcceleration) findViewById(R.id.gauge_acceleration_0);
		gaugeLPFAcceleration = (GaugeAcceleration) findViewById(R.id.gauge_acceleration_1);
	}

	/**
	 * Initialize the icons.
	 */
	private void initIcons()
	{
		// Create the logger icon
		iconLogger = (ImageView) findViewById(R.id.icon_logger);
		iconLogger.setVisibility(View.INVISIBLE);
	}

	/**
	 * Initialize the plots.
	 */
	private void initPlots()
	{
		View view = findViewById(R.id.plot_layout);
		view.setOnTouchListener(this);

		// Create the graph plot
		XYPlot plot = (XYPlot) findViewById(R.id.plot_sensor);
		plot.setTitle("Acceleration");
		dynamicPlot = new DynamicLinePlot(plot);
		dynamicPlot.setMaxRange(1.2);
		dynamicPlot.setMinRange(-1.2);

		addAccelerationPlot();
		addLPFPlot();
	}

	/**
	 * Initialize the Text View Sensor Outputs.
	 */
	private void initTextViewOutputs()
	{
		// Create the acceleration UI outputs
		xAxis = (TextView) findViewById(R.id.value_x_axis);
		yAxis = (TextView) findViewById(R.id.value_y_axis);
		zAxis = (TextView) findViewById(R.id.value_z_axis);

		// Format the UI outputs so they look nice
		df = new DecimalFormat("#.##");
		dfLong = new DecimalFormat("#.####");
	}

	/**
	 * Remove the Android Developer LPF plot.
	 */
	private void removeLPFPlot()
	{
		plotLPFReady = false;

		removePlot(PLOT_LPF_AND_DEV_X_AXIS_KEY);
		removePlot(PLOT_LPF_AND_DEV_Y_AXIS_KEY);
		removePlot(PLOT_LPF_AND_DEV_Z_AXIS_KEY);
	}

	/**
	 * Remove a plot from the graph.
	 * 
	 * @param key
	 */
	private void removePlot(int key)
	{
		dynamicPlot.removeSeriesPlot(key);
	}

	/**
	 * Begin logging data to an external .csv file.
	 */
	private void startDataLog()
	{
		if (logData == false)
		{
			CharSequence text = "Logging Data";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this, text, duration);
			toast.show();

			String headers = "Generation" + ",";

			headers += "Timestamp" + ",";

			headers += this.plotAccelXAxisTitle + ",";

			headers += this.plotAccelYAxisTitle + ",";

			headers += this.plotAccelZAxisTitle + ",";

			headers += this.plotLPFAndDevXAxisTitle + ",";

			headers += this.plotLPFAndDevYAxisTitle + ",";

			headers += this.plotLPFAndDevZAxisTitle + ",";

			log = headers + "\n";

			iconLogger.setVisibility(View.VISIBLE);

			logData = true;
		}
		else
		{
			iconLogger.setVisibility(View.INVISIBLE);

			logData = false;
			writeLogToFile();
		}
	}

	/**
	 * Plot the output data in the UI.
	 */
	private void plotData()
	{
		dynamicPlot.setData(rawAcceleration[0], PLOT_ACCEL_X_AXIS_KEY);
		dynamicPlot.setData(rawAcceleration[1], PLOT_ACCEL_Y_AXIS_KEY);
		dynamicPlot.setData(rawAcceleration[2], PLOT_ACCEL_Z_AXIS_KEY);

		gaugeAccelerationTilt.updateRotation(rawAcceleration);

		gaugeAcceleration.updatePoint(rawAcceleration[0]
				* SensorManager.GRAVITY_EARTH, rawAcceleration[1]
				* SensorManager.GRAVITY_EARTH, Color.parseColor("#33b5e5"));

		if (plotLPFReady)
		{
			dynamicPlot
					.setData(lpfAcceleration[0], PLOT_LPF_AND_DEV_X_AXIS_KEY);
			dynamicPlot
					.setData(lpfAcceleration[1], PLOT_LPF_AND_DEV_Y_AXIS_KEY);
			dynamicPlot
					.setData(lpfAcceleration[2], PLOT_LPF_AND_DEV_Z_AXIS_KEY);

			gaugeLPFTilt.updateRotation(lpfAcceleration);

			gaugeLPFAcceleration.updatePoint(lpfAcceleration[0]
					* SensorManager.GRAVITY_EARTH, lpfAcceleration[1]
					* SensorManager.GRAVITY_EARTH, Color.parseColor("#33b5e5"));
		}

		dynamicPlot.draw();

	}

	private void updateTextViewOutputs()
	{
		// Update the view with the new acceleration data
		xAxis.setText(df.format(rawAcceleration[0]));
		yAxis.setText(df.format(rawAcceleration[1]));
		zAxis.setText(df.format(rawAcceleration[2]));
	}

	/**
	 * Log output data to an external .csv file.
	 */
	private void logData()
	{
		if (logData)
		{
			if (generation == 0)
			{
				logTime = System.currentTimeMillis();
			}

			log += System.getProperty("line.separator");
			log += generation++ + ",";
			log += System.currentTimeMillis() - logTime + ",";

			log += rawAcceleration[0] + ",";
			log += rawAcceleration[1] + ",";
			log += rawAcceleration[2] + ",";

			log += lpfAcceleration[0] + ",";
			log += lpfAcceleration[1] + ",";
			log += lpfAcceleration[2] + ",";
		}
	}

	/**
	 * Write the logged data out to a persisted file.
	 */
	private void writeLogToFile()
	{
		Calendar c = Calendar.getInstance();
		String filename = "LPFLinearAcceleration-" + c.get(Calendar.YEAR) + "-"
				+ (c.get(Calendar.MONTH) + 1) + "-"
				+ c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.HOUR)
				+ "-" + c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND)
				+ ".csv";

		File dir = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "LPFLinearAcceleration" + File.separator
				+ "Logs");
		if (!dir.exists())
		{
			dir.mkdirs();
		}

		File file = new File(dir, filename);

		FileOutputStream fos;
		byte[] data = log.getBytes();
		try
		{
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
			fos.close();

			CharSequence text = "Log Saved";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
		}
		catch (FileNotFoundException e)
		{
			CharSequence text = e.toString();
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
		}
		catch (IOException e)
		{
			// handle exception
		}
		finally
		{
			// Update the MediaStore so we can view the file without rebooting.
			// Note that it appears that the ACTION_MEDIA_MOUNTED approach is
			// now blocked for non-system apps on Android 4.4.
			MediaScannerConnection.scanFile(this, new String[]
			{ file.getPath() }, null,
					new MediaScannerConnection.OnScanCompletedListener()
					{
						@Override
						public void onScanCompleted(final String path,
								final Uri uri)
						{

						}
					});
		}
	}

	/**
	 * Get the distance between fingers for the touch to zoom.
	 * 
	 * @param event
	 * @return
	 */
	private final float fingerDist(MotionEvent event)
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * Read in the current user preferences.
	 */
	private void readFilterPrefs()
	{
		SharedPreferences prefs = this.getSharedPreferences(
				PrefUtils.FILTER_PREFS, Activity.MODE_PRIVATE);

		this.invertAxisActive = prefs.getBoolean(PrefUtils.INVERT_AXIS_ACTIVE,
				false);

		this.lpfTimeConstant = prefs.getFloat(PrefUtils.LPF_TIME_CONSTANT, 1);

		lpf.setTimeConstant(lpfTimeConstant);
	}

	/**
	 * Read in the current user preferences.
	 */
	private void readSensorPrefs()
	{
		SharedPreferences prefs = this.getSharedPreferences(
				PrefUtils.SENSOR_PREFS, Activity.MODE_PRIVATE);

		this.frequencySelection = prefs.getString(
				PrefUtils.SENSOR_FREQUENCY_PREF,
				PrefUtils.SENSOR_FREQUENCY_FAST);
	}

	/**
	 * Set the sensor delay based on user preferences. 0 = slow, 1 = medium, 2 =
	 * fast.
	 * 
	 * @param position
	 *            The desired sensor delay.
	 */
	private void setSensorDelay(int position)
	{
		switch (position)
		{
		case 0:

			sensorManager.unregisterListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));

			// Register for sensor updates.
			sensorManager.registerListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_NORMAL);
			break;
		case 1:

			sensorManager.unregisterListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));

			// Register for sensor updates.
			sensorManager.registerListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_GAME);
			break;
		case 2:

			sensorManager.unregisterListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));

			// Register for sensor updates.
			sensorManager.registerListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_FASTEST);
			break;
		}
		
		lpf.reset();
	}

	/**
	 * Updates the sensor delay based on the user preference. 0 = slow, 1 =
	 * medium, 2 = fast.
	 */
	private void updateSensorDelay()
	{
		if (frequencySelection.equals(PrefUtils.SENSOR_FREQUENCY_SLOW))
		{
			setSensorDelay(0);
		}

		if (frequencySelection.equals(PrefUtils.SENSOR_FREQUENCY_MEDIUM))
		{
			setSensorDelay(1);
		}

		if (frequencySelection.equals(PrefUtils.SENSOR_FREQUENCY_FAST))
		{
			setSensorDelay(2);
		}
	}
}
