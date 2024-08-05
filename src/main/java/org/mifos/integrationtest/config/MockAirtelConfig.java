package org.mifos.integrationtest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "airtel-connector.mock-airtel")
@Component
public class MockAirtelConfig {

    @Value("${airtel-connector.contactpoint}")
    public String airtelConnectorContactPoint;

    @Value("${airtel-connector.mock-airtel.endpoints.mock-airtel-ussd-push}")
    public String mockAirtelUssdPushEndpoint;

    @Value("${airtel-connector.mock-airtel.endpoints.mock-airtel-transaction-enquiry}")
    public String mockAirtelTransactionEnquiryEndpoint;

    @Value("${airtel-connector.mock-airtel.endpoints.send-callback}")
    public String mockAirtelSendCallbackEndpoint;
}
