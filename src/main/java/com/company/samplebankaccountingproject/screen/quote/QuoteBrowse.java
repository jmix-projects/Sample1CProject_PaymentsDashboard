package com.company.samplebankaccountingproject.screen.quote;

import io.jmix.ui.screen.*;
import com.company.samplebankaccountingproject.entity.Quote;

@UiController("Quote.browse")
@UiDescriptor("quote-browse.xml")
@LookupComponent("quotesTable")
public class QuoteBrowse extends StandardLookup<Quote> {
}