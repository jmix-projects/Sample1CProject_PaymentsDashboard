package com.company.samplebankaccountingproject.screen.incomingdescription;

import io.jmix.ui.screen.*;
import com.company.samplebankaccountingproject.entity.IncomingDescription;

@UiController("IncomingDescription.browse")
@UiDescriptor("incoming-description-browse.xml")
@LookupComponent("incomingDescriptionsTable")
public class IncomingDescriptionBrowse extends StandardLookup<IncomingDescription> {
}