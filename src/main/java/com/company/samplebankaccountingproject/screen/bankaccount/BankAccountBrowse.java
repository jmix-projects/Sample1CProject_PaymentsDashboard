package com.company.samplebankaccountingproject.screen.bankaccount;

import com.company.samplebankaccountingproject.app.ExchangeOData;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.samplebankaccountingproject.entity.BankAccount;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("BankAccount.browse")
@UiDescriptor("bank-account-browse.xml")
@LookupComponent("bankAccountsTable")
public class BankAccountBrowse extends StandardLookup<BankAccount> {
    @Autowired
    private CollectionLoader<BankAccount> bankAccountsDl;

    @Autowired
    private ExchangeOData exchangeOData;

    @Autowired
    private Notifications notifications;

    @Subscribe("bankAccountsLoadBtn")
    public void onBankAccountsLoadBtnClick(Button.ClickEvent event) {
        try {
            exchangeOData.loadBankAccounts();
            bankAccountsDl.load();
        } catch (Exception e) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption("Load bank accounts error")
                    .withDescription(e.toString())
                    .show();
        }
    }
}