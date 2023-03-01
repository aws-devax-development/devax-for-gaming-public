package com.demo.devax.utils;

import com.demo.devax.Values;
import software.amazon.awscdk.services.codebuild.*;
import software.amazon.awscdk.services.codecommit.IRepository;
import software.amazon.awscdk.services.codecommit.Repository;
import software.amazon.awscdk.services.codepipeline.Artifact;
import software.amazon.awscdk.services.codepipeline.Pipeline;
import software.amazon.awscdk.services.codepipeline.StageProps;
import software.amazon.awscdk.services.codepipeline.actions.Action;
import software.amazon.awscdk.services.codepipeline.actions.CodeBuildAction;
import software.amazon.awscdk.services.codepipeline.actions.CodeCommitSourceAction;
import software.amazon.awscdk.services.codepipeline.actions.ManualApprovalAction;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.Policy;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.s3.Bucket;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.List;

public class GenericFunctions {


    private static Policy createCodePipelinePolicy(Construct scope, String projectPrefix) {
        return Policy.Builder.create(scope, projectPrefix + "-pipeline-code-build-policy").policyName(projectPrefix + "-pipeline-code-build-policy").statements(Arrays.asList(PolicyStatement.Builder.create().actions(Arrays.asList("codebuild:StartBuild")).resources(Arrays.asList("*")).effect(Effect.ALLOW).build())).build();
    }

    private static Policy createCodeBuildPolicy(Construct scope, String policyPrefix) {
        return Policy.Builder.create(scope, policyPrefix + "-codebuild-ssm-policy").policyName(policyPrefix + "-codebuild-ssm-policy").statements(Arrays.asList(PolicyStatement.Builder.create().actions(Arrays.asList("ssm:GetParameters")).resources(Arrays.asList("*")).effect(Effect.ALLOW).build())).build();
    }

    public static void updatePipelinePolicy(Construct scope, Pipeline pipeline, String pipelineId) {
        pipeline.getRole().attachInlinePolicy(createCodePipelinePolicy(scope, pipelineId));
    }

    public static void updateActionBuildPolicy(Construct scope, PipelineProject project, String buildProjectId) {
        project.getRole().attachInlinePolicy(createCodeBuildPolicy(scope, buildProjectId));
    }

    public static CodeBuildAction createDeployBuildAction(Construct scope, String buildProjectId, String buildFileName, String projectName, Artifact sourceInput) {
        PipelineProject project = PipelineProject.Builder.create(scope, buildProjectId).buildSpec(BuildSpec.fromSourceFilename(buildFileName)).projectName(projectName).environment(BuildEnvironment.builder().computeType(ComputeType.MEDIUM).buildImage(LinuxBuildImage.AMAZON_LINUX_2_3).build()).build();
        GenericFunctions.updateActionBuildPolicy(scope, project, buildProjectId);
        return CodeBuildAction.Builder.create().actionName("CodeBuild").project(project).executeBatchBuild(false).input(sourceInput).build();
    }

    public static CodeBuildAction createDockerBuildAction(Construct scope, String buildProjectId, String buildFileName, String projectName, Artifact sourceInput, boolean privileged) {
        return createDockerBuildAction(scope, buildProjectId, buildFileName, projectName, sourceInput, privileged, false);
    }

    public static CodeBuildAction createDockerBuildAction(Construct scope, String buildProjectId, String buildFileName, String projectName, Artifact sourceInput, boolean privileged, boolean cached) {
        PipelineProject project = null;
        if (cached) {
            project = PipelineProject.Builder.create(scope, buildProjectId).buildSpec(BuildSpec.fromSourceFilename(buildFileName)).projectName(projectName).environment(BuildEnvironment.builder().privileged(privileged).computeType(ComputeType.MEDIUM).buildImage(LinuxBuildImage.STANDARD_4_0).build()).cache(Cache.bucket(Bucket.fromBucketName(scope, "Bucket", "yagr-m2-cache"))).build();
        } else {
            project = PipelineProject.Builder.create(scope, buildProjectId).buildSpec(BuildSpec.fromSourceFilename(buildFileName)).projectName(projectName).environment(BuildEnvironment.builder().privileged(privileged).computeType(ComputeType.MEDIUM).buildImage(LinuxBuildImage.STANDARD_4_0).build()).build();
        }

        GenericFunctions.updateActionBuildPolicy(scope, project, buildProjectId);
        return CodeBuildAction.Builder.create().actionName("CodeBuild").project(project).executeBatchBuild(false).input(sourceInput).build();
    }

