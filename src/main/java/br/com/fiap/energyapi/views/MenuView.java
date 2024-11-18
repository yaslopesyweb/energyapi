package br.com.fiap.energyapi.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin")
public class MenuView extends VerticalLayout {

    public MenuView() {
        add(new com.vaadin.flow.component.html.H1("Menu Principal"));

        // Inicializa os botões
        Button deviceViewButton = new Button("Dispositivos");
        Button reportViewButton = new Button("Relatórios");
        Button analysisViewButton = new Button("Análises");
        Button energyMeterViewButton = new Button("Medidores");
        Button userViewButton = new Button("Usuários");

        // Configura ações para os botões
        deviceViewButton.addClickListener(e ->
                deviceViewButton.getUI().ifPresent(ui -> ui.navigate("vaadin-devices"))
        );
        reportViewButton.addClickListener(e ->
                reportViewButton.getUI().ifPresent(ui -> ui.navigate("vaadin-reports"))
        );
        analysisViewButton.addClickListener(e ->
                analysisViewButton.getUI().ifPresent(ui -> ui.navigate("vaadin-device-analysis"))
        );
        energyMeterViewButton.addClickListener(e ->
                energyMeterViewButton.getUI().ifPresent(ui -> ui.navigate("vaadin-energy-meters"))
        );
        userViewButton.addClickListener(e ->
                userViewButton.getUI().ifPresent(ui -> ui.navigate("vaadin-users"))
        );

        add(
                deviceViewButton,
                reportViewButton,
                analysisViewButton,
                energyMeterViewButton,
                userViewButton
        );
    }
}
