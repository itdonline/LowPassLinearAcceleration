package com.kircherelectronics.lowpasslinearacceleration;

import com.androidplot.xy.XYPlot;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Plots the acceleration in the x, y and z axes. The raw acceleration from the
 * device sensor can be plotted, or one of two low-pass filter implementations
 * can be used to separate the gravity and linear acceleration components of the
 * signal.
 * 
 * The first low-pass filter is based on the Wikipedia low-pass filter
 * algorithm. The second low-pass filter is based on the Android Developer
 * low-pass filter algorithm.
 * 
 * The user can vary the filter time constant dynamically. The user can also
 * chose what filters, signals and axes are plotted.
 * 
 * @author Kaleb@KircherElectronics
 * @version 1.0
 * @see http://developer.android.com/guide/topics/ui/controls/togglebutton.html
 * @see http://en.wikipedia.org/wiki/Low-pass_filter
 */
public class AccelerationActivity extends Activity implements
		SensorEventListener, OnSeekBarChangeListener, OnTouchListener
{
	// Constants for the low-pass filters
	private float timeConstant = 0.297f;
	private float alphaWiki = 0.1f;
	private float alphaADev = 0.9f;
	private float dt;

	// Timestamps for the low-pass filters
	private float timestamp;
	private float timestampOld;

	// Gravity and linear accelerations components for the
	// Wikipedia low-pass filter
	private float[] gravityWiki = new float[3];
	private float[] linearAccelerationWiki = new float[3];

	// Gravity and linear accelerations components for the
	// Android Developer low-pass filter
	private float[] gravityADev = new float[3];
	private float[] linearAccelerationADev = new float[3];

	// Raw accelerometer data
	private float[] input = new float[3];

	// Touch to zoom constants
	private float distance = 0;
	private float zoom = 10;

	private String tag = "LowPassLinearAccel";

	private SensorManager sensorManager;

	private TextView tvTimeConstant;
	private TextView tvAlphaWiki;
	private TextView tvAlphaADev;
	private TextView tvSamplePeroid;
	private TextView tvUpdateFrequency;

	// The Acceleration View
	private PlotView plotView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		View view = findViewById(R.id.ScrollView01);
		view.setOnTouchListener(this);

		SeekBar sb = (SeekBar) findViewById(R.id.slider);
		sb.setProgress(2970);
		sb.setOnSeekBarChangeListener(this);

		tvTimeConstant = (TextView) findViewById(R.id.timeConstant);
		tvTimeConstant
				.setText("Time Constant: " + Float.toString(timeConstant));

		tvAlphaWiki = (TextView) findViewById(R.id.alphaWiki);
		tvAlphaWiki.setText(Float.toString(alphaWiki));

		tvAlphaADev = (TextView) findViewById(R.id.alphaADev);
		tvAlphaADev.setText(Float.toString(alphaADev));

		tvSamplePeroid = (TextView) findViewById(R.id.samplePeriod);
		tvSamplePeroid.setText(Float.toString(0));

		tvUpdateFrequency = (TextView) findViewById(R.id.updateFrequency);
		tvUpdateFrequency.setText(Float.toString(0));

		plotView = new PlotView(
				(XYPlot) this.findViewById(R.id.dynamicLinePlot));
		plotView.setMaxRange(zoom);
		plotView.setMinRange(-zoom);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			timestamp = event.timestamp;

			// Make sure the timestamp for the event has changed.
			if (timestamp != timestampOld)
			{
				// Get a local copy of the sensor values
				System.arraycopy(event.values, 0, input, 0, event.values.length);

				// Find the sample period (between updates).
				// Convert from nanoseconds to seconds
				dt = (event.timestamp - timestampOld) / 1000000000.0f;

				tvSamplePeroid.setText("Sample Peroid: " + Float.toString(dt));
				tvUpdateFrequency.setText("Update Frequency: "
						+ Float.toString(1 / dt));

				// Calculate Wikipedia low-pass alpha
				alphaWiki = dt / (timeConstant + dt);

				tvAlphaWiki.setText("Alpha Wiki: " + Float.toString(alphaWiki));

				// Update the Wikipedia filter
				// y[i] = y[i] + alpha * (x[i] - y[i])
				gravityWiki[0] = gravityWiki[0] + alphaWiki
						* (input[0] - gravityWiki[0]);
				gravityWiki[1] = gravityWiki[1] + alphaWiki
						* (input[1] - gravityWiki[1]);
				gravityWiki[2] = gravityWiki[2] + alphaWiki
						* (input[2] - gravityWiki[2]);

				// Calculate the linear acceleration by subtracting gravity from
				// the input signal.
				linearAccelerationWiki[0] = input[0] - gravityWiki[0];
				linearAccelerationWiki[1] = input[1] - gravityWiki[1];
				linearAccelerationWiki[2] = input[2] - gravityWiki[2];

				// Calculate Android Developer low-pass alpha
				alphaADev = timeConstant / (timeConstant + dt);

				tvAlphaADev.setText("Alpha ADev: " + Float.toString(alphaADev));

				// Update the Android Developer low-pass filter
				// y[i] = y[i] * alpha + (1 - alpha) * x[i]
				gravityADev[0] = alphaADev * gravityADev[0] + (1 - alphaADev)
						* input[0];
				gravityADev[1] = alphaADev * gravityADev[1] + (1 - alphaADev)
						* input[1];
				gravityADev[2] = alphaADev * gravityADev[2] + (1 - alphaADev)
						* input[2];

				// Calculate the linear acceleration by subtracting gravity from
				// the input signal.
				linearAccelerationADev[0] = input[0] - gravityADev[0];
				linearAccelerationADev[1] = input[1] - gravityADev[1];
				linearAccelerationADev[2] = input[2] - gravityADev[2];

				plotView.setData(input, gravityWiki, linearAccelerationWiki,
						gravityADev, linearAccelerationADev);
			}

			// Save the timestamp for the next update;
			timestampOld = event.timestamp;
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// Register for sensor updates.
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser)
	{
		timeConstant = progress / 10000.0f;
		tvTimeConstant
				.setText("Time Constant: " + Float.toString(timeConstant));
	}

	@Override
	public boolean onTouch(View v, MotionEvent e)
	{
		Log.d(tag, "test");
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

				plotView.setMaxRange(zoom);
				plotView.setMinRange(-zoom);

				distance = newDist;
			}
		}

		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
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

		case R.id.menu_settings:
			showSettingsDialog();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Build a setting dialog and display it.
	 */
	private void showSettingsDialog()
	{
		final Dialog setOffsetDialog = new Dialog(this);
		setOffsetDialog.setTitle("Offset Calibration");
		setOffsetDialog.setCancelable(true);
		setOffsetDialog.setCanceledOnTouchOutside(true);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);

		View layout = inflater.inflate(R.layout.settings,
				(ViewGroup) findViewById(R.id.settings_dialog_root_element));
		setOffsetDialog.setContentView(layout);

		Button doneButton = (Button) layout.findViewById(R.id.done_button);
		doneButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				setOffsetDialog.cancel();
			}
		});

		final ToggleButton xAxisToggleButton = (ToggleButton) layout
				.findViewById(R.id.xaxistogglebutton);
		xAxisToggleButton.setChecked(plotView.isDrawXAxis());

		xAxisToggleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (xAxisToggleButton.isChecked())
				{
					plotView.setDrawXAxis(true);
				} else
				{
					plotView.setDrawXAxis(false);
				}
			}
		});

		final ToggleButton yAxisToggleButton = (ToggleButton) layout
				.findViewById(R.id.yaxistogglebutton);
		yAxisToggleButton.setChecked(plotView.isDrawYAxis());
		yAxisToggleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (yAxisToggleButton.isChecked())
				{
					plotView.setDrawYAxis(true);
				} else
				{
					plotView.setDrawYAxis(false);
				}
			}
		});

		final ToggleButton zAxisToggleButton = (ToggleButton) layout
				.findViewById(R.id.zaxistogglebutton);
		zAxisToggleButton.setChecked(plotView.isDrawZAxis());
		zAxisToggleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (zAxisToggleButton.isChecked())
				{
					plotView.setDrawZAxis(true);
				} else
				{
					plotView.setDrawZAxis(false);
				}
			}
		});

		final ToggleButton rawToggleButton = (ToggleButton) layout
				.findViewById(R.id.rawtogglebutton);
		rawToggleButton.setChecked(plotView.isDrawRaw());
		rawToggleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (rawToggleButton.isChecked())
				{
					plotView.setDrawRaw(true);
				} else
				{
					plotView.setDrawRaw(false);
				}
			}
		});

		final ToggleButton gravityToggleButton = (ToggleButton) layout
				.findViewById(R.id.gravitytogglebutton);
		gravityToggleButton.setChecked(plotView.isDrawGravity());
		gravityToggleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (gravityToggleButton.isChecked())
				{
					plotView.setDrawGravity(true);
				} else
				{
					plotView.setDrawGravity(false);
				}
			}
		});

		final ToggleButton accelToggleButton = (ToggleButton) layout
				.findViewById(R.id.linearacceltogglebutton);
		accelToggleButton.setChecked(plotView.isDrawAccel());
		accelToggleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (accelToggleButton.isChecked())
				{
					plotView.setDrawAccel(true);
				} else
				{
					plotView.setDrawAccel(false);
				}
			}
		});

		final ToggleButton wikiLPToggleButton = (ToggleButton) layout
				.findViewById(R.id.wikilptogglebutton);
		wikiLPToggleButton.setChecked(plotView.isDrawWikiLP());
		wikiLPToggleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (wikiLPToggleButton.isChecked())
				{
					plotView.setDrawWikiLP(true);
				} else
				{
					plotView.setDrawWikiLP(false);
				}
			}
		});

		final ToggleButton aDevLPToggleButton = (ToggleButton) layout
				.findViewById(R.id.andevlptogglebutton);
		aDevLPToggleButton.setChecked(plotView.isDrawAndDevLP());
		aDevLPToggleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (aDevLPToggleButton.isChecked())
				{
					plotView.setDrawAndDevLP(true);
				} else
				{
					plotView.setDrawAndDevLP(false);
				}
			}
		});

		setOffsetDialog.show();
	}

	private final float fingerDist(MotionEvent event)
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}
}
