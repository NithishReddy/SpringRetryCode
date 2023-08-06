package com.example.springretrytesting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

class ErrorMapping{
	@JsonProperty("status")
	private int status;
	@JsonProperty("error")
	private String error;
	@JsonProperty("message")
	private String message;
}

@SpringBootApplication
public class SpringretrytestingApplication {

	RestTemplate restTemplate = new RestTemplate();

	public static void main(String[] args) {
		//SpringApplication.run(SpringretrytestingApplication.class, args);
		SpringretrytestingApplication s = new SpringretrytestingApplication();

		String payload = "{\"key1\":\"value1\"}";

		String UriString = "";
		try{
			s.doRetryWithCondition("https://abc.com/post", payload);
		}
		catch(Exception ex){
			System.out.println("ENDING*******************" + ex);
			ex.printStackTrace();
		}

	}

	private String doRetryWithCondition(String UriString, String request){

		// Define the retry condition for the CustomRetryPolicy
		RetryCondition retryCondition = (statusCode, errorMessage) -> {
			System.out.println("errorMessage" + errorMessage);
			System.out.println("statusCode" + statusCode);
			// Your specific condition evaluation logic here
			// For example, retry only if the status code is 500 and the error message matches a specific condition
			System.out.println("StatusCode" + statusCode + statusCode.getClass() + "/" + HttpStatus.INTERNAL_SERVER_ERROR.value());
			System.out.println("errorMessage" + errorMessage);

			if (statusCode instanceof Integer) {
				int statusCodeInt = (Integer) statusCode;
				System.out.println("StatusCodassae: " + statusCodeInt + " / " + HttpStatus.INTERNAL_SERVER_ERROR.value());
				System.out.println("ErrorMessage: " + errorMessage);
				
				return statusCodeInt == HttpStatus.INTERNAL_SERVER_ERROR.value() && errorMessage != null && "Expired token".equalsIgnoreCase(errorMessage);
				
			}
			return false;
		};

		int MAX_RETY_ATTEMPTS = 1;

		System.out.println("******************1");

		// Create a custom retry template with a fixed back-off policy, custom retry policy, and custom retry listener
		RetryTemplate retryTemplate = new RetryTemplate();
		FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
		fixedBackOffPolicy.setBackOffPeriod(10000);
		retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
		retryTemplate.setRetryPolicy(new CustomRetryPolicy(MAX_RETY_ATTEMPTS, retryCondition));
		CustomRetryListener retryListener =  new CustomRetryListener(retryCondition);
		//retryTemplate.registerListener(new CustomRetryListener(retryCondition));
		retryTemplate.registerListener(retryListener);

		System.out.println("******************2");
		// Perform the request with retry
		RetryCallback<String, Exception> retryCallback = retryContext -> {
			// Modify the request before each retry (if needed)
			// For example, you can change the URL, headers, or payload data in the restTemplate
			// restTemplate.setXXX(...);
			// Perform your retryable operation here
			// For example, invoke a REST API using RestTemplate
			// Perform your retryable operation here
			// For example, invoke a REST API using RestTemplate

			RequestEntity<?> requestEntity = RequestEntity
					.post("urltohit")
					.contentType(MediaType.APPLICATION_JSON)
					.body(request);
					ResponseEntity<String> response = null;	

			CustomRetryContext customRetryContext = (CustomRetryContext) retryContext;

			try{
				response = restTemplate.exchange(requestEntity, String.class);
			}
			catch(HttpServerErrorException.InternalServerError ex){
				
				if (ex.getResponseBodyAsString().contains("Expired token")) {
					//System.out.println("response final exception" + jsonNode.getStatus() + "...." + jsonNode.getMessage());
					boolean shouldRetry = retryCondition.evaluate(500, "Expired token");
					if (shouldRetry && customRetryContext.getRetryCount() < MAX_RETY_ATTEMPTS) {
						customRetryContext.setShouldRetry(shouldRetry);
						System.out.println("***************CUSTOM EXXSS");
						customRetryContext.incrementRetryCount();
							// Increment the retry count
					}else{
						customRetryContext.setShouldRetry(false);		
					}
				}else {
					customRetryContext.setShouldRetry(false);
				}
				throw ex;
			}
			catch (Exception ex){
				customRetryContext.setShouldRetry(false);
				((CustomRetryContext) retryContext).setLastException(ex);
				throw ex;
			}
			return response.getBody();
		};

		try {
			return retryTemplate.execute(retryCallback);
		} catch (Exception ex) {
			Throwable originalException = retryListener.getLastException();
			// Handle other exceptions after all retries are exhausted (if needed).
			if (originalException != null) {
				throw new CustomException("" + originalException);
			} else {
				throw new CustomException("" + ex); // If original exception not available, throw the last exception encountered during retries
			}
		}
	}

}
