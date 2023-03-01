package com.demo.devax;

import java.util.HashMap;

public class Values {

    public static HashMap<String, String> REPO_NAMES = new HashMap() {{
        put("key1", "value1");
        put("key2", "value2");
    }};
    public static final String ACCOUNT_ID = "613477150601";
    public static final String REGION = "ap-southeast-1";
    public static final String CONTACT = "your-name@company.com";


    //build
    public static final String DEFAULT_BUILDSPEC = "buildspec.yml";
    public static final String DEV_BUILDSPEC = "dev-buildspec.yml";


    // apps
    public static final String PACKAGE_BUILDSPEC = "package-buildspec.yml";
    public static final String DEPLOY_BUILDSPEC = "deploy-buildspec.yml";
    public static final String APPS_PROJECT_PREFIX = "devax-day-demo";
    public static final String APPNAME_RANKING = "ranking";
    public static final String APPNAME_WEB = "web";

    // repo ARN
    public static final String WEB_CODE_ARN = "arn:aws:codecommit:" + REGION + ":" + ACCOUNT_ID + ":ab3-app-web";


    // Dev Env
    public static final String DEV_ENV_PREFIX = "dev-terraform-demo";

    // tooling
    public static final String TOOLING_PROJECT_PREFIX = "ab3-cdk-demo";
    public static final String INIT_CODECOMMIT_ARN = "arn:aws:codecommit:ap-southeast-1:613477150601:ab3-cdk-demo-tooling-pipeline";

    //specs
    public static final String TOOLING_PIPELINE_BUILD_SPEC = "pipeline-buildspec.yml";

    // infrastructure
    public static final String INFRASTRUCTURE_PROJECT_PREFIX = "dexax-day-tf-demo";
    public static final String INFRASTRUCTURE_APPLY_PROJECT_PREFIX = "dexax-day-tf-apply-demo";
    public static final String INFRASTRUCTURE_VALIDATE_PROJECT_PREFIX = "dexax-day-tf-validate-demo";
    public static final String INFRASTRUCTURE_CODECOMMIT_ARN = "arn:aws:codecommit:ap-southeast-1:613477150601:ab3-terraform-demo-infrastucture";
    public static final String INFRASTRUCTURE_PIPELINE_BUILD_SPEC = "infra-buildspec.yml";
    public static final String INFRA_VALIDATION_BUILD_SPEC = "infra-validation-buildspec.yml";

}
