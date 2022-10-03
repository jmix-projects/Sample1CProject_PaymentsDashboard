package com.company.samplebankaccountingproject.app;

import com.company.samplebankaccountingproject.entity.*;
import io.jmix.appsettings.AppSettings;
import io.jmix.core.DataManager;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetIteratorRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.*;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.http.BasicAuthHttpClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;

@Service
public class ExchangeOData {

    @Autowired
    private AppSettings appSettings;
    @Autowired
    private DataManager dataManager;
    private final ODataClient client;

    public ExchangeOData() {
        client = ODataClientFactory.getClient();
    }

    private ClientEntitySetIterator<ClientEntitySet, ClientEntity> getIterator(URI customURI) {
        ODataSettings oDataSettings = appSettings.load(ODataSettings.class);

        String user = oDataSettings.getODataUser();
        String pass = oDataSettings.getODataPassword();

        client.getConfiguration().setHttpClientFactory(
                new BasicAuthHttpClientFactory(user, pass == null ? "" : pass)
        );

        ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = client
                .getRetrieveRequestFactory()
                .getEntitySetIteratorRequest(customURI);
        request.setAccept("application/json");
        ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = request.execute();

        return response.getBody();
    }

    public void loadCustomers() {
        ODataSettings oDataSettings = appSettings.load(ODataSettings.class);

        String baseURL = oDataSettings.getODataURL();

        URI customURI =
                client.newURIBuilder(baseURL)
                        .appendEntitySetSegment("Catalog_Контрагенты")
                        .addCustomQueryOption("$select",
                                "Ref_Key,Description,ИНН,КПП,ЮридическоеФизическоеЛицо," +
                                        "РегистрационныйНомер,ДокументУдостоверяющийЛичность")
                        .addCustomQueryOption("$filter", "IsFolder eq false")
                        .build();

        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator = getIterator(customURI);

        while (iterator.hasNext()) {
            ClientEntity ce = iterator.next();
            String type = ce.getProperty("ЮридическоеФизическоеЛицо").getValue().toString();
            String id1C = ce.getProperty("Ref_Key").getValue().toString();

            if (type.equals("ЮридическоеЛицо")) {

                String inn = ce.getProperty("ИНН").getValue().toString();
                if (dataManager.load(CustomerLegal.class)
                        .query("select c from CustomerLegal c where c.inn = :inn1")
                        .parameter("inn1", inn)
                        .optional()
                        .isEmpty())
                {
                    CustomerLegal customer = dataManager.create(CustomerLegal.class);
                    customer.setType("LEGAL");
                    customer.setId(UUID.randomUUID());
                    customer.setId1C(id1C);
                    customer.setName(ce.getProperty("Description").getValue().toString());
                    customer.setInn(ce.getProperty("ИНН").getValue().toString());
                    customer.setKpp(ce.getProperty("КПП").getValue().toString());
                    customer.setOgrn(ce.getProperty("РегистрационныйНомер").getValue().toString());
                    dataManager.save(customer);
                }

            } else if (type.equals("ФизическоеЛицо")) {

                if (dataManager.load(CustomerPrivate.class)
                        .query("select c from CustomerPrivate c where c.id1C = :id1C1")
                        .parameter("id1C1", id1C)
                        .optional()
                        .isEmpty())
                {
                    CustomerPrivate customer = dataManager.create(CustomerPrivate.class);
                    customer.setType("PRIVATE");
                    customer.setId(UUID.randomUUID());
                    customer.setId1C(id1C);
                    customer.setName(ce.getProperty("Description").getValue().toString());
                    customer.setPassportID("");
                    dataManager.save(customer);
                }
            }
        }
    }

