# Blood Analysis App

This repository contains the sources for GSoC 2016 project named [**Mobile Based Blood Analysis**](https://summerofcode.withgoogle.com/projects/#4860676891738112) with Computational Biology @ **University of Nebraska-Lincoln**. 

## Introduction

The aim of this project is to use the camera and processing power of modern day cell phones to develop an intuitive and user-friendly application for the detection and concentration estimation of various bio-markers in blood sample images. It is later planned to be used as a screening test for cancer. The application will allow the user to take images of the blood samples in a set format. The image will then be segmented to detect the regions of interest. After noise removal, the intensity of each individual blob will be calculated. A linear curve will be fit through the intensity and known concentration data and the concentrations of the unknown samples will be estimated from the standard curve which will quantify the various molecules present in the sample.

## Demo

A video showing entire usage flow of the final Android Application with both demo mode on and off can be found [here](https://www.youtube.com/watch?v=Dz0ZodHLZn4).

The algorithm developed for detection is fairly robust. We tested it against images of very bad quality, taken in bad lighting and it performed well. [Here](https://youtu.be/wig3MIRoXp4) is a video showing the robustness of detection using our algorithm.

## Downloads

### How to Build

1. Clone the repository or download it as a [zip file](https://github.com/abdulfatir/blood-analysis-app/archive/master.zip) and extract it.
2. Open Android Studio and import the directory as an existing Android project.

### Binaries

`apk` binaries for the Android app can be downloaded from [releases](https://github.com/abdulfatir/blood-analysis-app/releases). 

## Usage

A detailed document explaining usage and the technical details of the algorithm can be found [here](http://abdulfatir.github.io/GSoC16/#usage).

## Known Issues

1. In some Samsung and Mi phones, at times the image taken from the camera is rotated after capture. For the application to function, the test card should be loaded in portrait orientation like [this](http://abdulfatir.github.io/GSoC16/images/correct.png), and not in [landscape](http://abdulfatir.github.io/GSoC16/images/wrong.png). Therefore, when using the camera feature, the picture should be taken such that it loads as a portrait image.

## Acknowledgment

I'm indebted to Dr. Tomas Helikar for giving me the opportunity of working on this amazing project. I would also like to thank Philipp Jahoda, whose library, [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart), has been used in this project. It has been released under Apache License 2.0.

Also, Thank you, Google. :D

### License for MPAndroidChart

Copyright 2016 Philipp Jahoda

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

```
http://www.apache.org/licenses/LICENSE-2.0
```

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.



