# Real Estate Agent
A real estate agent is a web application that helps clients buy or rent real estate by extracting and collecting ads from multiple sources, without intermediaries and provides a search and notification based system for registered users.

### Profiles example
Add file Profiles.clj ignored by .gitignore

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

### Run app
Use `lein with-profiles +dev,+dev-local run` to run the app.

### Run tests
Use `lein with-profiles +test,+test-local test` to run the tests.

###Check Artifacts

`lein-ancient`'s default behaviour is to check your current project (or a given file/directory) for artifacts that have newer versions available, e.g.:

```
$ lein ancient
[com.taoensso/timbre "2.6.2"] is available but we use "2.1.2"
[potemkin "0.3.3"] is available but we use "0.3.0"
[pandect "0.3.0"] is available but we use "0.2.3"
```