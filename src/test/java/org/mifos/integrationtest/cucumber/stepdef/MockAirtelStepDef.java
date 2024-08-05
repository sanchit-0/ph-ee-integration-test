package org.mifos.integrationtest.cucumber.stepdef;

import static com.google.common.truth.Truth.assertThat;
import static org.mifos.integrationtest.common.Utils.CONTENT_TYPE;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.json.JSONException;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelCallBackRequestDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelEnquiryResponseDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentRequestDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentRequestSubscriberDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentRequestTransactionDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentResponseDTO;
import org.mifos.integrationtest.common.Utils;
import org.mifos.integrationtest.config.MockAirtelConfig;
import org.springframework.beans.factory.annotation.Autowired;

public class MockAirtelStepDef extends BaseStepDef {

    @Autowired
    MockAirtelConfig mockAirtelConfig;

    @Given("I have MSISDN as {string}")
    public void iHaveMSISDNAs(String msisdn) {
        scenarioScopeState.msisdn = msisdn;
        assertThat(scenarioScopeState.msisdn).isNotEmpty();
    }

    @And("I have transaction id as {string}")
    public void iHaveTransactionIdAs(String transactionId) {
        scenarioScopeState.transactionId = transactionId;
        assertThat(scenarioScopeState.transactionId).isNotEmpty();
    }

    @And("I can mock USSD push payment request body")
    public void iCanMockUSSDPushPaymentRequestBody() {
        // The mock Airtel API does not process or validate the request body.
        // Adding random values for the request body.
        AirtelPaymentRequestDTO airtelPaymentRequestDTO = new AirtelPaymentRequestDTO();
        airtelPaymentRequestDTO.setReference("Testing transaction");

        AirtelPaymentRequestSubscriberDTO airtelPaymentRequestSubscriberDTO = new AirtelPaymentRequestSubscriberDTO();
        airtelPaymentRequestSubscriberDTO.setCountry("KE");
        airtelPaymentRequestSubscriberDTO.setCurrency("KES");
        airtelPaymentRequestSubscriberDTO.setMsisdn(scenarioScopeState.msisdn);
        airtelPaymentRequestDTO.setSubscriber(airtelPaymentRequestSubscriberDTO);

        AirtelPaymentRequestTransactionDTO airtelPaymentRequestTransactionDTO = new AirtelPaymentRequestTransactionDTO();
        airtelPaymentRequestTransactionDTO.setAmount(1000);
        airtelPaymentRequestTransactionDTO.setCountry("KE");
        airtelPaymentRequestTransactionDTO.setCurrency("KES");
        airtelPaymentRequestTransactionDTO.setId(scenarioScopeState.transactionId);
        airtelPaymentRequestDTO.setTransaction(airtelPaymentRequestTransactionDTO);

        scenarioScopeState.mockAirtelUSSDPaymentRequest = airtelPaymentRequestDTO;
        logger.info("mockAirtelUSSDPaymentRequest: {}", scenarioScopeState.mockAirtelUSSDPaymentRequest);
        assertThat(scenarioScopeState.mockAirtelUSSDPaymentRequest).isNotNull();
    }

    @When("I call USSD push payment API with MSISDN with expected status of {int}")
    public void iCallUSSDPushPaymentAPIWithMSISDNWithExpectedStatusOf(int expectedStatus) {
        // The mock Airtel API does not process or validate headers.
        // Adding random values for headers.
        RequestSpecification requestSpec = Utils.getDefaultSpec();
        requestSpec.header(CONTENT_TYPE, "application/json");
        requestSpec.header("X-Country", "KE");
        requestSpec.header("X-Currency", "KES");
        requestSpec.header("Authorization", "Bearer UC23y1292w");
        requestSpec.header("x-signature", "MGsp1Hs683Ag==");
        requestSpec.header("x-key", "DVZCgY91bS==");
        scenarioScopeState.response = RestAssured.given(requestSpec).baseUri(mockAirtelConfig.airtelConnectorContactPoint)
                .body(scenarioScopeState.mockAirtelUSSDPaymentRequest).expect()
                .spec(new ResponseSpecBuilder().expectStatusCode(expectedStatus).build()).when()
                .post(mockAirtelConfig.mockAirtelUssdPushEndpoint).andReturn().asString();

        logger.info("USSD Push Payment Req response: {}", scenarioScopeState.response);
    }

