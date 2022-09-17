package com.company.samplebankaccountingproject.screen.customer;

import com.company.samplebankaccountingproject.entity.CustomerLegal;
import com.company.samplebankaccountingproject.entity.CustomerPrivate;
import com.company.samplebankaccountingproject.entity.CustomerType;
import com.company.samplebankaccountingproject.screen.customerlegal.CustomerLegalEdit;
import com.company.samplebankaccountingproject.screen.customerprivate.CustomerPrivateEdit;
import io.jmix.core.DataManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.app.inputdialog.DialogOutcome;
import io.jmix.ui.app.inputdialog.InputDialog;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.screen.*;
import com.company.samplebankaccountingproject.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("Customer.browse")
@UiDescriptor("customer-browse.xml")
@LookupComponent("customersTable")
public class CustomerBrowse extends StandardLookup<Customer> {
    @Autowired
    private Notifications notifications;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private GroupTable<Customer> customersTable;
    @Autowired
    private CollectionLoader<Customer> customersDl;

    @Subscribe("CustomerTypeinputDialog")
    public void onCustomerTypeinputDialogInputDialogClose(InputDialog.InputDialogCloseEvent event) {
        if (event.closedWith(DialogOutcome.OK)) {
            if (event.getValue("CustomerType") == CustomerType.LEGAL) {
                CustomerLegal newCustomer = dataManager.create(CustomerLegal.class);
                newCustomer.setType("LEGAL");
                CustomerLegalEdit screen = screenBuilders.screen(this)
                        .withScreenClass(CustomerLegalEdit.class)
                        .withAfterCloseListener(e -> {
                            customersDl.load();
                        })
                        .build();
                screen.setEntityToEdit(newCustomer);
                screen.show();
            } else {
                CustomerPrivate newCustomer = dataManager.create(CustomerPrivate.class);
                newCustomer.setType("PRIVATE");
                CustomerPrivateEdit screen = screenBuilders.screen(this)
                        .withScreenClass(CustomerPrivateEdit.class)
                        .withAfterCloseListener(e -> {
                            customersDl.load();
                        })
                        .build();
                screen.setEntityToEdit(newCustomer);
                screen.show();
            };
        }
    }

}