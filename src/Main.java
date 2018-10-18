import UI.MainView;

class Main {
    private static final String VERSION = "1.0";
    public static void main(String[] args) {
        System.out.println("CANLogTool version $VERSION");
		MainView view = new MainView();
		view.start();
	}
}
