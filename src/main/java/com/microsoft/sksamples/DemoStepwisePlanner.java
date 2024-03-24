package com.microsoft.sksamples;

import java.io.File;
import java.util.List;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.SKBuilders;
import com.microsoft.semantickernel.connectors.ai.openai.util.ClientType;
import com.microsoft.semantickernel.connectors.ai.openai.util.OpenAIClientProvider;
import com.microsoft.semantickernel.exceptions.ConfigurationException;
import com.microsoft.semantickernel.orchestration.SKContext;
import com.microsoft.semantickernel.planner.actionplanner.Plan;
import com.microsoft.semantickernel.planner.stepwiseplanner.DefaultStepwisePlanner;
import com.microsoft.semantickernel.planner.stepwiseplanner.StepwisePlanner;
import com.microsoft.semantickernel.textcompletion.TextCompletion;

public class DemoStepwisePlanner {

  public static void main(String[] args) throws ConfigurationException {
    String[] questions = new String[]{
        "create a poem about dogs, then translate it into French",
    };

    for (String question : questions) {
      // Create kernel
      OpenAIAsyncClient client = OpenAIClientProvider.getWithAdditional(List.of(
          new File("src/main/resources/conf.properties")), ClientType.AZURE_OPEN_AI);
      TextCompletion textCompletion = SKBuilders.chatCompletion()
          .withOpenAIClient(client)
          .withModelId("gpt-4")
          .build();
      Kernel kernel = SKBuilders.kernel()
          .withDefaultAIService(textCompletion)
          .build();

      // Import skills
      kernel.importSkillFromDirectory("WriterSkill", "src/main/resources/Skills", "WriterSkill");
      kernel.importSkillFromDirectory("SummarizeSkill", "src/main/resources/Skills", "SummarizeSkill");
      kernel.importSkillFromDirectory("DesignThinkingSkill", "src/main/resources/Skills", "DesignThinkingSkill");

      // Create a stepwise planner
      StepwisePlanner planner = new DefaultStepwisePlanner(kernel, null, null);

      // Create a plan and execute it
      Plan plan = planner.createPlan(question);
      SKContext result = plan.invokeAsync(SKBuilders.context().withKernel(kernel).build()).block();

      System.out.println("Result: " + result.getResult());
    }
  }
}