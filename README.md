# Workshop instructions

Congratulations! You are a new hire on the Emergency Response Team. We are working on a revolutionary approach on how to respond to an emergency at a big scale.

Before you start working on the actual code we suggest that you take a look at our homepage https://www.erdemo.io/, where you will get some more information about, what we are working on. We also hope you enjoyed our introduction on the topic:-)

Okay, are you ready for your first challenge? If yes, read on.

As you saw in our [architectural diagram](https://www.erdemo.io/architecture/) we are working on a lot of microservices. As part of our continued improvement, we want to try out new technologies in order to investigate whether we can improve our code in terms of readability, performance, modularity and so on. To not blow up the whole thing, we've decided to try and change one of the microservices and see how it affects the solution. We have decided the following process for evaluating new technology.
* We setup several teams to work on the same problem.
* Each team will provide an implementation of the Incident Service
* At the end of the implementation phase, there will be a presentation, where each team will present their solution.
* There are no specific evaluation criteria. Each team can focus on, what they find is important. Suggestions for criteria could be:
  * Performance
  * Readability
  * Testability
  * Improved memory/CPU footprint
* After the presentation, we'll discuss the solutions and move on from there.

For providing the implementation, you'll need some further information.
1. [Getting Started Guide](docs/GettingStarted.md)
2. [Incident API Specification](docs/instructions/IncidentServiceAPISpec.md)
3. [Incident Deployment Guide](docs/instructions/IncidentServiceDeployGuide.md)

That's it. Now you have all the information to get started. We suggest that you discuss in your team, what you want to focus on, and how to get started. Some suggestions for a starting point could be:
1. Work automating the deployment of one of the existing solutions, maybe adding CI/CD capabilities?
2. Improve on one of the existing solutions (e.g. add database storage to the [Quarkus solution](https://github.com/Emergency-Response-Demo/workshop-hackathon/tree/master/solutions/quarkus))
3. Choose a framework and implement a minimal solution of the incident service
4. Go all in and implement the full service in your favorite framework and integrate it with monitoring now that you're at it.

If you are still in doubt about anything, feel free to reach out to one of the tech leads.
