// Copyright (c) Microsoft. All rights reserved.
package soham.sksamples;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.SKBuilders;
import com.microsoft.semantickernel.exceptions.ConfigurationException;
import com.microsoft.semantickernel.orchestration.SKContext;
import com.microsoft.semantickernel.planner.actionplanner.Plan;
import com.microsoft.semantickernel.planner.stepwiseplanner.DefaultStepwisePlanner;
import com.microsoft.semantickernel.planner.stepwiseplanner.StepwisePlanner;
import com.microsoft.semantickernel.textcompletion.TextCompletion;

import reactor.core.publisher.Mono;
import soham.sksamples.util.KernelUtils;


public class Example10_StepwisePlanner {


  public static void main(String[] args) throws ConfigurationException {

    String[] questions = new String[]{
        "create a poem about dogs, then translate it into French",
    };

    for (String question : questions) {
      System.out.println("Result: " + runChatCompletion(question).block().getResult());
    }
  }

  private static Mono<SKContext> runChatCompletion(String question) throws ConfigurationException {
    System.out.println("RunChatCompletion");
    var kernel = getKernel(true);
    return runWithQuestion(kernel, question);
  }

  private static Mono<SKContext> runWithQuestion(Kernel kernel, String question) {

    kernel.importSkillFromDirectory("WriterSkill", "src/main/resources/Skills", "WriterSkill");
    kernel.importSkillFromDirectory("SummarizeSkill", "src/main/resources/Skills", "SummarizeSkill");
    kernel.importSkillFromDirectory("DesignThinkingSkill", "src/main/resources/Skills", "DesignThinkingSkill");

    System.out.println("*****************************************************");
    System.out.println("Question: " + question);

    StepwisePlanner planner = new DefaultStepwisePlanner(kernel, null, null);

    Plan plan = planner.createPlan(question);
    System.out.println("Plan: " + plan.toPlanString());

    return plan.invokeAsync(SKBuilders.context().withKernel(kernel).build());
  }

  private static Kernel getKernel(boolean useChat) throws ConfigurationException {
    OpenAIAsyncClient client = KernelUtils.openAIAsyncClient();
    TextCompletion textCompletion;

      textCompletion = SKBuilders.chatCompletion()
          .withOpenAIClient(client)
          .withModelId("gpt-4")
          .build();

    return SKBuilders.kernel()
        .withDefaultAIService(textCompletion)
        .build();
  }
}