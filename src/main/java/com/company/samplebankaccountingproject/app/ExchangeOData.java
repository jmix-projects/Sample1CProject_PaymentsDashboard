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

import java.math.BigDecimal;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private String getStringValue(ClientEntity ce, String property) {
        ClientProperty clientProperty = ce.getProperty(property);
        if (clientProperty != null) {
            return clientProperty.getValue().toString();
        } else {
            return null;
        }
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
            String type = getStringValue(ce, "ЮридическоеФизическоеЛицо");
            String id1C = getStringValue(ce, "Ref_Key");

            if (type == null || id1C == null) {
                continue;
            }

            if (type.equals("ЮридическоеЛицо")) {

                String inn = getStringValue(ce, "ИНН");
                if (inn != null) {
                    if (dataManager.load(CustomerLegal.class)
                            .query("select c from CustomerLegal c where c.inn = :inn1")
                            .parameter("inn1", inn)
                            .optional()
                            .isEmpty()) {
                        CustomerLegal customer = dataManager.create(CustomerLegal.class);
                        customer.setType("LEGAL");
                        customer.setId(UUID.randomUUID());
                        customer.setId1C(id1C);
                        customer.setName(getStringValue(ce, "Description"));
                        customer.setInn(getStringValue(ce, "ИНН"));
                        customer.setKpp(getStringValue(ce, "КПП"));
                        customer.setOgrn(getStringValue(ce, "РегистрационныйНомер"));
                        dataManager.save(customer);
                    }
                }

            } else if (type.equals("ФизическоеЛицо")) {

                if (dataManager.load(CustomerPrivate.class)
                        .query("select c from CustomerPrivate c where c.id1C = :id1C1")
                        .parameter("id1C1", id1C)
                        .optional()
                        .isEmpty()) {
                    CustomerPrivate customer = dataManager.create(CustomerPrivate.class);
                    customer.setType("PRIVATE");
                    customer.setId(UUID.randomUUID());
                    customer.setId1C(id1C);
                    customer.setName(getStringValue(ce, "Description"));
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

            String description = getStringValue(ce, "Description");
            if (description != null) {
                if (dataManager.load(IncomingDescription.class)
                        .query("select c from IncomingDescription c where c.name = :name")
                        .parameter("name", description)
                        .optional()
                        .isEmpty()) {
                    IncomingDescription entity = dataManager.create(IncomingDescription.class);
                    entity.setId(UUID.randomUUID());
                    entity.setName(description);
                    entity.setId1C(getStringValue(ce, "Ref_Key"));
                    dataManager.save(entity);
                }
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
                                "Ref_Key,Description,НомерСчета,Owner,Owner_Type")
                        .build();

        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator = getIterator(customURI);

        while (iterator.hasNext()) {
            ClientEntity ce = iterator.next();

            String accountNumber = getStringValue(ce, "НомерСчета");
            if (accountNumber != null) {
                if (dataManager.load(BankAccount.class)
                        .query("select c from BankAccount c where c.accountNumber = :accountNumber")
                        .parameter("accountNumber", accountNumber)
                        .optional()
                        .isEmpty()) {
                    String owner = getStringValue(ce, "Owner");
                    if (owner != null) {
                        Optional<Customer> optional = dataManager.load(Customer.class)
                                .query("select c from Customer c where c.id1C = :id1C1")
                                .parameter("id1C1", owner)
                                .optional();
                        if (optional.isEmpty()) {
                            BankAccount entity = dataManager.create(BankAccount.class);
                            entity.setId(UUID.randomUUID());
                            entity.setAccountNumber(accountNumber);
                            entity.setName(getStringValue(ce, "Description"));
                            entity.setId1C(getStringValue(ce, "Ref_Key"));
                            dataManager.save(entity);
                        }
                    }
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

        while (docIterator.hasNext()) {
            ClientEntity ce = docIterator.next();

            String refKey = getStringValue(ce, "Ref_Key");
            String docNumber = getStringValue(ce, "Number");
            if (docNumber == null) {
                continue;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date docDate = null;
            try {
                docDate = dateFormat.parse(ce.getProperty("Date").getValue().toString());
            } catch (ParseException | NullPointerException e) {
                continue;
            }

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

            Optional<Payment> optional = dataManager.load(Payment.class)
                    .query("select p from Payment p where p.number = :number1 and p.date = :date1")
                    .parameter("number1", docNumber)
                    .parameter("date1", docDate)
                    .optional();
            if (optional.isEmpty()) {
                Payment payment = dataManager.create(Payment.class);
                payment.setId(UUID.randomUUID());
                payment.setNumber(docNumber);
                payment.setDate(docDate);
                payment.setSum(BigDecimal.valueOf(sum));

                String customerId = getStringValue(ce, "Контрагент");
                if (customerId != null) {
                    Optional<Customer> customer =
                            dataManager.load(Customer.class)
                                    .query("select c from Customer c where c.id1C = :id1C1")
                                    .parameter("id1C1", customerId)
                                    .optional();
                    if (customer.isPresent()) {
                        payment.setCustomer(customer.get());
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }

                String bankAccountId = getStringValue(ce, "СчетОрганизации");
                if (bankAccountId != null) {
                    Optional<BankAccount> bankAccount =
                            dataManager.load(BankAccount.class)
                                    .query("select b from BankAccount b where b.id1C = :id1C1")
                                    .parameter("id1C1", bankAccountId)
                                    .optional();
                    bankAccount.ifPresent(payment::setBankAccount);
                }

                dataManager.save(payment);
            }
        }
    }
}