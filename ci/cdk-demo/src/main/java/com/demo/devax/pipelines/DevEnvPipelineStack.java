package com.demo.devax.pipelines;

import com.demo.devax.Values;
import com.demo.devax.utils.GenericFunctions;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

public class DevEnvPipelineStack extends BasePipeline {

    public DevEnvPipelineStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
    }

    public DevEnvPipelineStack(final Construct scope, final String id, final StackProps props, String appName, String buildSpec, final String codeArn) {
        super(scope, id, props, appName, buildSpec, codeArn);
    }

    void createPipeline(Construct scope, String codeArn){
        GenericFunctions.createTwoStepsPipeline(scope, codeArn,Values.DEV_ENV_PREFIX + getProjectName(), getSourceOutput(), getBuildspecName());
    }

}
