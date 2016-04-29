package com.ge.predix.solsvc.dataingestion.handler;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ge.predix.solsvc.bootstrap.tbs.entity.InjectionBody;
import com.ge.predix.solsvc.bootstrap.tbs.entity.InjectionMetric;
import com.ge.predix.solsvc.bootstrap.tbs.entity.InjectionMetricBuilder;
import com.ge.predix.solsvc.bootstrap.tsb.client.TimeseriesWSConfig;
import com.ge.predix.solsvc.bootstrap.tsb.factories.TimeseriesFactory;
import com.ge.predix.solsvc.dataingestion.service.type.SensorDetails;
import com.ge.predix.solsvc.dataingestion.websocket.WSClientEndpointConfig;
import com.ge.predix.solsvc.dataingestion.websocket.WebSocketClient;
import com.ge.predix.solsvc.dataingestion.websocket.WebSocketConfig;

/**
 * 
 * @author predix -
 */
@Component
public class TimeSeriesDataIngestionHandler extends BaseFactoryIT
{
    private static Logger tsDiHandlerlog = Logger.getLogger(TimeSeriesDataIngestionHandler.class);
    @Autowired
    private TimeseriesFactory timeSeriesFactory;
        
	@Autowired
	private TimeseriesWSConfig tsInjectionWSConfig;

	@Autowired
	private WebSocketConfig wsConfig;
	
	@Autowired
	private WebSocketClient wsClient;
	
    /**
     *  -
     */
    @SuppressWarnings("nls")
    @PostConstruct
    public void intilizeDataIngestionHandler()
    {
    	tsDiHandlerlog.info("*******************TimeSeriesDataIngestionHandler Initialization complete*********************");
    }

    @Override
    @SuppressWarnings("nls")
    public void handleData(String tenentId, String controllerId, String data, String authorization)
    {
    	tsDiHandlerlog.info(data);
        if (StringUtils.isEmpty(authorization)) {
        	tsDiHandlerlog.info("reading credentials from "+restConfig.getOauthClientId());
        	String[] oauthClient  = restConfig.getOauthClientId().split(":");
        	authorization = "Bearer "+getRestTemplate(oauthClient[0],oauthClient[1]).getAccessToken().getValue();
        }
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            List<SensorDetails> list = mapper.readValue(data, new TypeReference<List<SensorDetails>>()
            {
                //
            });
            tsDiHandlerlog.info("TimeSeries URL : " + this.tsInjectionWSConfig.getInjectionUri() );
            tsDiHandlerlog.info("WebSocket URL : " + this.wsConfig.getPredixTsWebSocketURI());
            for (SensorDetails details : list)
            {
            	WSClientEndpointConfig.SetAuthorizationAndZone(authorization, tenentId);
            	
                InjectionMetricBuilder builder = InjectionMetricBuilder.getInstance();
                InjectionMetric metric = new InjectionMetric(new Long(System.currentTimeMillis()));
                InjectionBody body = new InjectionBody(details.sensorName);
                
                /*log.info(details.sensorName);
                log.info(details.SensorID);
                log.info(details.MaxValue);
                log.info(details.MinValue);*/
                body.addAttributes("sourceTagName",details.sensorName);
                body.addAttributes("sourceTagID",details.SensorID);
                body.addAttributes("MaxValue",details.MaxValue);
                body.addAttributes("MinValue",details.MinValue);
                
                int count = details.SensorReadings.size();
                for (int i = 0 ; i <count; i++)
                {
                	// log.info(details.SensorReadings.get(i).epoch);
                	// log.info(details.SensorReadings.get(i).value);
                    body.addDataPoint(Long.parseLong(details.SensorReadings.get(i).epoch),Double.parseDouble(details.SensorReadings.get(i).value));
                }
                
                metric.getBody().add(body);
                builder.addMetrics(metric);
                
                this.timeSeriesFactory.create(builder);
                
                tsDiHandlerlog.info("Added Data to Timeseries");
                                                
                wsClient.postToWebSocketServer(builder.build());
                tsDiHandlerlog.info("Posted Data to Timeseries Websocket Server");
            }                                    
        }
        catch (Exception e)
        {
        	tsDiHandlerlog.error("Error : Error in run data");
        }
    }
    
    @SuppressWarnings("nls")
    public void getTags()
    {
    	
    }

    @SuppressWarnings("nls")
    private OAuth2RestTemplate getRestTemplate(String clientId, String clientSecret)
    {
        // get token here based on username password;
        // ResourceOwnerPasswordResourceDetails resourceDetails = new ResourceOwnerPasswordResourceDetails();
        ClientCredentialsResourceDetails clientDetails = new ClientCredentialsResourceDetails();
        clientDetails.setClientId(clientId);
        clientDetails.setClientSecret(clientSecret);
        String url = this.restConfig.getOauthResourceProtocol() + "://" + this.restConfig.getOauthRestHost()
                + this.restConfig.getOauthResource();
        clientDetails.setAccessTokenUri(url);
        clientDetails.setGrantType("client_credentials");
       
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(clientDetails);

        return restTemplate;
    }
}
