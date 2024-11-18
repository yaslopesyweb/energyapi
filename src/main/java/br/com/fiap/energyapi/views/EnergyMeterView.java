package br.com.fiap.energyapi.views;

import br.com.fiap.energyapi.domain.energyMeter.EnergyMeter;
import br.com.fiap.energyapi.domain.energyMeter.EnergyMeterService;
import br.com.fiap.energyapi.domain.user.User;
import br.com.fiap.energyapi.domain.user.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("vaadin-energy-meters")
public class EnergyMeterView extends VerticalLayout {

    private final EnergyMeterService energyMeterService;
    private final UserRepository userRepository;
    private final Grid<EnergyMeter> meterGrid = new Grid<>(EnergyMeter.class);

    private final TextField meterNameField = new TextField("Nome do Medidor");
    private final TextField userIdField = new TextField("ID do Usuário");
    private EnergyMeter selectedMeter;

    @Autowired
    public EnergyMeterView(EnergyMeterService energyMeterService, UserRepository userRepository) {
        this.energyMeterService = energyMeterService;
        this.userRepository = userRepository;

        add(new com.vaadin.flow.component.html.H1("Cadastro de Medidores de Energia"));

        meterGrid.setColumns("energyMeterId", "meterName", "user.userId");
        meterGrid.setItems(energyMeterService.getAllEnergyMeters());
        meterGrid.asSingleSelect().addValueChangeListener(event -> selectMeter(event.getValue()));

        Button saveButton = new Button("Salvar Medidor");
        saveButton.addClickListener(e -> saveMeter());

        Button deleteButton = new Button("Excluir Medidor");
        deleteButton.addClickListener(e -> deleteMeter());

        add(meterGrid, meterNameField, userIdField, saveButton, deleteButton);
    }

    private void selectMeter(EnergyMeter meter) {
        if (meter != null) {
            selectedMeter = meter;
            meterNameField.setValue(meter.getMeterName());
            userIdField.setValue(meter.getUser() != null ? String.valueOf(meter.getUser().getUserId()) : "");
        } else {
            clearForm();
        }
    }

    private void saveMeter() {
        if (meterNameField.isEmpty() || userIdField.isEmpty()) {
            Notification.show("Por favor, preencha todos os campos.");
            return;
        }

        if (selectedMeter == null) {
            selectedMeter = new EnergyMeter();
        }
        selectedMeter.setMeterName(meterNameField.getValue());

        try {
            String userId = userIdField.getValue();
            User user = userRepository.findById(userId).orElseThrow(() ->
                    new RuntimeException("Usuário não encontrado com o ID: " + userId));
            selectedMeter.setUser(user);

            energyMeterService.saveEnergyMeter(selectedMeter);
            meterGrid.setItems(energyMeterService.getAllEnergyMeters());
            clearForm();
            Notification.show("Medidor salvo com sucesso!");
        } catch (NumberFormatException e) {
            Notification.show("ID do Usuário inválido. Por favor, insira um valor numérico.");
        } catch (RuntimeException e) {
            Notification.show(e.getMessage());
        }
    }

    private void deleteMeter() {
        if (selectedMeter != null) {
            energyMeterService.deleteEnergyMeter(selectedMeter.getEnergyMeterId());
            meterGrid.setItems(energyMeterService.getAllEnergyMeters());
            clearForm();
            Notification.show("Medidor excluído com sucesso!");
        } else {
            Notification.show("Selecione um medidor para excluir.");
        }
    }

    private void clearForm() {
        selectedMeter = null;
        meterNameField.clear();
        userIdField.clear();
    }
}
