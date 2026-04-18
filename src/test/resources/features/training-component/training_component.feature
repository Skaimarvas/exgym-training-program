Feature: Training service component coverage

  Scenario: A registered trainee can log in and read their own profile
    Given a clean training component state
    And I register trainee alias "alice" with first name "Alice", last name "Stone", birth date "1990-01-01", and address "Tallinn"
    When I log in as trainee alias "alice"
    Then the training response status should be 200
    And the training login response should contain a bearer token
    When I request the authenticated trainee profile for alias "alice"
    Then the training response status should be 200
    And the trainee profile response should contain first name "Alice"

  Scenario: A trainee cannot read another trainee profile
    Given a clean training component state
    And I register trainee alias "alice" with first name "Alice", last name "Stone", birth date "1990-01-01", and address "Tallinn"
    And I register trainee alias "bob" with first name "Bob", last name "Rivers", birth date "1991-02-02", and address "Riga"
    When I log in as trainee alias "bob"
    And I request the trainee profile for alias "alice" using the current token
    Then the training response status should be 403
    And the training error message should contain "cannot access another trainee profile"

  Scenario: Reject trainee registration with a future birth date
    Given a clean training component state
    When I attempt to register a trainee with first name "Future", last name "Person", birth date "2999-01-01", and address "Mars"
    Then the training response status should be 400
    And the training error message should contain "Date of birth must be in the past"