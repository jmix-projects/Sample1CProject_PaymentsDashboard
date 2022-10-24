package com.company.samplebankaccountingproject.screen.dashboardchart;

import com.company.samplebankaccountingproject.app.ExchangeOData;
import com.company.samplebankaccountingproject.entity.Payment;
import com.google.common.collect.ImmutableMap;
import io.jmix.charts.component.PieChart;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.dashboardsui.annotation.DashboardWidget;
import io.jmix.dashboardsui.event.DashboardEvent;
import io.jmix.dashboardsui.widget.RefreshableWidget;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.data.impl.ListDataProvider;
import io.jmix.ui.data.impl.MapDataItem;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("DashboardChart")
@UiDescriptor("dashboard-chart.xml")
@DashboardWidget(name = "Payments-chart")
public class DashboardChart extends ScreenFragment implements RefreshableWidget {
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

    private void updateChart() {
        List<KeyValueEntity> values =
                dataManager.loadValues("select customer.name, sum(e.sum) total " +
                                "from Payment e " +
                                "left join e.customer customer " +
                                "group by customer.name")
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

    @Override
    public void refresh(DashboardEvent dashboardEvent) {
        updateChart();
    }
}