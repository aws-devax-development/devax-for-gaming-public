#!/bin/bash

cd code-reviews
pip install --target ./package requests --upgrade
pip install --target ./package aws_xray_sdk --upgrade
cd package
zip -r ../reviews.zip .
cd ..
zip -g reviews.zip index.py
cd ..
mv ./code-reviews/reviews.zip ./tf/


cd code-details
pip install --target ./package requests --upgrade
pip install --target ./package aws_xray_sdk --upgrade
cd package
zip -r ../details.zip .
cd ..
zip -g details.zip index.py
cd ..
mv ./code-details/details.zip ./tf/