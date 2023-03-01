package com.demo.devax;

import com.demo.devax.pipelines.*;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class CicdApp {
    public static void main(final String[] args) {
        App app = new App();

        new AppsWebPipelineStack(app, Values.APPS_PROJECT_PREFIX + "-web-pipeline", StackProps.builder()
                .env(Environment.builder()
                        .account(Values.ACCOUNT_ID)
                        .region(Values.REGION)
                        .build())
                .build(),
                Values.APPNAME_WEB, Values.INFRA_VALIDATION_BUILD_SPEC + "," + Values.DEPLOY_BUILDSPEC,  Values.REPO_WEB_SSM_PARAMETER);

        app.synth();
    }
}
