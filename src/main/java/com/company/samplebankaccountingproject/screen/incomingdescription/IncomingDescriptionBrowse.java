package com.company.samplebankaccountingproject.screen.incomingdescription;

import com.company.samplebankaccountingproject.app.ExchangeOData;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.samplebankaccountingproject.entity.IncomingDescription;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("IncomingDescription.browse")
@UiDescriptor("incoming-description-browse.xml")
@LookupComponent("incomingDescriptionsTable")
public class IncomingDescriptionBrowse extends StandardLookup<IncomingDescription> {
    @Autowired
    private CollectionLoader<IncomingDescription> incomingDescriptionsDl;

    @Autowired
    private ExchangeOData exchangeOData;

    @Autowired
    private Notifications notifications;

    @Subscribe("incomingDescriptionLoadBtn")
    public void onIncomingDescriptionLoadBtnClick(Button.ClickEvent event) {
        try {
            exchangeOData.loadIncomingDescriptions();
            incomingDescriptionsDl.load();
        } catch (Exception e) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption("Load incoming descriptions error")
                    .withDescription(e.toString())
                    .show();
        }
    }
}