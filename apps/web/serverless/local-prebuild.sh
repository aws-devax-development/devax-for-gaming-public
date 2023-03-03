#!/bin/bash

cd code-reviews/package
zip -r ../reviews.zip .
cd ..
zip -g reviews.zip index.py
cd ..
mv ./code-reviews/reviews.zip ./tf/


cd code-details/package
zip -r ../details.zip .
cd ..
zip -g details.zip index.py
cd ..
mv ./code-details/details.zip ./tf/