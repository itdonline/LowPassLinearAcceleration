package com.kircherelectronics.lowpasslinearacceleration;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.kircherelectronics.lowpasslinearacceleration.dialog.FilterSettingsDialog;
import com.kircherelectronics.lowpasslinearacceleration.dialog.SensorSettingsDialog;
import com.kircherelectronics.lowpasslinearacceleration.filter.LowPassFilter;
import com.kircherelectronics.lowpasslinearacceleration.plot.PlotPrefCallback;
import com.kircherelectronics.lowpasslinearacceleration.prefs.PrefUtils;
import com.kircherelectronics.lowpasslinearacceleration.view.AccelerationVectorView;

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
 * Draws a two dimensional vector of the acceleration sensors measurements.
 * 
 * @author Kaleb
 * 
 */
public class AccelerationVectorActivity extends Activity implements
		SensorEventListener, PlotPrefCallback
{
	private boolean invertAxisActive = false;

	private float lpfTimeConstant = 1;

	private float[] acceleration = new float[3];

	private AccelerationVectorView view;

	private LowPassFilter lpf;

	private FilterSettingsDialog filterSettingsDialog;
	private SensorSettingsDialog sensorSettingsDialog;

	// Sensor manager to access the accelerometer sensor
	private SensorManager sensorManager;

	private String frequencySelection;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.acceleration_vector_activity);

		view = (AccelerationVectorView) findViewById(R.id.vector_acceleration);

		sensorManager = (SensorManager) this
				.getSystemService(Context.SENSOR_SERVICE);

		lpf = new LowPassFilter();
		lpf.setTimeConstant(lpfTimeConstant);

		readFilterPrefs();
		readSensorPrefs();
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		System.arraycopy(event.values, 0, acceleration, 0, event.values.length);

		// Invert the axes if desired.
		if (this.invertAxisActive)
		{
			acceleration[0] = -acceleration[0];
			acceleration[1] = -acceleration[1];
			acceleration[2] = -acceleration[2];
		}

		acceleration = lpf.addSamples(acceleration);

		view.updatePoint(acceleration[0], acceleration[1]);
	}

	@Override
	public void onPause()
	{
		super.onPause();

		sensorManager.unregisterListener(this);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		lpf.reset();

		updateSensorDelay();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings_vector_menu, menu);
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
		case R.id.action_plot:
			Intent plotIntent = new Intent(this,
					LinearAccelerationLPFActivity.class);
			startActivity(plotIntent);
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

	@Override
	public void checkPlotPrefs()
	{
		readFilterPrefs();
		readSensorPrefs();

		updateSensorDelay();
	}

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

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1)
	{
		// TODO Auto-generated method stub

	}

}
