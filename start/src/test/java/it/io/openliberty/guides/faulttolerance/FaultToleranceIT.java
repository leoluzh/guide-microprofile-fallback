package it.io.openliberty.guides.faulttolerance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.io.openliberty.guides.utils.TestUtils;

public class FaultToleranceIT {

	private Response response;
	private Client client;
	
	@BeforeEach
	public void setup() {
		client = ClientBuilder.newClient();
		client.register(JsrJsonpProvider.class);
	}
	
	@AfterEach
	public void teardown() {
		client.close();
		response.close();
	}
	
	@Test
	public void testFallbackForGet() throws InterruptedException {
		
		response = TestUtils.getResponse(
				client, 
				TestUtils.INVENTORY_LOCALHOST_URL );
		
		assertResponse( TestUtils.baseUrl , response );
		
		JsonObject obj = response.readEntity(JsonObject.class);
		
		int propertiesSize = obj.size();
		
		TestUtils.changeSystemProperty(
				TestUtils.SYSTEM_MAINTENANCE_FALSE , 
				TestUtils.SYSTEM_MAINTENANCE_TRUE );
		
		Thread.sleep(3000);
		
		response = TestUtils.getResponse(
				client, 
				TestUtils.INVENTORY_LOCALHOST_URL);
		
		assertResponse( TestUtils.baseUrl , response );
		obj = response.readEntity(JsonObject.class);
		int propertiesSizeFallBack = obj.size();
		
		assertTrue( propertiesSize > propertiesSizeFallBack , 
				"The total number of properties from the @Fallback method" 
				+ "is not smaller than the number from the system service"
				+ " as excepted.");
		
		TestUtils.changeSystemProperty( 
				TestUtils.SYSTEM_MAINTENANCE_TRUE , 
				TestUtils.SYSTEM_MAINTENANCE_FALSE );
		
		Thread.sleep(3000);
		
	}
	
	@Test
	public void testFallbackSkipForGet() {
		response = TestUtils.getResponse(
				client, 
				TestUtils.INVENTORY_UNKNOWN_HOST_URL);
		assertResponse(TestUtils.baseUrl, response , 404 );
		assertTrue( 
				response.readEntity(String.class).contains("error") ,
				"Incorrect response body from " + TestUtils.INVENTORY_UNKNOWN_HOST_URL
				);
	}
	
	private void assertResponse( String url , Response response , int status_code ) {
		assertEquals( status_code , response.getStatus() , "Incorrect response code from " + url );
	}
	
	private void assertResponse( String url , Response response ) {
		assertResponse( url , response , 200 );
	}
	
}
