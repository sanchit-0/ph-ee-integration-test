Feature: Mock Airtel API Test

  Scenario: MA-001 USSD Push Payment API Is Unsuccessful
    Given  I have MSISDN as "6729461912"
    And I have transaction id as "1278320"
    And I can mock USSD push payment request body
    When I call USSD push payment API with MSISDN with expected status of 200
    Then I should get non empty response
    And I should get airtel USSD push payment response body in response
    And I should get response with data field null

  Scenario: MA-002 USSD Push Payment API Is Successful And CallBack Gives Success Response
    Given I have MSISDN as "1643344477"
    And I have transaction id as "123456"
    And I can mock USSD push payment request body
    When I call USSD push payment API with MSISDN with expected status of 200
    Then I should get non empty response
    And I should get airtel USSD push payment response body in response
    And I should get response with data field not null
    When I call send callback API with expected status of 200
    Then I should get non empty response
    And I should get airtel callback response body in response
    And I should be able to extract status code from send callback response body
    And I should get status code as "TS"
    When I call the get transaction enquiry API with transactionID with expected status of 200
    Then I should get non empty response
    And I should get airtel transaction enquiry response body in response
    And I should get the same status code from transaction enquiry response body as callback

  Scenario: MA-003 USSD Push Payment API Is Successful And CallBack Gives Failure Response
    Given I have MSISDN as "0003"
    And I have transaction id as "1278320"
    And I can mock USSD push payment request body
    When I call USSD push payment API with MSISDN with expected status of 200
    Then I should get non empty response
    And I should get airtel USSD push payment response body in response
    And I should get response with data field not null
    When I call send callback API with expected status of 200
    Then I should get non empty response
    And I should be able to extract status code from send callback response body
    And I should get status code as "TF"
    When I call the get transaction enquiry API with transactionID with expected status of 400
    Then I should get non empty response
    And I should get airtel transaction enquiry response body in response
    And I should get the same status code from transaction enquiry response body as callback
