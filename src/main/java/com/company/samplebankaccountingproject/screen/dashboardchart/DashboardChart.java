package com.company.samplebankaccountingproject.screen.dashboardchart;

import com.company.samplebankaccountingproject.app.ExchangeOData;
import com.company.samplebankaccountingproject.entity.Payment;
import io.jmix.charts.component.PieChart;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.dashboardsui.annotation.DashboardWidget;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
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

    private void updateChart() {
        List<KeyValueEntity> values =
                dataManager.loadValues("select bankAccount.name, sum(e.sum) total " +
                                "from Payment e " +
                                "left join e.bankAccount bankAccount " +
                                "group by bankAccount.name")
                        .properties("name", "sum")
                        .list();
        for (KeyValueEntity value : values) {
            pie3dChart.addData(
                    MapDataItem.of("customer", value.getValue("name"),
                            "sum", value.getValue("sum"))
            );
        }
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

            exchangeOData.loadCustomers();
            exchangeOData.loadBankAccounts();
            exchangeOData.loadIncomingDescriptions();
            exchangeOData.loadQuotes();
            exchangeOData.loadPayments();

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
}