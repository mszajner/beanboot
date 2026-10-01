package io.github.mszajner.beanboot.setup;

import lombok.Data;

@Data
public class SetupForm {

    /** Database engine: MSSQL | PGSQL | MYSQL */
    private String connection = "MSSQL";
    private String host = "localhost";
    private int port = 1433;
    private String database = "";
    private String username = "";
    private String password = "";
}
