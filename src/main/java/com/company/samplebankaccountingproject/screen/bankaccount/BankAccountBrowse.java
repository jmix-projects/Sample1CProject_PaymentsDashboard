package com.company.samplebankaccountingproject.screen.bankaccount;

import io.jmix.ui.screen.*;
import com.company.samplebankaccountingproject.entity.BankAccount;

@UiController("BankAccount.browse")
@UiDescriptor("bank-account-browse.xml")
@LookupComponent("bankAccountsTable")
public class BankAccountBrowse extends StandardLookup<BankAccount> {
}