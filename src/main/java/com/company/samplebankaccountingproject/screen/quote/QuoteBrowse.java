package com.company.samplebankaccountingproject.screen.quote;

import com.company.samplebankaccountingproject.app.ExchangeOData;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.samplebankaccountingproject.entity.Quote;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("Quote.browse")
@UiDescriptor("quote-browse.xml")
@LookupComponent("quotesTable")
public class QuoteBrowse extends StandardLookup<Quote> {
    @Autowired
    private CollectionLoader<Quote> quotesDl;

    @Autowired
    private ExchangeOData exchangeOData;

    @Autowired
    private Notifications notifications;

    @Subscribe("quotesLoadBtn")
    public void onQuotesLoadBtnClick(Button.ClickEvent event) {
        try {
            exchangeOData.loadQuotes();
            quotesDl.load();
        } catch (Exception e) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption("Load quotes error")
                    .withDescription(e.toString())
                    .show();
        }
    }
}