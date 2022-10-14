package com.company.samplebankaccountingproject.screen.payment;

import io.jmix.ui.screen.*;
import com.company.samplebankaccountingproject.entity.Payment;

@UiController("Payment.browse")
@UiDescriptor("payment-browse.xml")
@LookupComponent("paymentsTable")
public class PaymentBrowse extends StandardLookup<Payment> {
}