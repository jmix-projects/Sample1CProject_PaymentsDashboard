package com.company.samplebankaccountingproject.screen.payment;

import com.company.samplebankaccountingproject.app.ExchangeOData;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.DialogOutcome;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.component.Button;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.samplebankaccountingproject.entity.Payment;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

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
    @Autowired
    private Dialogs dialogs;

    @Subscribe("paymentsLoadBtn")
    public void onPaymentsLoadBtnClick(Button.ClickEvent event) {
        dialogs.createInputDialog(this)
                .withCaption("Please enter date")
                .withParameters(
                        InputParameter.dateParameter("date")
                                .withCaption("date")
                                .withRequired(true)
                )
                .withActions(DialogActions.OK_CANCEL)
                .withCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(DialogOutcome.OK)) {
                        Date date = closeEvent.getValue("date");
                        try {
                            exchangeOData.loadPayments(date);
                            paymentsDl.load();
                        } catch (Exception e) {
                            notifications.create(Notifications.NotificationType.ERROR)
                                    .withCaption("Load payments error")
                                    .withDescription(e.toString())
                                    .show();
                        }
                    }
                })
                .show();
    }
}