package com.demo.devax.pipelines;

import com.demo.devax.Values;
import com.demo.devax.utils.GenericFunctions;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.codebuild.*;
import software.amazon.awscdk.services.codecommit.IRepository;
import software.amazon.awscdk.services.codecommit.Repository;
import software.amazon.awscdk.services.codepipeline.Artifact;
import software.amazon.awscdk.services.codepipeline.Pipeline;
import software.amazon.awscdk.services.codepipeline.StageProps;
import software.amazon.awscdk.services.codepipeline.actions.CodeBuildAction;
import software.amazon.awscdk.services.codepipeline.actions.CodeCommitSourceAction;
import software.amazon.awscdk.services.codepipeline.actions.ManualApprovalAction;
import software.constructs.Construct;

import java.util.Arrays;

public class InfrastructureStack  extends Stack {
    public InfrastructureStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public InfrastructureStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        createPipeline(this);
    }
    public void createPipeline (Construct scope){
        PipelineProject CodeBuildProject = PipelineProject.Builder.create(scope, Values.INFRASTRUCTURE_PROJECT_PREFIX + "-infrastructure-apply")
                .buildSpec(BuildSpec.fromSourceFilename(Values.INFRASTRUCTURE_PIPELINE_BUILD_SPEC))
                .projectName(Values.INFRASTRUCTURE_PROJECT_PREFIX + "-infrastructure-apply")
                .environment(BuildEnvironment.builder()
                        .computeType(ComputeType.MEDIUM)
                        .buildImage(LinuxBuildImage.AMAZON_LINUX_2_3)
                        .build())
                .build();
        PipelineProject ValidateProject = PipelineProject.Builder.create(scope, Values.INFRASTRUCTURE_PROJECT_PREFIX + "-infrastructure-validate")
                .buildSpec(BuildSpec.fromSourceFilename(Values.INFRA_VALIDATION_BUILD_SPEC))
                .projectName(Values.INFRASTRUCTURE_PROJECT_PREFIX + "-infrastructure-validate")
                .environment(BuildEnvironment.builder()
                        .computeType(ComputeType.MEDIUM)
                        .buildImage(LinuxBuildImage.AMAZON_LINUX_2_3)
                        .build())
                .build();
        IRepository repository = Repository.fromRepositoryArn(scope, Values.INFRASTRUCTURE_PROJECT_PREFIX + "-infrastructure-repo", Values.INFRASTRUCTURE_CODECOMMIT_ARN);
        Artifact sourceOutput = new Artifact("source_output");
        CodeCommitSourceAction sourceAction = CodeCommitSourceAction.Builder.create()
                .actionName("CodeCommit")
                .repository(repository)
                .branch("main")
                .output(sourceOutput)
                .build();
        CodeBuildAction applyAction = CodeBuildAction.Builder.create()
                .actionName("Infrastructure-Apply")
                .project(CodeBuildProject)
                .executeBatchBuild(false)
                .input(sourceOutput)
                .build();
        CodeBuildAction validateAction = CodeBuildAction.Builder.create()
                .actionName("Infrastructure-Validate")
                .project(ValidateProject)
                .executeBatchBuild(false)
                .input(sourceOutput)
                .build();
        ManualApprovalAction manualApprovalAction = ManualApprovalAction.Builder.create()
                .actionName("Approve")
                .notifyEmails(Arrays.asList("yagrxu@amazon.com"))
                .build();
        Pipeline pipeline = Pipeline.Builder.create(scope, Values.INFRASTRUCTURE_PROJECT_PREFIX + "-infrastructure-pipeline")
                .pipelineName(Values.INFRASTRUCTURE_PROJECT_PREFIX + "-infrastructure-pipeline")
                .stages(Arrays.asList(
                        StageProps.builder()
                                .stageName("Source")
                                .actions(Arrays.asList(sourceAction))
                                .build(),
                        StageProps.builder()
                                .stageName("Validate")
                                .actions(Arrays.asList(validateAction))
                                .build(),
                        StageProps.builder()
                                .stageName("Approval")
                                .actions(Arrays.asList(manualApprovalAction))
                                .build(),
                        StageProps.builder()
                                .stageName("Apply")
                                .actions(Arrays.asList(applyAction))
                                .build()
                ))
                .build();
        GenericFunctions.updatePipelinePolicy(this, pipeline, Values.INFRASTRUCTURE_PROJECT_PREFIX);
        GenericFunctions.updateActionBuildPolicy(this, CodeBuildProject, Values.INFRASTRUCTURE_APPLY_PROJECT_PREFIX);
        GenericFunctions.updateActionBuildPolicy(this, ValidateProject, Values.INFRASTRUCTURE_VALIDATE_PROJECT_PREFIX);
    }
}
