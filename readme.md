## Azure Blob Storage Sample
This is a sample spring boot application that shows how to upload files to an Azure blob storage account using the Azure BLOB Java SDK, it also illustrates how to create dynamically create SAS tokens using a "User Delegation Key" approach via an application registered in Azure AD. A reference to this approach can be found here-> https://learn.microsoft.com/en-us/rest/api/storageservices/create-user-delegation-sas

### Prerequisites
- Azure subscription/account
- Azure storage account and a Blob storage container already provisioned
- Azure App Registration as per the steps below

### Create an app registration using Azure cli

1. Create an App Registration in Azure AD.
    ```shell
    $app = az ad app create --display-name "YourAppName"
    ```

2. Generate a client secret for the App Registration.
    ```shell
    # Get App Registration details
    $appId = $app.appId
    $secret = az ad app credential reset --id $appId --append
    ```

3. Assign the "Blob Storage Account Administrator" role to the App's service principal.
    ```shell
    $spId = az ad sp create --id $appId
    az role assignment create --assignee $spId --role "Storage Blob Data Contributor" --scope /subscriptions/<your-subscription-id>/resourceGroups/<resource-group-name>/providers/Microsoft.Storage/storageAccounts/<storage-account-name>
    ```

4. Configure the client Id, secret and tenant Id as environment variables for your application as referenced in the application.properties file. You can set these using powershell commands as shown below
    ```
    $env:AZURE_CLIENT_ID=<<your_client_id>>
    $env:AZURE_CLIENT_SECRET = <your_secret>
    $env:AZURE_TENANT_ID = <your_secret>   
    ```

### Build & deploy the Java application
1. Update the application.properties file to provide your Azure Blob storage account details, also configure the following properties from the app registration steps above
```shell
   azure.client.id=${AZURE_CLIENT_ID}
   azure.client.secret=${AZURE_CLIENT_SECRET}
   azure.tenant.id=${AZURE_TENANT_ID}
``` 
2. Run the following command to build the deployable jar
    ```shell
    mvn clean package
    ```
3. Run the application locally
    ```shell
    mvn spring-boot:run
    ```


