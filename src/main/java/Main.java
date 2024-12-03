public class Main {

    public static void main(String[] args) {
        if (!Server.isServerRunning("localhost", 8080)) {
            new Thread(() -> Server.startServer()).start();
        }

        DatabaseManager databaseManager = new DatabaseManager();
        LoginPanel loginPanel = new LoginPanel(databaseManager);
    }
}
