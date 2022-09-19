package com.company.samplebankaccountingproject.app;

import com.company.samplebankaccountingproject.entity.CustomerLegal;
import com.company.samplebankaccountingproject.entity.CustomerPrivate;
import com.company.samplebankaccountingproject.entity.ODataSettings;
import io.jmix.appsettings.AppSettings;
import io.jmix.core.DataManager;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetIteratorRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.http.BasicAuthHttpClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.UUID;

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

    public void loadCustomers() {
        ODataSettings oDataSettings = appSettings.load(ODataSettings.class);

        String baseURL = oDataSettings.getODataURL();
        String user = oDataSettings.getODataUser();
        String pass = oDataSettings.getODataPassword();

        client.getConfiguration().setHttpClientFactory(
                new BasicAuthHttpClientFactory(user, pass == null ? "" : pass)
        );

        URI customerURI =
                client.newURIBuilder(baseURL)
                        .appendEntitySetSegment("Catalog_Контрагенты")
                        .addCustomQueryOption("$select",
                                "Ref_Key,Description,ИНН,КПП,ЮридическоеФизическоеЛицо,РегистрационныйНомер")
                        .addCustomQueryOption("$filter", "IsFolder eq false")
                        .build();

        ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = client
                .getRetrieveRequestFactory()
                .getEntitySetIteratorRequest(customerURI);
        request.setAccept("application/json");
        ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = request.execute();

        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator = response.getBody();

        while (iterator.hasNext()) {
            ClientEntity ce = iterator.next();
            String type = ce.getProperty("ЮридическоеФизическоеЛицо").getValue().toString();
            if (type.equals("ЮридическоеЛицо")) {
                CustomerLegal customer = dataManager.create(CustomerLegal.class);
                customer.setType("LEGAL");
                customer.setId(UUID.randomUUID());
                customer.setId1C(ce.getProperty("Ref_Key").getValue().toString());
                customer.setName(ce.getProperty("Description").getValue().toString());
                customer.setInn(ce.getProperty("ИНН").getValue().toString());
                customer.setKpp(ce.getProperty("КПП").getValue().toString());
                customer.setOgrn(ce.getProperty("РегистрационныйНомер").getValue().toString());
                dataManager.save(customer);
            } else if (type.equals("ФизическоеЛицо")) {
                CustomerPrivate customer = dataManager.create(CustomerPrivate.class);
                customer.setType("PRIVATE");
                customer.setId(UUID.randomUUID());
                customer.setId1C(ce.getProperty("Ref_Key").getValue().toString());
                customer.setName(ce.getProperty("Description").getValue().toString());
            }
        }
    }

}