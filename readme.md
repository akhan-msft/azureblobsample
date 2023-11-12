

## Azure Blob Storage Sample
This is a sample spring boot application that shows how to upload files to an Azure blob storage account using the Azure BLOB Java SDK, it also illustrates how to create dynamically created SAS tokens using a "User Delegation Key" via an application registered in Azure AD

### Prerequisites
- Azure subscription/account
- Azure storage account and a Blob storage container already created
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

4. Save the client ID and secret to configure in your application

### Build & deploy the java application
1. Update the application.properties file to provide your Azure Blob storage account details, also configure the following properties from the app registration steps above
```shell
   azure.client.id=${AZURE_CLIENT_ID}
   azure.client.secret=${AZURE_CLIENT_SECRET}
   azure.tenant.id=${AZURE_TENANT_ID}
```
2. Update the maven pom.xml to provide azure app service configuration details, this is needed to use the maven plugin to deploy the application to Azure App Service using mvn commands
3. Run the following command to build the deployable jar
    ```shell
    mvn clean package
    ```
4. Run the application locally, ensure you have set the 3 environment variables from step 1 in your IDE's terminal 
    ```shell
    mvn spring-boot:run
    ```


