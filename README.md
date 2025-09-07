try no to put to much obvios name in thing for john deere.
git only use development but i kmnow it should be in branches fro each feature and more.
the model is created for avoiding damage when the message data changes. comon.
not tdd but test as soon and as much as i can.
to run mock whitelist autehtication and local queue initialization star de proyect with -Dspring.profiles.active=dev
you acantest authorized client and mock with curl --location 'http://localhost:8080/api/machines/30/authorized'
many ways to better it up: chain of responsabilit patter in service or something like that. or use aws sqs templates and async clients.
validacion como si fuera pro, docker compose, test de integracion. reciliencia, pdf.

Task summary
Keeping the provided scenario in mind, here is a quick overview of what we would like to be included in your project:
1. Messaging Service Integration:
   o Implement a solution that reads messages from a messaging service (e.g., AWS SQS).
2. HTTP Request Handling:
   o Make an HTTP request to a microservice based on the provided API definition.
   o Utilize an appropriate messaging client for this interaction.
3. Database Interaction:
   o Process the response and save the result in a database.
4. Message Forwarding:
   o Forward the original message to the next step in the workflow.
5. Testing:
   o Write at least one integration test to verify the overall functionality.
   Write at least one unit test for a service class in your implementation.
   Please consider the following requirements concerning the deliverable
   • Containerization: Package your application using a container solution (e.g., Docker or Podman).
   • Version Control: Use Git for your project and provide a repository link with a clear commit history. If Git is not feasible, you may submit a zip file of your project.
   • Instructions: Include clear instructions on how to run your application..
   Evaluation Criteria
   Your submission will be evaluated based on the following criteria:
   • Completeness and correctness of the implementation
   • Robustness & Resilience of the code
   • Code quality and maintainability
   • Quality of documentation
   • Effectiveness and coverage of tests
   We look forward to seeing your approach to this challenge and how you can contribute to our team. If you have any questions or need further clarification, please don’t hesitate to reach out.
   Good luck, and we can’t wait to see what you create!