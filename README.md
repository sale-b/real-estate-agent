# Real Estate Agent
A real estate agent is a web application that helps clients buy or rent real estate by extracting and collecting ads from multiple sources, without intermediaries and provides a search and notification based system for registered users.

#Profiles example
Profiles.clj ignored by .gitignore

```
{:dev-local {:env {:port "8080"
                    :database-type "postgresql"
                    :database-name "real_estate_agent"
                    :database-username "postgres"
                    :database-password "password"
                    :database-host "localhost"
                    :database-port "5432"}}
  :test-local {:env {:port "9090"
                     :database-type "postgresql"
                     :database-name "real_estate_agent_test"
                     :database-username "postgres"
                     :database-password "password"
                     :database-host "localhost"
                     :database-port "5432"}}}
```

# Run app
Use `lein with-profiles +dev,+dev-local run` to run the app.

# Run tests
Use `lein with-profiles +test,+test-local test to run the tests.