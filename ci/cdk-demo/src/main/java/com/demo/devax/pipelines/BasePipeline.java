package com.demo.devax.pipelines;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.codepipeline.Artifact;
import software.amazon.awscdk.services.codepipeline.StageProps;
import software.amazon.awscdk.services.codepipeline.actions.Action;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class BasePipeline extends Stack {
    private String appName = null;
    private String projectName = null;
    private String buildspecName = null;
    private List<String> buildspecNames = null;

    private Artifact sourceOutput = new Artifact("source_output");

    public void setAppName(String appName){
        this.appName = appName;
        this.projectName = "-" + appName + "-pipeline";
    }
    public BasePipeline(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
    }

    public BasePipeline(final Construct scope, final String id, final StackProps props, String appName, String buildSpec, final String codeArn) {
        this(scope, id, props);
        this.setAppName(appName);
        this.setBuildspecName(buildSpec);
        createPipeline(this, codeArn);
    }
    public void setBuildspecName(String buildspecName) {
        if(buildspecName.split(",").length > 1){
            this.buildspecNames = new ArrayList<>();
            for(String item : buildspecName.split(",")){
                this.buildspecNames.add(item);
            }
        }
        this.buildspecName = buildspecName;
    }

    public Artifact getSourceOutput() {
        return sourceOutput;
    }

    public String getAppName() {
        return appName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getBuildspecName() {
        return buildspecName;
    }
    public String getBuildspecName(int i) {
        if(buildspecNames == null){
            return null;
        }
        return buildspecNames.get(i);
    }

    public List<String> getBuildspecNames() {
        return buildspecNames;
    }

    public StageProps createStage(String name, Action action){
        return StageProps.builder().stageName(name).actions(Arrays.asList(action)).build();
    }

    abstract void createPipeline(final Construct scope, String codeArn);
}
