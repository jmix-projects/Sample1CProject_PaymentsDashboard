package com.company.samplebankaccountingproject.screen.customerprivate;

import io.jmix.ui.screen.*;
import com.company.samplebankaccountingproject.entity.CustomerPrivate;

@UiController("CustomerPrivate.edit")
@UiDescriptor("customer-private-edit.xml")
@EditedEntityContainer("customerPrivateDc")
public class CustomerPrivateEdit extends StandardEditor<CustomerPrivate> {
    @Subscribe
    public void onAfterCommitChanges(AfterCommitChangesEvent event) {

    }


}