package br.com.fiap.energyapi.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin")
public class MenuView extends VerticalLayout {

    public MenuView() {
        // Configuração para centralizar todos os elementos
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setSizeFull();

        // Adicionando a logo
        Image logo = new Image("images/logo-image.png", "Logo do Projeto");
        logo.setHeight("160px");
        add(logo);

        // Título do menu
        add(new com.vaadin.flow.component.html.H2("Menu"));

        // Inicializa os botões
        Button userViewButton = new Button("Usuários");
        Button energyMeterViewButton = new Button("Medidores");
        Button deviceViewButton = new Button("Dispositivos");
        Button analysisViewButton = new Button("Análises");
        Button reportViewButton = new Button("Relatórios");

        // Definindo a cor dos botões
        String buttonColor = "#1E3E69";
        userViewButton.getStyle().set("background-color", buttonColor).set("color", "white");
        energyMeterViewButton.getStyle().set("background-color", buttonColor).set("color", "white");
        deviceViewButton.getStyle().set("background-color", buttonColor).set("color", "white");
        analysisViewButton.getStyle().set("background-color", buttonColor).set("color", "white");
        reportViewButton.getStyle().set("background-color", buttonColor).set("color", "white");

        // Ajuste de largura, altura e margem
        String widthValue = "377px";
        userViewButton.getStyle().set("width", widthValue);
        energyMeterViewButton.getStyle().set("width", widthValue);
        deviceViewButton.getStyle().set("width", widthValue);
        analysisViewButton.getStyle().set("width", widthValue);
        reportViewButton.getStyle().set("width", widthValue);


        String heightValue = "55px";
        userViewButton.getStyle().set("height", heightValue);
        energyMeterViewButton.getStyle().set("height", heightValue);
        deviceViewButton.getStyle().set("height", heightValue);
        analysisViewButton.getStyle().set("height", heightValue);
        reportViewButton.getStyle().set("height", heightValue);

        String marginValue = "3px";
        userViewButton.getStyle().set("margin", marginValue);
        energyMeterViewButton.getStyle().set("margin", marginValue);
        deviceViewButton.getStyle().set("margin", marginValue);
        analysisViewButton.getStyle().set("margin", marginValue);
        reportViewButton.getStyle().set("margin", marginValue);

        // Configura ações para os botões
        userViewButton.addClickListener(e ->
                userViewButton.getUI().ifPresent(ui -> ui.navigate("vaadin-users"))
        );
        reportViewButton.addClickListener(e ->
                reportViewButton.getUI().ifPresent(ui -> ui.navigate("vaadin-reports"))
        );
        deviceViewButton.addClickListener(e ->
                deviceViewButton.getUI().ifPresent(ui -> ui.navigate("vaadin-devices"))
        );
        energyMeterViewButton.addClickListener(e ->
                energyMeterViewButton.getUI().ifPresent(ui -> ui.navigate("vaadin-energy-meters"))
        );
        analysisViewButton.addClickListener(e ->
                analysisViewButton.getUI().ifPresent(ui -> ui.navigate("vaadin-device-analysis"))
        );

        // Adicionando os botões ao layout
        add(
                userViewButton,
                energyMeterViewButton,
                deviceViewButton,
                analysisViewButton,
                reportViewButton

        );
    }
}
