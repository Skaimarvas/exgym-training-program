Feature: Training and workload service integration

  Scenario: Creating a training updates the trainer workload summary
    Given the microservice integration state is clean
    And I register integration trainee alias "trainee" with first name "Alice" and last name "Stone"
    And I register integration trainer alias "trainer" with first name "John", last name "Coach", and specialization "YOGA"
    When I log in to the training service as alias "trainee"
    And I assign trainer alias "trainer" to trainee alias "trainee"
    And I create a training named "Morning Yoga" for trainee alias "trainee" and trainer alias "trainer" on "2099-03-15" with duration 60
    Then the integration training response status should be 200
    When I query the workload summary for trainer alias "trainer"
    Then the integration workload response status should be 200
    And the workload summary for trainer alias "trainer" should contain 60 minutes for month 3 of year 2099

  Scenario: A forbidden training request does not create workload data
    Given the microservice integration state is clean
    And I register integration trainee alias "owner" with first name "Alice" and last name "Stone"
    And I register integration trainee alias "intruder" with first name "Bob" and last name "Rivers"
    And I register integration trainer alias "trainer" with first name "John", last name "Coach", and specialization "YOGA"
    When I log in to the training service as alias "intruder"
    And I create a training named "Unauthorized Yoga" for trainee alias "owner" and trainer alias "trainer" on "2099-03-15" with duration 45
    Then the integration training response status should be 403
    When I query the workload summary for trainer alias "trainer"
    Then the integration workload response status should be 404