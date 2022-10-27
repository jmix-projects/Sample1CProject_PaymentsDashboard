package com.company.samplebankaccountingproject.screen.dashboardchart;

import com.company.samplebankaccountingproject.app.ExchangeOData;
import com.company.samplebankaccountingproject.entity.Payment;
import com.google.common.collect.ImmutableMap;
import io.jmix.charts.component.PieChart;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.dashboardsui.annotation.DashboardWidget;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.DateField;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.data.impl.ListDataProvider;
import io.jmix.ui.data.impl.MapDataItem;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@UiController("DashboardChart")
@UiDescriptor("dashboard-chart.xml")
@DashboardWidget(name = "Payments-chart")
public class DashboardChart extends ScreenFragment {
    @Autowired
    private PieChart pie3dChart;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Button loadFrom1C;
    @Autowired
    private ExchangeOData exchangeOData;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DateField<Date> startDate;
    @Autowired
    private DateField<Date> endDate;

    private void updateChart() {
        Optional<Date> startDateOpt = Optional.ofNullable(startDate.getValue());
        Optional<Date> endDateOpt = Optional.ofNullable(endDate.getValue());
        List<KeyValueEntity> values =
                dataManager.loadValues("select customer.name, sum(p.sum) total " +
                                "from Payment p " +
                                "left join p.customer customer " +
                                "where p.date between :startDate and :endDate " +
                                "group by customer.name")
                        .parameter("startDate", startDateOpt.orElseGet(Date::new))
                        .parameter("endDate", endDateOpt.orElseGet(Date::new))
                        .properties("name", "sum")
                        .list();
        ListDataProvider dataProvider = new ListDataProvider();
        for (KeyValueEntity value : values) {
            dataProvider.addItem(new MapDataItem(
                    ImmutableMap.of("customer", value.getValue("name"),
                            "sum", value.getValue("sum"))
            ));
        }
        pie3dChart.setDataProvider(dataProvider);
    }

    @Subscribe
    public void onInit(InitEvent event) {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(today);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        setTimeToBeginningOfDay(calendar);
        startDate.setValue(calendar.getTime());

        calendar.setTime(today);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        setTimeToBeginningOfDay(calendar);
        endDate.setValue(calendar.getTime());

        if(dataManager.load(Payment.class)
                .query("select p from Payment p")
                .optional()
                .isEmpty()) {
            pie3dChart.setVisible(false);
            loadFrom1C.setVisible(true);
        } else {
            pie3dChart.setVisible(true);
            loadFrom1C.setVisible(false);
            updateChart();
        }
    }

    @Subscribe("loadFrom1C")
    public void onLoadFrom1CClick(Button.ClickEvent event) {
        try {

            exchangeOData.loadReferences();
            exchangeOData.loadQuotes();
            exchangeOData.loadPayments(null);

            notifications.create()
                    .withDescription("Data has been loaded")
                    .withPosition(Notifications.Position.BOTTOM_LEFT)
                    .withHideDelayMs(500)
                    .show();

            loadFrom1C.setVisible(false);
            pie3dChart.setVisible(true);
            updateChart();

        } catch (Exception e) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption("Error")
                    .withDescription(e.getMessage())
                    .show();
        }
    }

    @Subscribe("startDate")
    public void onStartDateValueChange(HasValue.ValueChangeEvent<Date> event) {
        updateChart();
    }

    @Subscribe("endDate")
    public void onEndDateValueChange(HasValue.ValueChangeEvent<Date> event) {
        updateChart();
    }

    private static void setTimeToBeginningOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}