    public void loadIncomingDescriptions() {
        ODataSettings oDataSettings = appSettings.load(ODataSettings.class);

        String baseURL = oDataSettings.getODataURL();

        URI customURI =
                client.newURIBuilder(baseURL)
                        .appendEntitySetSegment("Catalog_СтатьиДвиженияДенежныхСредств")
                        .addCustomQueryOption("$select",
                                "Ref_Key,Description")
                        .addCustomQueryOption("$filter", "IsFolder eq false")
                        .build();

        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator = getIterator(customURI);

        while (iterator.hasNext()) {
            ClientEntity ce = iterator.next();

            String description = ce.getProperty("Description").getValue().toString();
            if (dataManager.load(IncomingDescription.class)
                    .query("select c from IncomingDescription c where c.description = :description")
                    .parameter("description", description)
                    .optional()
                    .isEmpty())
            {
                IncomingDescription entity = dataManager.create(IncomingDescription.class);
                entity.setId(UUID.randomUUID());
                entity.setName(description);
                dataManager.save(entity);
            }
        }
    }

    public void loadBankAccounts() {
        ODataSettings oDataSettings = appSettings.load(ODataSettings.class);

        String baseURL = oDataSettings.getODataURL();

        URI customURI =
                client.newURIBuilder(baseURL)
                        .appendEntitySetSegment("Catalog_БанковскиеСчета")
                        .addCustomQueryOption("$select",
                                "Ref_Key,Description,НомерСчета,Owner")
                        .build();

        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator = getIterator(customURI);

        while (iterator.hasNext()) {
            ClientEntity ce = iterator.next();

            String accountNumber = ce.getProperty("НомерСчета").getValue().toString();
            if (dataManager.load(BankAccount.class)
                    .query("select c from BankAccount c where c.accountNumber = :accountNumber")
                    .parameter("accountNumber", accountNumber)
                    .optional()
                    .isEmpty())
            {
                String owner = ce.getProperty("Owner").getValue().toString();
                Optional<Customer> optional = dataManager.load(Customer.class).id(owner).optional();
                if (optional.isEmpty()) {
                    BankAccount entity = dataManager.create(BankAccount.class);
                    entity.setId(UUID.randomUUID());
                    entity.setAccountNumber(accountNumber);
                    entity.setName(ce.getProperty("Description").getValue().toString());
                    dataManager.save(entity);
                }
            }
        }
    }

    public void loadDocuments() {
        ODataSettings oDataSettings = appSettings.load(ODataSettings.class);

        String baseURL = oDataSettings.getODataURL();

        URI tabURI =
                client.newURIBuilder(baseURL)
                        .appendEntitySetSegment("Document_ПоступлениеНаРасчетныйСчет_РасшифровкаПлатежа")
                        .addCustomQueryOption("$select",
                                "Ref_Key,СчетНаОплату,СуммаПлатежа")
                        .build();

        ClientEntitySetIterator<ClientEntitySet, ClientEntity> tabIterator = getIterator(tabURI);

        Map<String, ClientEntity> table = new HashMap<>();
        while (tabIterator.hasNext()) {
            ClientEntity ce = tabIterator.next();

            String refKey = ce.getProperty("Ref_Key").getValue().toString();
            table.put(refKey, ce);
        }

        URI docURI =
                client.newURIBuilder(baseURL)
                        .appendEntitySetSegment("Document_ПоступлениеНаРасчетныйСчет")
                        .addCustomQueryOption("$select",
                                "Ref_Key,Date,Number,СчетОрганизации,Контрагент,ВидОперации")
                        .addCustomQueryOption("$filter",
                                "ВидОперации eq 'ОплатаПокупателя'")
                        .build();

        ClientEntitySetIterator<ClientEntitySet, ClientEntity> docIterator = getIterator(docURI);

        Double totalSum = .0;
        while (docIterator.hasNext()) {
            ClientEntity ce = docIterator.next();

            String refKey = ce.getProperty("Ref_Key").getValue().toString();
            String docNumber = ce.getProperty("Number").getValue().toString();

            ClientEntity ceTable = table.get(refKey);
            //String order = ceTable.getProperty("СчетНаОплату").getValue().toString();
            ClientPrimitiveValue cv = ceTable.getProperty("СуммаПлатежа").getPrimitiveValue();
            Double sum = .0;
            if (cv != null) {
                if (cv.getTypeName().equals("Edm.Double")) {
                    sum = (Double) cv.toValue();
                } else {
                    sum = Double.valueOf((Integer) cv.toValue());
                }
            }
            totalSum += sum;
        }
    }
}