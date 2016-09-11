package ua.com.juja.sqlcmd.controller.command;

import ua.com.juja.sqlcmd.model.DatabaseManager;
import ua.com.juja.sqlcmd.viuw.View;

import java.util.Arrays;

/**
 * Created by MEBELBOS-2 on 04.09.2016.
 */
public class List implements Command {

    private View view;
    private DatabaseManager manager;

    public List(View view, DatabaseManager manager ){
        this.view = view;
        this.manager = manager;
    }

    @Override
    public boolean canProcess(String command) {
        return command.equals("list");
    }

    @Override
    public void process(String command) {
        String[] tables = manager.getAllTablesOfDataBase();
        view.write(Arrays.toString(tables));
    }

}
