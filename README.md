LowPassLinearAcceleration
=========================

![](http://www.kircherelectronics.com/bundles/keweb/css/images/low_pass_linear_acceleration_phone_graphic.png?raw=true)

Low Pass Linear Acceleration is an Android based open source code example and working application that estimates gravity with a low-pass filter implementation. The gravity estimation can then be used to calculate linear acceleration. The low-pass filter has the advantage of only using the acceleration sensor to provide an estimation of linear acceleration, no other sensors are required. While this example is implemented with Android/Java, the jist of the algorithm can be applied to almost any hardware/language combination to determine linear acceleration.

Linear Acceleration:

An acceleromter can measure the static gravitation field of earth (like a tilt sensor) or it can measure measure linear acceleration (like accelerating in a vehicle), but it cannot measure both at the same time. When talking about linear acceleration in reference to an acceleration sensor, what we really mean is Linear Acceleration = Measured Acceleration - Gravity. The tricky part is determining what part of the signal is gravity.

The Problem:

It is difficult to sequester the gravity component of the signal from the linear acceleration. Some Android devices implement Sensor.TYPE_LINEAR_ACCELERATION and Sensor.TYPE_GRAVITY which perform the calculations for you. Most of these devices are new and equipped with a gyroscope. If you have and older device and do not have a gyroscope, you are going to face some limitations with Sensor.TYPE_ACCELERATION. Note that the implementations of Sensor.TYPE_LINEAR_ACCELERATION and Sensor.TYPE_GRAVITY tend to be poor and are skewed while the device is under peroids of true linear acceleration.

Low-Pass Filters: 

A low-pass filter is a filter that passes low-frequency signals and attenuates (reduces the amplitude of) signals with frequencies higher than the cutoff frequency. The actual amount of attenuation for each frequency varies depending on specific filter design. To find the gravity component of an acceleration signal, a low-pass filter is used to pass the long term portion of the signal (which is assumed to be gravity) through the filter and to attenuate everything else. The gravity component of the signal can then be subtracted from the original acceleration signal to find the linear acceleration.

