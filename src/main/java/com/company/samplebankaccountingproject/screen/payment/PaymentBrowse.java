package com.company.samplebankaccountingproject.screen.payment;

import com.company.samplebankaccountingproject.app.ExchangeOData;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.samplebankaccountingproject.entity.Payment;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("Payment.browse")
@UiDescriptor("payment-browse.xml")
@LookupComponent("paymentsTable")
public class PaymentBrowse extends StandardLookup<Payment> {
    @Autowired
    private CollectionLoader<Payment> paymentsDl;
    @Autowired
    private ExchangeOData exchangeOData;
    @Autowired
    private Notifications notifications;

    @Subscribe("paymentsLoadBtn")
    public void onPaymentsLoadBtnClick(Button.ClickEvent event) {
        try {
            exchangeOData.loadCustomers();
            exchangeOData.loadIncomingDescriptions();
            exchangeOData.loadBankAccounts();
            exchangeOData.loadQuotes();
            exchangeOData.loadPayments();
            paymentsDl.load();
        } catch (Exception e) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption("Load payments error")
                    .withDescription(e.toString())
                    .show();
        }
    }
}