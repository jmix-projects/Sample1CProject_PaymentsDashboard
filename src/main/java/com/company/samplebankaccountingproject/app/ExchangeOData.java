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

    private Optional<String> getStringValue(ClientEntity ce, String property) {
        ClientProperty clientProperty = ce.getProperty(property);
        if (clientProperty != null) {
            return Optional.of(clientProperty.getValue().toString());
        } else {
            return Optional.empty();
        }
    }

    private Optional<Date> getDateValue(ClientEntity ce, String property) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Optional<String> date = getStringValue(ce, property);
        if (date.isPresent()) {
            try {
                return Optional.of(dateFormat.parse(date.get()));
            } catch (ParseException | NullPointerException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
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
            Optional<String> type = getStringValue(ce, "ЮридическоеФизическоеЛицо");
            Optional<String> id1C = getStringValue(ce, "Ref_Key");
            if (type.isEmpty() || id1C.isEmpty()) {
                continue;
            }

            if (type.get().equals("ЮридическоеЛицо")) {

                Optional<String> inn = getStringValue(ce, "ИНН");
                if (inn.isPresent()) {
                    if (dataManager.load(CustomerLegal.class)
                            .query("select c from CustomerLegal c where c.inn = :inn1")
                            .parameter("inn1", inn.get())
                            .optional()
                            .isEmpty()) {
                        CustomerLegal customer = dataManager.create(CustomerLegal.class);
                        customer.setType("LEGAL");
                        customer.setId(UUID.randomUUID());
                        customer.setId1C(id1C.get());
                        getStringValue(ce, "Description").ifPresent(customer::setName);
                        getStringValue(ce, "ИНН").ifPresent(customer::setInn);
                        getStringValue(ce, "КПП").ifPresent(customer::setKpp);
                        getStringValue(ce, "РегистрационныйНомер").ifPresent(customer::setOgrn);
                        dataManager.save(customer);
                    }
                }

            } else if (type.get().equals("ФизическоеЛицо")) {

                if (dataManager.load(CustomerPrivate.class)
                        .query("select c from CustomerPrivate c where c.id1C = :id1C1")
                        .parameter("id1C1", id1C.get())
                        .optional()
                        .isEmpty()) {
                    CustomerPrivate customer = dataManager.create(CustomerPrivate.class);
                    customer.setType("PRIVATE");
                    customer.setId(UUID.randomUUID());
                    customer.setId1C(id1C.get());
                    getStringValue(ce, "Description").ifPresent(customer::setName);
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

            Optional<String> description = getStringValue(ce, "Description");
            if (description.isPresent()) {
                if (dataManager.load(IncomingDescription.class)
                        .query("select c from IncomingDescription c where c.name = :name")
                        .parameter("name", description.get())
                        .optional()
                        .isEmpty()) {
                    IncomingDescription entity = dataManager.create(IncomingDescription.class);
                    entity.setId(UUID.randomUUID());
                    entity.setName(description.get());
                    getStringValue(ce, "Ref_Key").ifPresent(entity::setId1C);
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

            Optional<String> accountNumber = getStringValue(ce, "НомерСчета");
            if (accountNumber.isPresent()) {
                if (dataManager.load(BankAccount.class)
                        .query("select c from BankAccount c " +
                                "where c.accountNumber = :accountNumber")
                        .parameter("accountNumber", accountNumber.get())
                        .optional()
                        .isEmpty()) {
                    Optional<String> owner = getStringValue(ce, "Owner");
                    if (owner.isPresent()) {
                        if (dataManager.load(Customer.class)
                                .query("select c from Customer c where c.id1C = :id1C1")
                                .parameter("id1C1", owner.get())
                                .optional()
                                .isEmpty()) {
                            BankAccount entity = dataManager.create(BankAccount.class);
                            entity.setId(UUID.randomUUID());
                            entity.setAccountNumber(accountNumber.get());
                            getStringValue(ce, "Description").ifPresent(entity::setName);
                            getStringValue(ce, "Ref_Key").ifPresent(entity::setId1C);
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

            Optional<String> refKey = getStringValue(ce, "Ref_Key");
            refKey.ifPresent(s -> table.put(s, ce));
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

            Optional<String> refKey = getStringValue(ce, "Ref_Key");
            Optional<String> docNumber = getStringValue(ce, "Number");
            Optional<Date> docDate = getDateValue(ce, "Date");
            if (refKey.isEmpty() || docNumber.isEmpty() || docDate.isEmpty()) {
                continue;
            }

            ClientEntity ceTable = table.get(refKey.get());
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
                    .query("select p from Payment p " +
                            "where p.number = :number1 and p.date = :date1")
                    .parameter("number1", docNumber.get())
                    .parameter("date1", docDate.get())
                    .optional();
            if (optional.isEmpty()) {
                Payment payment = dataManager.create(Payment.class);
                payment.setId(UUID.randomUUID());
                payment.setNumber(docNumber.get());
                payment.setDate(docDate.get());
                payment.setSum(BigDecimal.valueOf(sum));

                Optional<String> customerId = getStringValue(ce, "Контрагент");
                if (customerId.isPresent()) {
                    Optional<Customer> customer =
                            dataManager.load(Customer.class)
                                    .query("select c from Customer c where c.id1C = :id1C1")
                                    .parameter("id1C1", customerId.get())
                                    .optional();
                    if (customer.isPresent()) {
                        payment.setCustomer(customer.get());
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }

                Optional<String> bankAccountId = getStringValue(ce, "СчетОрганизации");
                if (bankAccountId.isPresent()) {
                    dataManager.load(BankAccount.class)
                            .query("select b from BankAccount b where b.id1C = :id1C1")
                            .parameter("id1C1", bankAccountId)
                            .optional()
                            .ifPresent(payment::setBankAccount);
                }

                dataManager.save(payment);
            }
        }
    }
}