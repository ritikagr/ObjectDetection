# Image Analytics for stock counting and Brand Presence at Modern trade stores

Android application for object detection and localization using SentiSight SDK provided by NeuroTechnology

SentiSight SDK , provided by NeuroTechnology, provides computer vision based object recognition for many 
platforms( Windows, Linux, Android).
 
Features:
Reliable, innovative algorithm that is tolerant of variation in appearance, object scale, rotation and pose.
Accurate detection, processing and tracking of objects in real-time.

NeuroTechnology provides two type of SDK for Object Recognition:
 
SentiSight SDK (SentiSight is intended for developers who want to use computer vision-based object recognition in their applications. 
Through manual or fully automatic object learning it enables searching for learned objects in images from almost any camera, webcam,
still picture or live video in an easy, yet versatile, way.)
 
SentiSight Embedded SDK (SentiSight Embedded is designed for developers who want to use computer vision-based object recognition in 
their applications for smartphones, tablets and other mobile devices.)
 
Android Implementation KeyPoints :
 
There are two steps for object detection:
 
Learning
Recognition
 
Learning: In order to recognize an object in an image, the appearance of the object should be memorized. The process of memorizing an 
appearance of the object from images with various poses is called object learning. 
 
A set of images containing the object should be provided to the algorithm and the algorithm extracts so called model - a symbolic
representation of the object. It is highly recommended to provide information about exact location of the object in the image. 
This can be done by the shapes of the object. For learning either we can define shape of object in image or take whole image as a 
shape for learning. We will take whole image as a shape for learning. So there should be only specific object in learning image and 
no other background or object.
 
The quality of object recognition highly depends on model created by object learning part. Thus, a set of images of the object should
contain all possible poses of the object - the three dimensional rotations (off plane rotations) are highly recommended. Also, 
it is recommended that images of the object would be taken under different light conditions or using different light sources in 
order to improve invariance to diverse light conditions. It is recommended to use shapes.
 
2. Recognition: After learning task, we can use learned models for detection of objects in image. We will use Recognition module of
SDK for recognition. We will need object models and image for recognition. 
 
General Constraints :
 
1. Matching threshold: 
A Matching threshold allows you to manipulate the recognition results.
The matching threshold is selected experiment way because objects are very different. Default is 40000 and for concrete case it 
should be determined. Matching threshold can be in range 0 to 500 000. 
 
2. Enable mask enhancement: 
Enables mask enhancement. The given mask can be extended in order to eliminate noise and small halls inside the object.
 
3. Recognition speed: 
High Speed - Cannot find all instances of an object (finds only one instance). 
 
Low speed - Slower, but returns all detected instances of an object, better recognition quality.
 
We will use Low speed of Recognition because we want to detect multiple
occurrences of a object.
 
Constraints for Learning/Recognition:
 
1. Learning mode:
LowProfile - Fastest and has smallest template size, suitable in most situations. 
 
HighProfile - Additional not rotation invariant information added, improves recognition quality for not rotated objects 
(has no impact on rotated object recognition). About 5%-10% slower and has about 20%-60% bigger template size than for low profile
learning mode. 
 
HighProfileEx - Additional information is added, improves recognition for all types of objects. About 50%-100% slower and has 
about 5%-10% bigger template size compared with low profile. 

2. Feature type 
Recognition or learning features type. 
 
Blob - Local features based recognition/learning type. This feature type is faster compared to Shape type. 
 
Shape - Shape based recognition/learning type. Allows to recognize not so local feature rich (texture rich) objects if they have 
distinguishable external or internal edges. This mode is intended for rigid object recognition and localization. This recognition 
mode is scale invariant and in plane rotation invariant. If the scale difference is so big that object changes its appearance,
like rugged edge becomes smooth line, several views can be added to the model to still recognize the object. The speed of this
mode is slower than of blob based one however in some conditions near real time performance can be achieved. 
 
Combined - Combined recognition/learning type. Shape based recognition/learning mode can be combined with blob based mode to 
increase recognition/learning quality even more for object which have both kind features.
