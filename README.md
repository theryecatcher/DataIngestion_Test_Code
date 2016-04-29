##DataIngestion_Test_Code

Test code for Data Ingestion service hosted on predix

Contains the required code to setup a basic Data Ingestion Wrapper Service.
- Rest Client
- Timeseries Bootstrap
- Data Ingestion Service
- Asset Bootstrap

### Rest Client, Timeseries Bootstrap, Asset Bootstrap
The default bootstrap classes provided by GE to build your application on. Rest Client to make necessary REST Calls from the application, mainly used for UAA as of now. Timseries Bootstrap has the beans and classes to connect to the timeseries URL's using the Timeseries service. Asset Bootstrap has the beans and classes to query and update the assets using the asset services. The details are avilable in the respective links below.

- [Predix Asset](https://github.com/predixdev/asset-bootstrap/tree/03313adaca23d200261986f114d92c2fb96c4475)
- [Predix Timeseries](https://github.com/predixdev/timeseries-bootstrap/tree/040140d36dc754531b5b8f4805c2856362644716)
- [Predix Rest Client](https://github.com/predixdev/predix-rest-client/tree/a6aed1f24c1ef2a85bdc6bf18fe44c5070e344d4)

### Data Ingestion Service
This is the modified application from The base Predix Data Ingestion Service used in the RMD Ref App. The modifications allow developer to push data to any timeseries service irrespective of which zone-id he/she is targetting.####(UNTESTED)
For building/editing the applicatio to suit your purposes, please follow the below.
All the three projects have to be imported to the Spring Tool Suite. It automatically will download all the required jar files if connected to open internet (Without company firewall).
Once imported and after modifications the project has to be rebuilt and deployed once again with th ehelp of manifest.yml file.

###Usage
The current code has already been deployed on our workspace (but is inaccessible due to some cloud space issues have raised a ticket for the same). But once up or deployed as is on your side it can be used for the below purposes:

- ping
  *	Type : GET
  *	Returns a string “Successful Ping”

- SaveTimeSeriesData
  *	Type : POST
  *	Headers
    +	Authorization – bearer token
    +	Content-Type - application/json
  *	Parameters
    +	clientId - <UAA Client ID
    +	tenantId- Timeseries Instance Zone ID
    +	content - Data to be posted

 Note: 
 If the Authorization is not passed explicitly it will go and save in the credentials that are by default associated in the application, in this case ours.

- ingestdata
  *	Type : POST
  *	Headers
    +	Authorization – bearer <token>
  *	Parameters
    +	filename - Filename
    +	clientId - UAA Client ID
    +	tenantId- Timeseries Instance Zone ID
    +	file - Data to be posted as a file

Data format for Ingestion data
[{"sensorName":"Tst5","SensorID":"TST5","MaxValue":"1022","MinValue":"15","SensorReadings":[{"epoch":"1456552800000","value":"25"}, {"epoch":"1456556400000","value":"34"}, {"epoch":"1456560000000","value":"17"}, {"epoch":"1456563600000","value":"30"},...]}]

All the above are endpoints to be provided in the URL's you derive after you host the application on the Predix Cloud.
In this case the published the published URL is [LINK](http://http://test-dataingestion-service-demo-1.run.aws-usw02-pr.ice.predix.io/ping)

The Authorization token and the Predix Timeseries Zone ID has to be kept in hand to be able to use the above application.

