LowPassLinearAcceleration
=========================

Estimates gravity to calculate linear acceleration with a low-pass filter implementation.

Linear Acceleration:

An acceleromter can measure the static gravitation field of earth (like a tilt sensor) or it can measure measure linear acceleration (like accelerating in a vehicle), but it cannot measure both at the same time. When talking about linear acceleration in reference to an acceleration sensor, what we really mean is Linear Acceleration = Measured Acceleration - Gravity. The hard part is determining what part of the signal is gravity.

The Problem:

It is almost difficult to sequester the gravity component of the signal from the linear acceleration. Some Android devices implement Sensor.TYPE_LINEAR_ACCELERATION and Sensor.TYPE_GRAVITY which perform the calculations for you. Most of these devices are new and equipped with a gyroscope. If you have and older device and do not have a gyroscope, you are going to face some limitations with Sensor.TYPE_ACCELERATION. The tilt of the device can only be measured accurately assuming the device is not experiencing any linear acceleration. The linear acceleration can only be measured accurately if the tilt of the device is known. We can, however, estimate the tilt with a low-pass filter and apply the corrections selectively. 

![Alt text](http://blog.kircherelectronics.com/blog/images/droid_razr_lpf_linear_accel_alpha_zero_point_nine.png "Low Pass Linear Acceleration")
