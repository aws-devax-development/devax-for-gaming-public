package com.demo.devax;

import com.demo.devax.pipelines.*;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ssm.StringParameter;

public class CicdApp {
    public static void main(final String[] args) {
        App app = new App();

        StringParameter parameter = new StringParameter(app, "/devax/repo/app-web", null);
        String webRepoName = parameter.getStringValue();

        new AppsWebPipelineStack(app, Values.APPS_PROJECT_PREFIX + "-web-pipeline", StackProps.builder()
                .env(Environment.builder()
                        .account(Values.ACCOUNT_ID)
                        .region(Values.REGION)
                        .build())
                .build(),
                Values.APPNAME_WEB, Values.INFRA_VALIDATION_BUILD_SPEC + "," + Values.DEPLOY_BUILDSPEC, Values.CODECOMMIT_ARN_PREFIX + webRepoName);
        app.synth();
    }
}
