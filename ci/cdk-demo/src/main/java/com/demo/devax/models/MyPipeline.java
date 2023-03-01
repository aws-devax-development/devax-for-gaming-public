package com.demo.devax.models;

import software.amazon.awscdk.services.codepipeline.Pipeline;
import software.amazon.awscdk.services.codepipeline.StageProps;

import java.util.ArrayList;
import java.util.List;

public class MyPipeline {
    private Pipeline pipeline = null;
    List<StageProps> stages = new ArrayList<>();

    public void build(){

    }

}
