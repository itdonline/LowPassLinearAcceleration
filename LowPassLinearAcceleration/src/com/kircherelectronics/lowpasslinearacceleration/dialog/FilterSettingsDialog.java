package com.kircherelectronics.lowpasslinearacceleration.dialog;

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.kircherelectronics.lowpasslinearacceleration.R;
import com.kircherelectronics.lowpasslinearacceleration.plot.PlotPrefCallback;
import com.kircherelectronics.lowpasslinearacceleration.prefs.PrefUtils;

/*
 * Acceleration Filter
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
 * A special dialog for the settings of the application. Allows the user to
 * select what filters are plotted and set filter parameters.
 * 
 * @author Kaleb
 * @version %I%, %G%
 */
public class FilterSettingsDialog extends Dialog
{
	private boolean invertAxisActive = false;

	private float lpfTimeConstant;

	private Button buttonAccept;

	private CheckBox checkBoxInvertAxisActive;

	private DecimalFormat df;

	private EditText editTextLpfTimeConstant;

	private final PlotPrefCallback callback;

	/**
	 * Create a dialog.
	 * 
	 * @param context
	 *            The context.
	 * @param lpfWiki
	 *            The Wikipedia LPF.
	 * @param lpfAndDev
	 *            The Android Developer LPF.
	 */
	public FilterSettingsDialog(Context context, PlotPrefCallback callback)
	{
		super(context);

		this.callback = callback;

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		readPrefs();

		df = new DecimalFormat("#.##");

		LayoutInflater inflater = getLayoutInflater();

		View settingsView = inflater.inflate(R.layout.settings_dialog_view, null,
				false);

		buttonAccept = (Button) settingsView.findViewById(R.id.button_accept);

		buttonAccept.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				invertAxisActive = checkBoxInvertAxisActive.isChecked();

				lpfTimeConstant = Float.valueOf(editTextLpfTimeConstant
						.getText().toString());

				writePrefs();
				
				FilterSettingsDialog.this.callback.checkPlotPrefs();
				
				FilterSettingsDialog.this.dismiss();
			}
		});



		checkBoxInvertAxisActive = (CheckBox) settingsView
				.findViewById(R.id.check_box_invert_axis_active);

		editTextLpfTimeConstant = (EditText) settingsView
				.findViewById(R.id.edit_text_lpf_time_constant);

		checkBoxInvertAxisActive.setChecked(this.invertAxisActive);

		editTextLpfTimeConstant.setText(String.valueOf(this.lpfTimeConstant));

		this.setContentView(settingsView);
	}

	@Override
	public void onStop()
	{
		super.onStop();

		writePrefs();
	}

	/**
	 * Read in the current user preferences.
	 */
	private void readPrefs()
	{
		SharedPreferences prefs = this.getContext().getSharedPreferences(
				PrefUtils.FILTER_PREFS, Activity.MODE_PRIVATE);

		this.invertAxisActive = prefs.getBoolean(
				PrefUtils.INVERT_AXIS_ACTIVE, false);
		this.lpfTimeConstant = prefs.getFloat(PrefUtils.LPF_TIME_CONSTANT, 1);


	}

	/**
	 * Write the preferences.
	 */
	private void writePrefs()
	{
		// Write out the offsets to the user preferences.
		SharedPreferences.Editor editor = this
				.getContext()
				.getSharedPreferences(PrefUtils.FILTER_PREFS,
						Activity.MODE_PRIVATE).edit();

		editor.putBoolean(PrefUtils.INVERT_AXIS_ACTIVE,
				this.invertAxisActive);
		editor.putFloat(PrefUtils.LPF_TIME_CONSTANT, this.lpfTimeConstant);

		editor.commit();
	}
}
