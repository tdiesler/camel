<?xml version="1.0" encoding="UTF-8"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<Profile xmlns="http://soap.sforce.com/2006/04/metadata">
    <applicationVisibilities>
        <application>Warehouse</application>
        <default>false</default>
        <visible>true</visible>
    </applicationVisibilities>
    <classAccesses>
        <apexClass>MerchandiseRestResource</apexClass>
        <enabled>true</enabled>
    </classAccesses>
    <classAccesses>
        <apexClass>MerchandiseRestResourceTests</apexClass>
        <enabled>false</enabled>
    </classAccesses>
    <classAccesses>
        <apexClass>UpdateProfile</apexClass>
        <enabled>false</enabled>
    </classAccesses>
    <custom>true</custom>
    <fieldPermissions>
        <editable>true</editable>
        <field>Account.Shipping_Location__c</field>
        <readable>true</readable>
    </fieldPermissions>
    <fieldPermissions>
        <editable>true</editable>
        <field>Invoice__c.Status__c</field>
        <readable>true</readable>
    </fieldPermissions>
    <fieldPermissions>
        <editable>true</editable>
        <field>Line_Item__c.Merchandise__c</field>
        <readable>true</readable>
    </fieldPermissions>
    <fieldPermissions>
        <editable>true</editable>
        <field>Line_Item__c.Unit_Price__c</field>
        <readable>true</readable>
    </fieldPermissions>
    <fieldPermissions>
        <editable>true</editable>
        <field>Line_Item__c.Units_Sold__c</field>
        <readable>true</readable>
    </fieldPermissions>
    <fieldPermissions>
        <editable>true</editable>
        <field>Merchandise__c.Description__c</field>
        <readable>true</readable>
    </fieldPermissions>
    <layoutAssignments>
        <layout>Invoice__c-Invoice Layout</layout>
    </layoutAssignments>
    <layoutAssignments>
        <layout>Line_Item__c-Line Item Layout</layout>
    </layoutAssignments>
    <layoutAssignments>
        <layout>Merchandise__c-Merchandise Layout</layout>
    </layoutAssignments>
    <objectPermissions>
        <allowCreate>true</allowCreate>
        <allowDelete>true</allowDelete>
        <allowEdit>true</allowEdit>
        <allowRead>true</allowRead>
        <modifyAllRecords>true</modifyAllRecords>
        <object>Invoice__c</object>
        <viewAllRecords>true</viewAllRecords>
    </objectPermissions>
    <objectPermissions>
        <allowCreate>true</allowCreate>
        <allowDelete>true</allowDelete>
        <allowEdit>true</allowEdit>
        <allowRead>true</allowRead>
        <modifyAllRecords>true</modifyAllRecords>
        <object>Line_Item__c</object>
        <viewAllRecords>true</viewAllRecords>
    </objectPermissions>
    <objectPermissions>
        <allowCreate>true</allowCreate>
        <allowDelete>true</allowDelete>
        <allowEdit>true</allowEdit>
        <allowRead>true</allowRead>
        <modifyAllRecords>true</modifyAllRecords>
        <object>Merchandise__c</object>
        <viewAllRecords>true</viewAllRecords>
    </objectPermissions>
    <tabVisibilities>
        <tab>Invoice__c</tab>
        <visibility>DefaultOn</visibility>
    </tabVisibilities>
    <tabVisibilities>
        <tab>Merchandise__c</tab>
        <visibility>DefaultOn</visibility>
    </tabVisibilities>
    <userLicense>Salesforce</userLicense>
    <userPermissions>
        <enabled>true</enabled>
        <name>ActivateContract</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ActivateOrder</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>AllowUniversalSearch</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>AllowViewKnowledge</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ApiEnabled</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>AssignPermissionSets</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>AssignTopics</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>AuthorApex</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>BulkApiHardDelete</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>BulkMacrosAllowed</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>CanInsertFeedSystemFields</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>CanUseNewDashboardBuilder</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ChatterEditOwnPost</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ChatterEditOwnRecordPost</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ChatterFileLink</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ChatterInternalUser</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ChatterInviteExternalUsers</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ChatterOwnGroups</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ConnectOrgToEnvironmentHub</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ContentAdministrator</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ConvertLeads</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>CreateCustomizeDashboards</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>CreateCustomizeFilters</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>CreateCustomizeReports</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>CreateDashboardFolders</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>CreatePackaging</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>CreateReportFolders</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>CreateTopics</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>CreateWorkspaces</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>CustomizeApplication</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>DelegatedTwoFactor</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>DeleteActivatedContract</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>DeleteTopics</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>DistributeFromPersWksp</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditActivatedOrders</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditBillingInfo</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditBrandTemplates</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditCaseComments</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditEvent</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditHtmlTemplates</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditKnowledge</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditMyDashboards</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditMyReports</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditOppLineItemUnitPrice</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditPublicDocuments</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditPublicFilters</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditPublicTemplates</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditReadonlyFields</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditTask</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EditTopics</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EmailMass</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EmailSingle</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>EnableNotifications</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ExportReport</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ImportCustomObjects</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ImportLeads</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ImportPersonal</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>InstallPackaging</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>LightningExperienceUser</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageAnalyticSnapshots</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageAuthProviders</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageBusinessHourHolidays</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageCallCenters</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageCases</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageCategories</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageContentPermissions</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageContentProperties</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageContentTypes</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageCustomPermissions</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageCustomReportTypes</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageDashbdsInPubFolders</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageDataCategories</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageDataIntegrations</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageDynamicDashboards</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageEmailClientConfig</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageExchangeConfig</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageInteraction</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageInternalUsers</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageIpAddresses</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageKnowledge</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageKnowledgeImportExport</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageLeads</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageLoginAccessPolicies</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageMobile</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageNetworks</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManagePackageLicenses</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManagePasswordPolicies</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageProfilesPermissionsets</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManagePvtRptsAndDashbds</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageRemoteAccess</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageReportsInPubFolders</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageRoles</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageSearchPromotionRules</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageSharing</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageSolutions</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageSynonyms</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageUnlistedGroups</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ManageUsers</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>MassInlineEdit</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>MergeTopics</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ModerateChatter</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ModifyAllData</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>NewReportBuilder</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>PublishPackaging</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ResetPasswords</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>RunReports</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ScheduleReports</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>SelectFilesFromSalesforce</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>SendSitRequests</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ShareInternalArticles</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ShowCompanyNameAsUserBadge</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>SolutionImport</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>SubmitMacrosAllowed</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>SubscribeToLightningReports</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>TransferAnyCase</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>TransferAnyEntity</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>TransferAnyLead</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>UseTeamReassignWizards</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ViewAllData</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ViewAllUsers</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ViewDataCategories</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ViewEventLogFiles</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ViewHelpLink</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ViewMyTeamsDashboards</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ViewPublicDashboards</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ViewPublicReports</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ViewSetup</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>WorkCalibrationUser</name>
    </userPermissions>
    <userPermissions>
        <enabled>true</enabled>
        <name>ModifyMetadata</name>
    </userPermissions>
</Profile>
