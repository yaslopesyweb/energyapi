package br.com.fiap.energyapi.views;

import br.com.fiap.energyapi.domain.user.User;
import br.com.fiap.energyapi.domain.user.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("vaadin-users")
public class UserView extends VerticalLayout {

    private final UserService userService; // Serviço para gerenciar usuários
    private final Grid<User> userGrid = new Grid<>(User.class); // Grid para listar usuários

    private final TextField emailField = new TextField("E-mail");
    private final TextField createdAtField = new TextField("Data de Criação");

    private User selectedUser;

    @Autowired
    public UserView(UserService userService) {
        this.userService = userService;

        add(new com.vaadin.flow.component.html.H1("Gerenciamento de Usuários"));

        // Configuração do Grid
        userGrid.setColumns("userId", "email", "createdAt");
        userGrid.setItems(userService.getAllUsers());
        userGrid.asSingleSelect().addValueChangeListener(event -> selectUser(event.getValue()));

        // Botões de ação
        Button saveButton = new Button("Salvar Usuário");
        saveButton.addClickListener(e -> saveUser());
        Button deleteButton = new Button("Excluir Usuário");
        deleteButton.addClickListener(e -> deleteUser());

        // Adicionando componentes à interface
        add(userGrid, emailField, createdAtField, saveButton, deleteButton);
    }

    private void selectUser(User user) {
        if (user != null) {
            selectedUser = user;
            emailField.setValue(user.getEmail());
            createdAtField.setValue(user.getCreatedAt() != null ? user.getCreatedAt() : "");
        } else {
            clearForm();
        }
    }

    private void saveUser() {
        if (selectedUser == null) {
            selectedUser = new User();
        }
        selectedUser.setEmail(emailField.getValue());
        selectedUser.setCreatedAt(createdAtField.getValue());

        try {
            userService.saveUser(selectedUser);
            userGrid.setItems(userService.getAllUsers());
            clearForm();
            Notification.show("Usuário salvo com sucesso!");
        } catch (RuntimeException e) {
            Notification.show("Erro ao salvar usuário: " + e.getMessage());
        }
    }

    private void deleteUser() {
        if (selectedUser != null) {
            userService.deleteUser(selectedUser.getUserId());
            userGrid.setItems(userService.getAllUsers());
            clearForm();
            Notification.show("Usuário excluído com sucesso!");
        } else {
            Notification.show("Selecione um usuário para excluir.");
        }
    }

    private void clearForm() {
        selectedUser = null;
        emailField.clear();
        createdAtField.clear();
    }
}
