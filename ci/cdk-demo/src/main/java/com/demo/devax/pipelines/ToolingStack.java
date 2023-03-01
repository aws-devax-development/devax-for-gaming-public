package com.demo.devax.pipelines;

import com.demo.devax.Values;
import com.demo.devax.utils.GenericFunctions;
import software.amazon.awscdk.services.codebuild.*;
import software.amazon.awscdk.services.codecommit.IRepository;
import software.amazon.awscdk.services.codepipeline.Artifact;
import software.amazon.awscdk.services.codepipeline.StageProps;
import software.amazon.awscdk.services.codecommit.Repository;
import software.amazon.awscdk.services.codepipeline.Pipeline;
import software.amazon.awscdk.services.codepipeline.actions.CodeBuildAction;
import software.amazon.awscdk.services.codepipeline.actions.CodeCommitSourceAction;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class ToolingStack extends Stack {
    public ToolingStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public ToolingStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        PipelineProject CodeBuildProject = PipelineProject.Builder.create(this, Values.TOOLING_PROJECT_PREFIX + "-tooling-pipeline")
                .buildSpec(BuildSpec.fromSourceFilename(Values.TOOLING_PIPELINE_BUILD_SPEC))
                .projectName(Values.TOOLING_PROJECT_PREFIX + "-tooling-pipeline")
                .environment(BuildEnvironment.builder()
                        .computeType(ComputeType.MEDIUM)
                        .buildImage(LinuxBuildImage.AMAZON_LINUX_2_3)
                        .build())
                .build();
        IRepository repository = Repository.fromRepositoryArn(this, Values.TOOLING_PROJECT_PREFIX + "-tooling-repo", Values.INIT_CODECOMMIT_ARN);
        Artifact sourceOutput = new Artifact("source_output");

        CodeCommitSourceAction sourceAction = CodeCommitSourceAction.Builder.create()
                .actionName("CodeCommit")
                .repository(repository)
                .branch("main")
                .output(sourceOutput)
                .build();
        CodeBuildAction buildAction = CodeBuildAction.Builder.create()
                .actionName("CodeBuild")
                .project(CodeBuildProject)
                .executeBatchBuild(false)
                .input(sourceOutput)
                .build();

        Pipeline pipeline = Pipeline.Builder.create(this, Values.TOOLING_PROJECT_PREFIX + "-pipeline-automation")
                .pipelineName(Values.TOOLING_PROJECT_PREFIX + "-pipeline-automation")
                .stages(Arrays.asList(StageProps.builder()
                        .stageName("Source")
                        .actions(Arrays.asList(sourceAction))
                        .build(), StageProps.builder()
                        .stageName("Build")
                        .actions(Arrays.asList(buildAction))
                        .build()))
                .build();

        GenericFunctions.updatePipelinePolicy(this, pipeline, Values.TOOLING_PROJECT_PREFIX);
        GenericFunctions.updateActionBuildPolicy(this, CodeBuildProject, Values.TOOLING_PROJECT_PREFIX);
    }


}
