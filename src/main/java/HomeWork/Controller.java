package HomeWork;

public class Controller {
    GUI gui;
    Core core;

    public Controller() {
        gui = new GUI();
        core = new Core();
        gui.set_on_data_changed(() -> {
            if (core.get_line_id() != 0) {
                gui.enable_step_button(false);
                gui.enable_full_pass_button(false);
            }
        });
        gui.setStepButtonListener(this::onStepButtonClicked);
        gui.setFullPassButtonListener(this::onFullPassButtonClicked);
        gui.setRestartButtonListener(this::onRestartButtonClicked);
        gui.setResetButtonListener(this::onResetButtonClicked);
    }

    private void onStepButtonClicked() {
        if (core.get_line_id() == 0) {
            core.one_step(gui.get_source_code());
        } else {
            core.one_step(null);
        }
        updateView();
        if (core.get_line_id() == gui.get_source_code().length) {
            core.reset();
        }
        gui.enable_source_code(core.isCan_be_editable());
        gui.enable_select_source_file(core.isCan_be_editable());
        gui.enable_select_result_file(core.isCan_be_editable());
    }

    private void onFullPassButtonClicked() {
        core.reset();
        core.full_pass(gui.get_source_code());
        gui.enable_source_code(core.isCan_be_editable());
        updateView();
        core.reset();
        gui.enable_source_code(true);
        gui.enable_select_source_file(true);
        gui.enable_select_result_file(true);
    }

    private void onRestartButtonClicked() {
        core.reset();
        gui.restart();
        gui.enable_source_code(core.isCan_be_editable());
        gui.enable_select_source_file(core.isCan_be_editable());
        gui.enable_select_result_file(core.isCan_be_editable());
        gui.enable_step_button(true);
        gui.enable_full_pass_button(true);
    }

    private void onResetButtonClicked() {
        core.reset();
        gui.clear();
        gui.enable_source_code(core.isCan_be_editable());
        gui.enable_select_source_file(core.isCan_be_editable());
        gui.enable_select_result_file(core.isCan_be_editable());
        gui.enable_step_button(true);
        gui.enable_full_pass_button(true);
    }

    private void updateView() {
        gui.update_def_table(core.getMacroDefinitionTable());
        gui.update_nam_table(core.getMacroNameTable());
        gui.update_var_table(core.getVariableTable());
        gui.update_result_code(core.getResultCodeAsString());
        String message;
        boolean flag = false;
        if (!core.getERROR().isEmpty()) {
            message = core.getERROR();
            flag = true;
            gui.enable_source_code(core.isCan_be_editable());
            gui.enable_step_button(false);
            gui.enable_full_pass_button(false);
        } else {
            message = "Выполнена строка " + core.get_line_id();
        }
        gui.set_message(message, flag);
    }
}
