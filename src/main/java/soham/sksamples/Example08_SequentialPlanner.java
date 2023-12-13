package soham.sksamples;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.exceptions.ConfigurationException;
import com.microsoft.semantickernel.planner.actionplanner.Plan;
import com.microsoft.semantickernel.planner.sequentialplanner.SequentialPlanner;
import com.microsoft.semantickernel.planner.sequentialplanner.SequentialPlannerRequestSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Set;

import static soham.sksamples.util.Constants.*;
import static soham.sksamples.util.KernelUtils.kernel;

public class Example08_SequentialPlanner {

    private static final Logger log = LoggerFactory.getLogger(Example08_SequentialPlanner.class);

    public static void main(String[] args) {
        try {
            log.debug("== Instantiates the Kernel ==");
            Kernel kernel = kernel();

            log.debug("== Adding multiple skills to kernel ==");
            kernel.importSkillFromDirectory("WriterSkill", "src/main/resources/Skills", "WriterSkill");
            kernel.importSkillFromDirectory("SummarizeSkill", "src/main/resources/Skills", "SummarizeSkill");
            kernel.importSkillFromDirectory("DesignThinkingSkill", "src/main/resources/Skills", "DesignThinkingSkill");

            log.debug("== Create a Planner using the kernel ==");
            SequentialPlanner planner = new SequentialPlanner(kernel, new SequentialPlannerRequestSettings(
                    1.0f,
                    100,
                    Set.of(),
                    Set.of(),
                    Set.of(),
                    1024,
                    true
            ), null);

           log.debug("== Example 1 : use Summarizer and Translator skills ==");
           summarizeAndTranslate(planner);
            log.debug("== Example 2 : use Rewrite skill ==");
           // rewriteInAStyle(planner);
            log.debug("== Example 3 : use DesignThinking skill and compose an email ==");
            //designThinking(planner);
        } catch (ConfigurationException | IOException e) {
            log.error("Problem in paradise", e);
        }
    }

    private static void designThinking(SequentialPlanner planner) {
        log.debug("== Run kernel with Planner ==");
        Mono<Plan> plan =
                planner.createPlanAsync("apply Design Thinking to above call transcript.");
        printResult(plan, CallTranscript);
    }

    private static void rewriteInAStyle(SequentialPlanner planner) {
        log.debug("== Run kernel with Planner ==");
        Mono<Plan> plan =
                planner.createPlanAsync("rewrite the above content in Yoda from Starwars style");
        printResult(plan, TextToSummarize);
    }

    private static void summarizeAndTranslate(SequentialPlanner planner) {
        log.debug("== Run kernel with Planner ==");
        Mono<Plan> plan =
                planner.createPlanAsync("summarize the content and then translate it to Dutch.");
        printResult(plan, TextToSummarize);
    }

    private static void printResult(Mono<Plan> plan, String input) {
        log.debug("== Result ==");
        log.debug(plan.block().invokeAsync(input).block().getResult());
    }
}
