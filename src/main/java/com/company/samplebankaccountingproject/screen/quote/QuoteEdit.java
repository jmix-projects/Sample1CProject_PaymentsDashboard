package com.company.samplebankaccountingproject.screen.quote;

import io.jmix.ui.screen.*;
import com.company.samplebankaccountingproject.entity.Quote;

@UiController("Quote.edit")
@UiDescriptor("quote-edit.xml")
@EditedEntityContainer("quoteDc")
public class QuoteEdit extends StandardEditor<Quote> {
}