package com.company.samplebankaccountingproject.screen.customerlegal;

import io.jmix.ui.screen.*;
import com.company.samplebankaccountingproject.entity.CustomerLegal;

@UiController("CustomerLegal.edit")
@UiDescriptor("customer-legal-edit.xml")
@EditedEntityContainer("customerLegalDc")
public class CustomerLegalEdit extends StandardEditor<CustomerLegal> {
}