    @When("I call send callback API with expected status of {int}")
    public void iCallSendCallbackAPIWithExpectedStatusOf(int expectedStatus) {
        RequestSpecification requestSpec = Utils.getDefaultSpec();
        requestSpec.header(CONTENT_TYPE, "application/json");
        scenarioScopeState.response = RestAssured.given(requestSpec).baseUri(mockAirtelConfig.airtelConnectorContactPoint)
                .body(scenarioScopeState.transactionId).expect().spec(new ResponseSpecBuilder().expectStatusCode(expectedStatus).build())
                .when().post(mockAirtelConfig.mockAirtelSendCallbackEndpoint).andReturn().asString();
        logger.info("send callback API: {}", scenarioScopeState.response);
    }

    @And("I should be able to extract status code from send callback response body")
    public void iShouldBeAbleToExtractStatusCodeFromSendCallbackResponseBody() {
        scenarioScopeState.transactionStatusCode = scenarioScopeState.airtelCallBackRequestDTO.getTransaction().getStatusCode();
    }

    @And("I should get status code as {string}")
    public void iShouldGetStatusCodeAs(String statusCode) {
        assertThat(scenarioScopeState.transactionStatusCode.equalsIgnoreCase(statusCode));
    }

    @And("I should get airtel USSD push payment response body in response")
    public void iShouldGetAirtelUSSDPushPaymentResponseBodyInResponse() throws JSONException {
        try {
            scenarioScopeState.airtelPaymentResponseDTO = objectMapper.readValue(scenarioScopeState.response,
                    AirtelPaymentResponseDTO.class);
        } catch (Exception e) {
            scenarioScopeState.airtelPaymentResponseDTO = null;
        }
    }

    @And("I should get airtel callback response body in response")
    public void iShouldGetAirtelCallbackResponseBodyInResponse() throws JSONException {
        try {
            scenarioScopeState.airtelCallBackRequestDTO = objectMapper.readValue(scenarioScopeState.response,
                    AirtelCallBackRequestDTO.class);
        } catch (Exception e) {
            scenarioScopeState.airtelPaymentResponseDTO = null;
        }
    }

    @And("I should get response with data field null")
    public void iShouldGetResponseWithDataFieldNull() {
        assertThat(scenarioScopeState.airtelPaymentResponseDTO.getData()).isNull();
    }

    @And("I should get response with data field not null")
    public void iShouldGetResponseWithDataFieldNotNull() {
        assertThat(scenarioScopeState.airtelPaymentResponseDTO.getData()).isNotNull();
    }

    @When("I call the get transaction enquiry API with transactionID with expected status of {int}")
    public void iCallTheGetTransactionEnquiryAPIWithTransactionIdWithExpectedStatusOf(int expectedStatus) {
        RequestSpecification requestSpec = Utils.getDefaultSpec();
        scenarioScopeState.response = RestAssured.given(requestSpec).baseUri(mockAirtelConfig.airtelConnectorContactPoint).expect()
                .spec(new ResponseSpecBuilder().expectStatusCode(expectedStatus).build()).when()
                .get(mockAirtelConfig.mockAirtelTransactionEnquiryEndpoint + scenarioScopeState.transactionId).andReturn().asString();

        logger.info("Transaction Enquiry response: {}", scenarioScopeState.response);
    }

    @And("I should get airtel transaction enquiry response body in response")
    public void iShouldGetAirtelTransactionEnquiryResponseBodyInResponse() throws JSONException {
        try {
            scenarioScopeState.airtelEnquiryResponseDTO = objectMapper.readValue(scenarioScopeState.response,
                    AirtelEnquiryResponseDTO.class);
        } catch (Exception e) {
            scenarioScopeState.airtelEnquiryResponseDTO = null;
        }
    }

    @And("I should get the same status code from transaction enquiry response body as callback")
    public void iShouldGetTheSameStatusCodeFromTransactionEnquiryResponseBodyAsCallback() {
        String transactionEnquiryStatusCode = scenarioScopeState.airtelEnquiryResponseDTO.getData().getTransaction().getStatus();
        assertThat(transactionEnquiryStatusCode.equalsIgnoreCase(scenarioScopeState.transactionStatusCode));
    }
}