    public static CodeCommitSourceAction createSourceAction(Construct scope, String repoID, String codeArn, Artifact sourceOutput) {
        IRepository repository = Repository.fromRepositoryArn(scope, repoID, codeArn);
        return CodeCommitSourceAction.Builder.create().actionName("CodeCommit").repository(repository).branch("main").output(sourceOutput).build();

    }

    public static Pipeline createPipeline(Construct scope, String pipelineID, List<StageProps> actions) {
        return Pipeline.Builder.create(scope, pipelineID).pipelineName(pipelineID).stages(actions).build();
    }

    public static void createTwoStepsPipeline(Construct scope, String codeArn, String projectPrefix, Artifact sharedOutput, String buildSpecFile) {
        CodeCommitSourceAction sourceAction = GenericFunctions.createSourceAction(scope, projectPrefix + "-repo", codeArn, sharedOutput);
        CodeBuildAction buildAction = GenericFunctions.createDeployBuildAction(scope, projectPrefix + "-build", buildSpecFile, projectPrefix + "-build", sharedOutput);

        StageProps sourceStage = createStage("Source", sourceAction);
        StageProps buildStage = createStage("Build", buildAction);

        String pipelineId = projectPrefix + "-build-pipeline";
        Pipeline pipeline = GenericFunctions.createPipeline(scope, pipelineId, Arrays.asList(sourceStage, buildStage/*, approveStage, applyStage*/));
        GenericFunctions.updatePipelinePolicy(scope, pipeline, pipelineId);
    }

    public static void createFourStepsPipeline(Construct scope, String codeArn, String projectPrefix, Artifact sharedOutput, List<String> buildSpecFiles) {
        createFourStepsPipeline(scope, codeArn, projectPrefix, sharedOutput, buildSpecFiles, false);
    }

    public static void createFourStepsPipeline(Construct scope, String codeArn, String projectPrefix, Artifact sharedOutput, List<String> buildSpecFiles, boolean cached) {
        CodeCommitSourceAction sourceAction = GenericFunctions.createSourceAction(scope, projectPrefix + "-repo", codeArn, sharedOutput);
        CodeBuildAction buildAction = GenericFunctions.createDockerBuildAction(scope, projectPrefix + "-build", buildSpecFiles.get(0), projectPrefix + "-build", sharedOutput, true);
        ManualApprovalAction approveAction = GenericFunctions.CreateManualAction("Approve", Values.CONTACT);
        CodeBuildAction deployAction = GenericFunctions.createDeployBuildAction(scope, projectPrefix + "-deploy", buildSpecFiles.get(1), projectPrefix + "-deploy", sharedOutput);

        StageProps sourceStage = createStage("Source", sourceAction);
        StageProps buildStage = createStage("Build", buildAction);
        StageProps approveStage = createStage("Approve", approveAction);
        StageProps deployStage = createStage("Deploy", deployAction);

        String pipelineId = projectPrefix + "-build-pipeline";
        Pipeline pipeline = GenericFunctions.createPipeline(scope, pipelineId, Arrays.asList(sourceStage, buildStage, approveStage, deployStage));
        GenericFunctions.updatePipelinePolicy(scope, pipeline, pipelineId);
    }

    private static StageProps createStage(String name, Action action) {
        return StageProps.builder().stageName(name).actions(Arrays.asList(action)).build();
    }

    public static ManualApprovalAction CreateManualAction(String name, String email) {
        return ManualApprovalAction.Builder.create().actionName(name).notifyEmails(Arrays.asList(email)).build();
    }

}
