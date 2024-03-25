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
import com.microsoft.semantickernel.skilldefinition.annotations.DefineSKFunction;
import com.microsoft.semantickernel.skilldefinition.annotations.SKFunctionInputAttribute;
import com.microsoft.semantickernel.skilldefinition.annotations.SKFunctionParameters;
import com.microsoft.semantickernel.textcompletion.TextCompletion;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.awt.Desktop;

public class DemoStepwisePlanner {

    public static void main(String[] args) throws ConfigurationException {
        String question = "create a poem about dogs, then translate it into French then email the French poem to Steven";

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
        kernel.importSkill(new Emailer(), "Emailer"); // Import Emailer skill

        // Create a stepwise planner
        StepwisePlanner planner = new DefaultStepwisePlanner(kernel, null, null);

        // Create a plan and execute it
        Plan plan = planner.createPlan(question);
        SKContext result = plan.invokeAsync(SKBuilders.context().withKernel(kernel).build()).block();

        System.out.println("Result: " + result.getResult());

    }

    public static class Emailer {
        @DefineSKFunction(name = "getEmailAddress", description = "Retrieves the email address for a given user")
        public String getEmailAddress(
                @SKFunctionInputAttribute(description = "The name of the person to get an email address for") String name) {
            switch (name) {
                case "Steven":
                    return "codeking@example.com";
                default:
                    throw new RuntimeException("Unknown user: " + name);
            }
        }

        @DefineSKFunction(name = "sendEmail", description = "Sends an email")
        public String sendEmail(
                @SKFunctionParameters(name = "subject", description = "The email subject") String subject,
                @SKFunctionParameters(name = "message", description = "The message to email") String message,
                @SKFunctionParameters(name = "emailAddress", description = "The emailAddress to send the message to") String emailAddress) {
            System.out.println("================= Sending Email ====================");
            System.out.printf("To: %s%n", emailAddress);
            System.out.printf("Subject: %s%n", subject);
            System.out.printf("Message: %s%n", message);
            System.out.println("====================================================");

            // Display the email content in a browser
            String emailContent = "To: " + emailAddress + "\nSubject: " + subject + "\nMessage: " + message;
            ResultDisplayServer server = new ResultDisplayServer();
            try {
                server.startServer(emailContent);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "Message sent";
        }
    }

    static class ResultDisplayServer {
        private HttpServer server;

        public void startServer(String text) throws Exception {
            server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/", new MyHandler(text));
            server.setExecutor(null); // creates a default executor
            server.start();

            // Open default browser to the server's URL
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                URI uri = new URI("http://localhost:8000/");
                Desktop.getDesktop().browse(uri);
            }
        }

        static class MyHandler implements HttpHandler {
            private String text;

            public MyHandler(String text) {
                this.text = text;
            }

            @Override
            public void handle(HttpExchange t) throws IOException {
                String response = this.text;
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        public void stopServer() {
            server.stop(0);
        }
    }
}