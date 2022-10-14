package com.company.samplebankaccountingproject.screen.incomingdescription;

import io.jmix.ui.screen.*;
import com.company.samplebankaccountingproject.entity.IncomingDescription;

@UiController("IncomingDescription.edit")
@UiDescriptor("incoming-description-edit.xml")
@EditedEntityContainer("incomingDescriptionDc")
public class IncomingDescriptionEdit extends StandardEditor<IncomingDescription> {
}