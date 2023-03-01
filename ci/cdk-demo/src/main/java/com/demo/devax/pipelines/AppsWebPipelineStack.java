package com.demo.devax.pipelines;

import com.demo.devax.Values;
import com.demo.devax.utils.GenericFunctions;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

public class AppsWebPipelineStack extends BasePipeline {

    public AppsWebPipelineStack(Construct scope, String id, StackProps props) {
        super(scope, id, props);
    }

    public AppsWebPipelineStack(Construct scope, String id, StackProps props, String appName, String buildSpec, String codeArn) {
        super(scope, id, props, appName, buildSpec, codeArn);
    }

    @Override
    void createPipeline(Construct scope, String codeArn) {
        GenericFunctions.createTwoStepsPipeline(scope, codeArn, Values.APPS_PROJECT_PREFIX + getProjectName(), getSourceOutput(), getBuildspecName());
    }
}
